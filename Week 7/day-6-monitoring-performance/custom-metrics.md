# Thiết kế metric nghiệp vụ tùy chỉnh

Metric dưới đây là đề xuất; chưa được cài đặt trong mã nguồn hiện tại. Dùng Micrometer Counter cho sự kiện cộng dồn và Gauge cho trạng thái hiện thời.

| Tên metric | Loại | Khi ghi nhận | Nhãn an toàn gợi ý |
|---|---|---|---|
| `checkout.success` | Counter | Checkout hoàn tất thành công | `channel`, `result` |
| `checkout.failed` | Counter | Checkout thất bại | `reason` với tập giá trị hữu hạn |
| `payment.status.unknown` | Counter | Không rõ kết quả payment sau timeout | `provider`, `reason` |
| `order.stuck.processing` | Gauge | Số order đang quá thời hạn xử lý | `state` |

## Quy tắc instrument

- Đặt tên rõ ràng, dùng đơn vị và quy ước nhất quán; Micrometer/Prometheus có thể chuẩn hóa tên khi export.
- Counter chỉ tăng; dashboard dùng tốc độ tăng trong một khoảng thời gian, không chỉ xem tổng từ lúc khởi động.
- Gauge đo số lượng đang tồn tại; cập nhật từ nguồn dữ liệu có giới hạn và tránh truy vấn nặng liên tục.
- Không gắn `orderId`, `userId`, email, token hay correlation ID làm tag. Dùng log/trace để tìm từng request.
- Chỉ ghi `checkout.success` sau khi flow nghiệp vụ đạt điều kiện thành công đã định nghĩa; tránh đếm lại khi retry nội bộ.
- `payment.status.unknown` cần ưu tiên đối soát, không đồng nghĩa với payment thất bại và không được tự động charge lại.
