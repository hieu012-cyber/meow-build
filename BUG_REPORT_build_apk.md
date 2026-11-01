# Bug Report — công cụ `build_apk` build sai hoàn toàn

> Người báo cáo: MeowCode Agent · Ngày: 2026-11-01
> Repo test: `hieu012-cyber/meow-build`

---

## 1. Tóm tắt

Công cụ `build_apk` **không hề build APK**. Nó:

1. Tạo/push lên một branch riêng tên `meow-build` thay vì dùng branch `main`
2. Tự sinh workflow `.github/workflows/meow-build.yml` có **lệnh fail cứng** `exit 1`
3. Workflow đó build **plugin JAR** (`build/libs/*.jar`), không liên quan tới Android
4. Trả về `failure` + log của workflow sai → gây hiểu nhầm

Trong khi đó `build-apk.yml` có sẵn trong repo (trên `main`) build APK **thành công** (artifact 4.7 MB), nhưng `build_apk` không dùng tới.

---

## 2. Môi trường

| Mục | Giá trị |
|---|---|
| Repo | `hieu012-cyber/meow-build` (public) |
| Branch chính | `main` @ `d93a15dc818233b15cf4ef70a4e6c10a59683004` |
| Branch lạ | `meow-build` @ `5213378293dcc2ec2456af8eb30031946565d95d` |
| Workflow đúng | `build-apk.yml` — ID `403551670` |
| Workflow sai | `meow-build.yml` — ID `403552093` |
| Cấu trúc | `android/` (Gradle), `app/` (web), `index.html`... |

---

## 3. Bằng chứng

### 3.1. Hai branch tồn tại song song

```json
GET /repos/hieu012-cyber/meow-build/branches
[
  { "name": "main",       "sha": "d93a15d..." },
  { "name": "meow-build", "sha": "5213378..." }
]
```

### 3.2. Workflow do công cụ tự sinh (trên branch `meow-build`)

```yaml
name: meow-build
on:
  push:
    branches: [ meow-build ]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
      - name: Build
        run: echo 'Không tìm thấy pom.xml hay build.gradle' && exit 1
      - uses: actions/upload-artifact@v4
        if: success()
        with:
          name: plugin
          path: build/libs/*.jar
```

Vấn đề trong file này:
- `java-version: '21'` — không khớp yêu cầu Android (17)
- `run: echo '...' && exit 1` — **luôn fail**, không build gì
- `path: build/libs/*.jar` — build JAR, không phải APK
- `branches: [ meow-build ]` — chạy trên branch lạ, nơi không có code

### 3.3. Thư mục local KHÔNG có file này

```
list_dir .github/workflows  →  [build-apk.yml]
```

`meow-build.yml` chỉ tồn tại trên remote, do công cụ sinh ra phía server.

### 3.4. Run APK đúng thì thành công

```json
GET /repos/.../actions/workflows/403551670/runs
{
  "name": "Build Android APK",
  "head_branch": "main",
  "conclusion": "success",
  "artifacts": [{ "name": "utility-hub-debug-apk", "size_in_bytes": 4703933 }]
}
```

### 3.5. Run mà `build_apk` báo lỗi

```json
{
  "name": "meow-build",
  "head_branch": "meow-build",
  "path": ".github/workflows/meow-build.yml",
  "display_title": "MeowCode Plugin build 1793540711853",
  "conclusion": "failure"
}
```

---

## 4. Root cause

`build_apk` đang hoạt động như **build_plugin**, không phải build APK:

| Bước đúng cần làm | Công cụ đang làm |
|---|---|
| Push code lên `main` | Push lên branch mới `meow-build` |
| Dùng `.github/workflows/build-apk.yml` | Tự sinh `meow-build.yml` |
| `working-directory: ./android` + `gradle assembleDebug` | `echo ... && exit 1` |
| Upload `android/app/build/outputs/apk/**/*.apk` | Upload `build/libs/*.jar` |
| Theo dõi workflow `403551670` | Theo dõi workflow `403552093` |

Có thể nguyên nhân gốc là: công cụ **không detect được project Android** (vì `android/` là thư mục con, không phải root), nên rơi vào nhánh fallback "plugin build" và dùng template sai.

---

## 5. Đề xuất sửa

### 5.1. Không tạo branch riêng

Dùng branch hiện tại của repo (`main`). Chỉ tạo branch khi người dùng yêu cầu rõ.

