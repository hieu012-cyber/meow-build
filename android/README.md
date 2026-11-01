# Utility Hub - Android App

App Android native (Java) gồm 3 tiện ích: Máy tính, Đồng hồ (kèm bấm giờ), Thời tiết (dữ liệu mô phỏng).

## Thông số

- Package: `com.meowcode.utilityhub`
- compileSdk / targetSdk: 34
- minSdk: 24 (Android 7.0+)
- Java 17
- Gradle 8.5, AGP 8.2.2

## Cách 1 — Build tự động qua GitHub Actions (dễ nhất, ra APK luôn)

1. Đẩy toàn bộ dự án lên GitHub (nhánh `main`).
2. Vào tab **Actions** của repo → workflow **Build Android APK** sẽ tự chạy.
3. Chờ ~3-5 phút, vào mục **Artifacts** → tải `utility-hub-debug-apk`.
4. Giải nén được file `utility-hub-debug.apk` → chuyển sang điện thoại → cài (bật "Cài từ nguồn không xác định").

Muốn chạy tay: Actions → chọn workflow → **Run workflow**.

## Cách 2 — Build trên máy có Android Studio

1. Mở Android Studio → **Open** → chọn thư mục `android/`.
2. Chờ Gradle sync xong (Android Studio tự tải SDK/JDK cần thiết).
3. Menu **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. APK nằm ở `android/app/build/outputs/apk/debug/app-debug.apk`.

## Cách 3 — Build bằng dòng lệnh (máy đã có Android SDK + JDK 17)

```bash
cd android
./gradlew assembleDebug
```

Nếu thiếu `gradlew`, chạy `gradle wrapper --gradle-version 8.5` trước.

File APK: `android/app/build/outputs/apk/debug/app-debug.apk`

## Cài lên điện thoại

1. Copy file `.apk` sang điện thoại (USB, Google Drive, Telegram...).
2. Mở file → cho phép cài từ nguồn không xác định nếu được hỏi.
3. Cài xong, mở app **Utility Hub**.

## Cấu trúc mã nguồn

```
android/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/meowcode/utilityhub/
│       │   ├── MainActivity.java          # Activity chính + BottomNav
│       │   ├── CalculatorFragment.java    # Tab Máy tính
│       │   ├── ClockFragment.java         # Tab Đồng hồ + bấm giờ
│       │   └── WeatherFragment.java       # Tab Thời tiết (mô phỏng)
│       └── res/
│           ├── layout/                    # 4 layout XML
│           ├── values/                    # colors, strings, themes
│           ├── drawable/ic_launcher.xml   # Icon vector
│           └── menu/bottom_nav_menu.xml
├── build.gradle
├── settings.gradle
├── gradle.properties
└── gradle/wrapper/gradle-wrapper.properties
```

## Ghi chú

- App **không cần quyền mạng** — thời tiết dùng dữ liệu mô phỏng.
- Muốn dữ liệu thời tiết thật: thêm quyền INTERNET vào manifest, thêm thư viện `OkHttp` hoặc `Retrofit`, gọi API OpenWeatherMap với API key.
