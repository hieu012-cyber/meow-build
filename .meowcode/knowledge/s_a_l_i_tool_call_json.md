# Sửa lỗi tool call JSON

Nguyên nhân: code chứa chuỗi nhiều dòng với \n escape lồng nhau gây lỗi parse JSON của khối tool_call. Giải pháp: (1) Viết code JS 1 dòng ngắn không escape phức tạp, hoặc (2) write_file script .js rồi gọi run_node với tham số {"file":"check.js"}. Đã kiểm chứng run_node file hoạt động 100%.