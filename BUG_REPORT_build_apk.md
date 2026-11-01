# Bug Report — công cụ `build_apk` theo dõi nhầm workflow

## 1. Tóm tắt

Công cụ `build_apk` báo **build thất bại (`failure`)** trong khi build APK thực tế **đã thành công (`success`)**.

Nguyên nhân: repo có nhiều workflow cùng lúc. Công cụ lấy **run mới nhất của toàn repo** (hoặc run mới nhất bất kể workflow) thay vì lọc theo đúng workflow build APK.

## 2. Môi trường tái hiện

- Repo: `hieu012-cyber/meow-build` (public, owner `hieu012-cyber`)
- Branch mặc định: `main`
- Commit: `d93a15dc818233b15cf4ef70a4e6c10a59683004`
- Dự án Android đặt trong thư mục `android/`

## 3. Trạng thái repo tại thời điểm lỗi

Repo tồn tại **2 workflow**:

| Workflow | File | Branch | Workflow ID | Run ID | Kết quả thật |
|---|---|---|---|---|---|
| `Build Android APK` | `.github/workflows/build-apk.yml` | `main` | 403551670 | 41308990918 | ✅ **success** |
| `meow-build` | `.github/workflows/meow-build.yml` | `meow-build` | 403552093 | 41309022920 | ❌ failure |

Lưu ý quan trọng: file `.github/workflows/meow-build.yml` **đã bị xoá khỏi repo** (API `contents` trả 404), nhưng GitHub vẫn giữ record workflow + run cũ.

## 4. Các bước tái hiện

1. Cấu hình GitHub đúng (token + repo `meow-build`).
2. Gọi `git_push` → push thành công commit `d93a15d` lên `main`.
3. Gọi `build_apk`.
4. **Kỳ vọng:** trả về `success` + link artifact `utility-hub-debug-apk`.
5. **Thực tế:** trả về `failure` kèm log KHÔNG phải của `build-apk.yml`.

## 5. Bằng chứng sai lệch

Log mà `build_apk` trả về chứa các dòng **không hề tồn tại** trong `.github/workflows/build-apk.yml` của dự án:

```
with:
  java-version: 21        ← file dự án ghi '17'
```

```
echo 'Không tìm thấy pom.xml hay build.gradle' && exit 1
```
→ Dòng này không có trong `build-apk.yml`. Đây là nội dung của `meow-build.yml` (workflow build plugin tự sinh, chạy trên branch `meow-build`).

Ngoài ra log có cảnh báo:
```
setup-java v4 is deprecated ... migrate to actions/setup-java@v5
Node.js 20 is deprecated ... actions/checkout@v4, actions/setup-java@v4
```
→ `build-apk.yml` của dự án dùng `setup-java@v4` + `java-version: '17'`, không phải 21.

## 6. Xác minh bằng API GitHub (ground truth)

```http
GET https://api.github.com/repos/hieu012-cyber/meow-build/actions/workflows/403551670/runs
```
Kết quả:
```json
{
  "name": "Build Android APK",
  "head_branch": "main",
  "head_sha": "d93a15dc818233b15cf4ef70a4e6c10a59683004",
  "path": ".github/workflows/build-apk.yml",
  "conclusion": "success"
}
```

Artifact đã sinh ra bình thường:
```http
GET https://api.github.com/repos/hieu012-cyber/meow-build/actions/runs/41308990918/artifacts
```
```json
{
  "name": "utility-hub-debug-apk",
  "size_in_bytes": 4703933,
  "expired": false,
  "archive_download_url": "https://api.github.com/repos/hieu012-cyber/meow-build/actions/artifacts/13084077772/zip"
}
```

## 7. Đề xuất sửa

### 7.1. Lọc run theo đúng workflow (bắt buộc)

Không dùng `GET /repos/{owner}/{repo}/actions/runs` (trả về run của mọi workflow, sắp theo thời gian).

Thay bằng một trong hai:

- **Cách A — theo workflow ID/path:**
  ```
  GET /repos/{owner}/{repo}/actions/workflows/{workflow_id}/runs?per_page=1
  ```
  hoặc lọc client-side: `runs[].path == '.github/workflows/build-apk.yml'`

- **Cách B — cấu hình được tên workflow:**
  Thêm setting `workflow_file` (mặc định `build-apk.yml`), hoặc `workflow_name`.



### 7.2. Không dựa vào "run mới nhất"

`created_at` mới nhất chưa chắc là run của workflow cần build. Phải filter theo `path`/`workflow_id` TRƯỚC, rồi mới lấy run mới nhất của tập đã lọc.

### 7.3. Xử lý workflow đã bị xoá file

`meow-build.yml` đã 404 ở `contents` nhưng vẫn còn trong `actions/workflows` (state active) và còn run cũ. Cần chấp nhận việc này và chỉ tin run có `path` khớp file cấu hình, không tin `state` của workflow.

### 7.4. Trả về artifact khi thành công

Khi `conclusion == success`, công cụ nên trả luôn:
- `run_id`, `html_url`
- danh sách artifact: `name`, `size_in_bytes`, `archive_download_url`, `expires_at`

Hiện tại kể cả khi build đúng, công cụ cũng không trả thông tin artifact, khiến người dùng không biết tải APK ở đâu.

### 7.5. Trả log đúng run

Khi báo lỗi, phải kèm log của đúng run vừa lấy được sau khi filter, kèm `run_id` + `head_sha` + `path` để người dùng đối chiếu. Log hiện tại gây hiểu nhầm hoàn toàn.

## 8. Phụ lục — lỗi liên quan đã gặp (đã khắc phục)

| Lỗi | Nguyên nhân thật | Ghi chú |
|---|---|---|
| `Chưa cấu hình repo GitHub` | Chưa nhập token/repo | OK |
| `HTTP 404 Not Found` | Tên repo sai: `meowcore-build` -> đúng là `meow-build` | Công cụ chỉ trả 404 chung chung, không nói rõ "repo không tồn tại" vs "sai tên" |
| `HTTP 401 Requires authentication` | Token chưa được nạp vào app | OK, đã sửa khi dán token |

**Đề xuất thêm:** khi gặp 404, phân biệt rõ:
- repo không tồn tại / sai tên -> gợi ý kiểm tra `owner/repo`
- repo private + token thiếu scope -> gợi ý cấp quyền `repo` + `workflow`
