# Kế hoạch kiểm thử hợp đồng trong CI

## Khi tạo yêu cầu gộp mã

- Biên dịch và chạy kiểm thử đơn vị.
- Chạy phân tích mã tĩnh.
- Xác minh dịch vụ Product theo hợp đồng đã công bố cho bên sử dụng.
- Xác minh dịch vụ Order với bản giả lập dịch vụ Product được tạo từ cùng hợp đồng.
- Kiểm tra hợp đồng thông điệp `OrderConfirmed` và cách dịch vụ nhận sự kiện xử lý.
- Không cho phép yêu cầu gộp mã được thông qua nếu thiếu `available`, đổi tên trường này hoặc thay đổi sai kiểu dữ liệu.

## Khi gộp vào nhánh chính (`main`)

- Chạy kiểm thử tích hợp/kiểm thử thành phần với gói hợp đồng đã xác minh.
- Tạo ảnh container và chạy kiểm thử nhanh trên container.
- Phát hành gói hợp đồng/bản giả lập có phiên bản cùng bản dựng.

## Môi trường tiền triển khai (staging)

- Chạy các luồng đầu-cuối quan trọng và kiểm thử nhanh về khả năng chống lỗi trên các dịch vụ đã triển khai.
- Xác nhận các dịch vụ nhận dữ liệu xử lý được sự kiện và các lỗi API dự kiến.

## Môi trường chính thức (production)

- Chạy kiểm tra tình trạng hoạt động và kiểm tra tự động mô phỏng người dùng sau khi triển khai.
- Theo dõi tỷ lệ lỗi, độ trễ xử lý của bên nhận và lỗi liên quan đến hợp đồng; triển khai thử nghiệm cho một phần lưu lượng (canary) hoặc quay lui (rollback) khi thay đổi có rủi ro.

## Quy tắc chặn triển khai

Không được triển khai dịch vụ nếu kiểm thử xác minh phía cung cấp thất bại hoặc bên sử dụng không vượt qua kiểm thử với bản giả lập đã công bố. Chỉ kiểm thử đơn vị và kiểm tra tình trạng hoạt động không đủ để chứng minh các dịch vụ tương thích với nhau.
