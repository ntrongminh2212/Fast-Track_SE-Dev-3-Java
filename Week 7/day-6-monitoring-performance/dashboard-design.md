# Thiết kế dashboard

## 1. Sức khỏe nghiệp vụ

- Tỷ lệ checkout thành công/thất bại theo phút.
- Số payment thành công nhưng order chưa xác nhận.
- Tỷ lệ Inventory reservation thất bại.
- Số order ở `PAYMENT_PROCESSING` quá thời hạn.
- Số payment trùng đã được ngăn chặn (khi có metric).

## 2. Gateway và Order Service

- Request/giây theo route; 2xx/4xx/5xx.
- p50/p95/p99 theo route và method; request đang xử lý.
- Retry, timeout, fallback, trạng thái circuit breaker.
- Bulkhead concurrency, queue và số request bị từ chối.
- JVM heap, GC pause, CPU, số thread và restart.

## 3. Xử lý sự kiện

Sau khi tích hợp Kafka/Outbox: consumer lag theo nhóm, throughput, lỗi/retry, DLQ count, outbox pending/tuổi event, thời gian xử lý và duplicate event.

## 4. Dependency

- Inventory và Payment: latency p95/p99, lỗi/timeout, lưu lượng.
- Database (khi tích hợp): connection active/pending, query latency, lock wait.
- Kafka (khi tích hợp): broker health và consumer lag.

## Cách sử dụng

Đặt chung khoảng thời gian và environment; đánh dấu thời điểm deploy. Dashboard nên hiển thị cả baseline lẫn SLO, có liên kết từ biểu đồ lỗi tới log/trace theo `traceId` hoặc `X-Correlation-Id`. Không tạo metric tag có độ đa dạng cao như `orderId` hay email.

**Phạm vi hiện tại:** project có HTTP/JVM/Actuator/Prometheus và circuit breaker. Business metrics, DB/Kafka/Outbox metrics cần bổ sung khi luồng tương ứng được triển khai.
