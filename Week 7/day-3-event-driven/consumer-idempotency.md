# Idempotent Consumer

Message broker có thể giao lại event, vì vậy consumer phải xử lý an toàn khi nhận trùng.

## Mẫu xử lý

Trong **cùng transaction DB** với tác động nghiệp vụ, ghi khóa `(consumer_name, event_id)` vào bảng `processed_events` có unique constraint. Nếu khóa đã tồn tại thì bỏ qua event; nếu chưa, xử lý rồi ghi khóa. Chỉ ack message sau khi transaction commit.

```text
BEGIN
  INSERT processed_events(consumer_name, event_id)  -- unique key
  apply consumer effect
COMMIT
ACK
```

Nếu insert đụng unique key, consumer đã xử lý event đó và có thể ack mà không lặp tác động.

Notification cần chú ý: gửi email là tác động ngoài DB. Dùng provider có idempotency key (ví dụ `eventId`) hoặc lưu trạng thái gửi và thiết kế retry có đối soát; chỉ dùng bảng processed không thể tạo transaction nguyên tử với email provider.

Audit và analytics cũng nên có khóa duy nhất theo `eventId` để ngăn record/update trùng.