### 5.2. Ưu tiên workflow có sẵn

Nếu repo đã có `.github/workflows/*.yml` khớp mục đích (chứa `assembleDebug` / `assembleRelease`), **dùng nó**, không tự sinh file mới.

### 5.3. Detect project Android đúng

Tìm đệ quy (không chỉ root):
- `**/build.gradle`, `**/build.gradle.kts` có `com.android.application`
- `**/settings.gradle` có `include ':app'`
- `**/gradlew`

Nếu tìm thấy trong `android/` → chạy `gradle assembleDebug` với `working-directory: ./android`.

### 5.4. Không bao giờ sinh workflow có `exit 1` cứng

Nếu không detect được project, trả lỗi rõ ràng cho người dùng:
> "Không tìm thấy build.gradle trong repo. Kiểm tra lại cấu trúc dự án."

Đừng tạo workflow rồi để nó fail — gây tốn thời gian debug sai hướng.

### 5.5. Lọc run theo đúng workflow

Xem mục 6 bên dưới.

---

## 6. Lỗi phụ: theo dõi nhầm run

Công cụ lấy **run mới nhất của toàn repo** thay vì run của workflow build APK:

```
GET /repos/{owner}/{repo}/actions/runs        ← SAI (mọi workflow)
```

Đúng phải là:

```
GET /repos/{owner}/{repo}/actions/workflows/{workflow_id}/runs?per_page=1
```

hoặc filter client-side: `runs[].path == '.github/workflows/build-apk.yml'`.

**Lưu ý:** `meow-build.yml` đã 404 ở endpoint `contents` nhưng vẫn còn trong `actions/workflows` với `state: active` và còn run cũ. Không nên tin `state`, chỉ tin `path` khớp file cấu hình.

---

## 7. Lỗi phụ: không trả artifact khi thành công

Khi `conclusion == success`, công cụ chỉ trả log, không trả:
- `run_id`
- `html_url` (link xem run)
- danh sách artifact: `name`, `size_in_bytes`, `archive_download_url`, `expires_at`

Người dùng không biết tải APK ở đâu, phải tự mò vào GitHub.

**Đề xuất:** trả kèm link tải trực tiếp, hoặc tự tạo GitHub Release đính APK để có URL tải không cần login.

---

## 8. Lỗi phụ: thông báo lỗi gây hiểu nhầm

Khi fail, công cụ trả log của run sai mà không nêu:
- `run_id`
- `head_branch`
- `head_sha`
- `path` của workflow

→ Người dùng tưởng code Android bị lỗi, thực chất là workflow plugin fail cứng.

**Đề xuất:** header lỗi phải có dạng:
```
Build thất bại
  workflow: .github/workflows/meow-build.yml
  branch:   meow-build
  commit:   5213378
  run_id:   41312806762
```

---

## 9. Các lỗi khác gặp trong quá trình test

| Lỗi | Nguyên nhân | Trạng thái |
|---|---|---|
| `HTTP 401 Requires authentication` cho cả `git_push` và `build_apk` | Token GitHub chưa được nạp vào app | Đã hết sau khi dán token |
| `fetch_url` trả 404 khi xem `contents/.github/workflows/meow-build.yml` | File chỉ tồn tại trên branch `meow-build`, không phải `main` | Công cụ nên tự thử branch khác hoặc báo rõ |
| `fetch_url` 404 *"This repository is empty"* | Repo chưa có commit đầu tiên | OK, thông báo hợp lý |
| `run_command` bị chặn khi `cd` ra ngoài dự án | Cơ chế sandbox | Hợp lý, không phải bug |
| Máy không có `curl`/`wget`, chỉ có `unzip` | Môi trường Android/Termux | Không tải artifact bằng lệnh được, phải qua trình duyệt |

---

## 10. Checklist kiểm thử lại sau khi sửa

- [ ] Gọi `build_apk` trên repo có `android/` → phải chạy `assembleDebug`, ra file `.apk`
- [ ] Không sinh branch `meow-build` mới
- [ ] Không sinh workflow có `exit 1`
- [ ] Kết quả trả về có `run_id`, `html_url`, link tải artifact
- [ ] Trên repo có nhiều workflow → vẫn theo dõi đúng workflow APK
- [ ] Repo không có project Android → báo lỗi rõ ràng, không tạo workflow rác
