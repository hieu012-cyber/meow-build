# Checkpoint: chuẩn hóa cấu trúc Gradle cho build APK

Đã tái cấu trúc dự án Cờ Caro để build APK được:
- Đổi tên module: src/main → src/app/src/main (workflow build cd vào src/ và chạy ./gradlew).
- Tạo src/settings.gradle, src/build.gradle (AGP 8.5.2), src/app/build.gradle (namespace com.meowcode.cocaro, minSdk 21, compileSdk 34, dependency appcompat 1.7.0), src/gradle.properties (AndroidX).
- Ghi đè AndroidManifest.xml: bỏ package= (AGP 8 cấm), đổi theme sang @style/Theme.AppCompat.Light.NoActionBar (AppCompatActivity yêu cầu theme AppCompat).
- MainActivity.java + GomokuView.java: src/app/src/main/java/com/meowcode/cocaro/.
- Còn thiếu: Gradle wrapper (gradlew) trong src/ để workflow chạy được — cần tạo gradle/wrapper/gradle-wrapper.properties + gradlew script, hoặc đổi cách workflow gọi build. Cần đọc workflow phần bị lược để xác minh.