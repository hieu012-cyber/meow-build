# Quy tắc ProGuard cho app Cờ Caro Meow
# App đơn giản, không dùng reflection nên không cần giữ class riêng.
# Giữ nguyên View tùy chỉnh (an toàn cho GomokuView khi R8 chạy)
-keep class com.meowcode.cocaro.** { *; }
