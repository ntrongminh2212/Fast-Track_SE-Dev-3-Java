# Phân tích thay đổi gây mất tương thích

| Thay đổi | Gây mất tương thích? | Lý do / cách xử lý |
|---|---|---|
| Thêm trường phản hồi tùy chọn | Thường không | Dịch vụ sử dụng hiện tại có thể bỏ qua; cần kiểm tra khả năng bỏ qua trường lạ khi giải mã dữ liệu. |
| Xóa trường bắt buộc `available` | Có | Order Service phụ thuộc vào trường này; kiểm thử xác minh hợp đồng của dịch vụ cung cấp phải thất bại. |
| Đổi tên `name` thành `productName` | Có | Dịch vụ sử dụng đang chờ `name`; cần giai đoạn hỗ trợ đồng thời hai trường hoặc tạo phiên bản API mới. |
| Đổi `price` từ số thành chuỗi | Có | Kiểu dữ liệu JSON thay đổi và việc phân tích có thể thất bại. |
| Thay đổi mã trạng thái/nội dung phản hồi lỗi | Có thể | Dịch vụ sử dụng có thể xử lý dựa trên mã trạng thái hoặc trường dữ liệu; cần xem phản hồi lỗi đã công bố là một phần hợp đồng. |
| Thêm trường tùy chọn vào payload `OrderConfirmed` | Thường không | Đảm bảo dịch vụ nhận sự kiện bỏ qua các trường chưa biết. |
| Xóa/đổi tên trường sự kiện hoặc đổi kiểu dữ liệu | Có | Các dịch vụ nhận sự kiện hiện tại hoặc quá trình phát lại có thể lỗi; cần tạo phiên bản và chuyển đổi có kế hoạch. |

Trước khi triển khai: xác định các dịch vụ nhận dữ liệu, cập nhật hợp đồng, xác minh dịch vụ cung cấp theo hợp đồng của bên nhận, phát hành gói hợp đồng/bản giả lập tương thích và phối hợp chuyển đổi có phiên bản nếu thay đổi gây mất tương thích.
