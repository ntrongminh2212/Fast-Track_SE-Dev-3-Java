# DLQ Runbook — OrderConfirmed

DLQ là hàng đợi cần được giám sát và xử lý, không phải nơi bỏ qua message.

## Khi message vào DLQ

1. Kiểm tra `eventId`, `eventType`, `aggregateId`, payload và lỗi.
2. Ghi nhận số lần retry, thời điểm lỗi đầu/cuối và consumer group.
3. Xác định owner (Notification, Audit hoặc Analytics) và phân loại lỗi: tạm thời hay dữ liệu/schema.
4. Sửa dependency hoặc dữ liệu/schema theo quy trình; không chỉnh message gốc tùy tiện.

## Replay có kiểm soát

1. Xác nhận nguyên nhân đã khắc phục và consumer idempotency đang bật.
2. Thử replay một message ở staging hoặc với một message được chọn.
3. Replay theo lô nhỏ, giới hạn tốc độ; giữ nguyên `eventId` để ngăn tác động lặp.
4. Theo dõi success/error, retry và DLQ depth. Dừng replay nếu lỗi tăng.
5. Lưu audit về người replay, khoảng thời gian, message count và kết quả.

Đặt retention, cảnh báo DLQ depth/tuổi message và người chịu trách nhiệm. Cấu hình retry hữu hạn với backoff; khi hết retry chuyển message cùng metadata lỗi vào DLQ.
