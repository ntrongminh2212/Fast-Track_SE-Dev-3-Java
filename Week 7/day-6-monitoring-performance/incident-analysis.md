# Phân tích diễn tập sự cố: Inventory chậm

## Tình huống

Inventory chậm 3 giây ở 20% request; checkout p95 tăng từ 300 ms lên 1,8 giây, p99 từ 700 ms lên 6 giây; retry tăng và circuit breaker chuyển Half-open/Open. `GET /api/orders` cần giữ p95 dưới 300 ms.

## Phản ứng

1. Cảnh báo checkout success giảm hoặc p95/p99 vượt SLO nên kích hoạt trước; cảnh báo Inventory latency/breaker giúp khoanh vùng.
2. Mở dashboard nghiệp vụ, sau đó dashboard API/dependency và resilience.
3. Chọn request chậm, theo `X-Correlation-Id` từ Gateway đến Order rồi Inventory. Trong project hiện tại đây là correlation ID; trace phân tán đầy đủ cần bổ sung exporter/tracer.
4. So sánh retry count với timeout và error rate. Retry lặp trên request đã chậm có thể khuếch đại tải Inventory; xác nhận retry chỉ áp dụng cho lỗi transient và có giới hạn/backoff.
5. Nếu `GET` cũng chậm, kiểm tra xem endpoint này có dùng chung executor/queue/thread hoặc gọi Inventory không. Bulkhead cần cô lập riêng luồng checkout→Inventory; GET order không nên chờ luồng reserve stock.

## Giảm tác động

- Giữ timeout ngắn, retry giới hạn và backoff; không retry lỗi validation/business.
- Để circuit breaker fail fast khi Inventory lỗi; giới hạn concurrency bằng bulkhead.
- Giảm/tạm dừng checkout phụ thuộc Inventory nếu cần; không làm ảnh hưởng chức năng đọc order không phụ thuộc.
- Theo dõi success rate, p95/p99, timeout, breaker và bulkhead rejection sau thay đổi.

## Khắc phục dài hạn

- Tìm nguyên nhân Inventory chậm bằng trace/log/metrics của dependency và kiểm thử tải.
- Rà soát giới hạn thread/queue, connection pool, timeout tổng và ngân sách latency.
- Bổ sung tracing phân tán và alert có runbook; diễn tập lại để đảm bảo GET order được cách ly.

## Kết luận cần ghi nhận

Ghi mốc thời gian, số checkout bị ảnh hưởng, retry/rejection, trạng thái breaker, thay đổi mitigation, người phụ trách và việc cần làm tiếp theo. Không kết luận nguyên nhân chỉ từ CPU/memory.
