# Checklist sẵn sàng production

## Image và cấu hình

- [ ] Docker image dùng multi-stage build và runtime tối thiểu cần thiết.
- [ ] Tiến trình chạy non-root; build context loại `target`, `.git` và file không cần thiết.
- [ ] Cấu hình theo environment; không commit password, API key hoặc private key.
- [ ] Giới hạn/quan sát JVM memory phù hợp giới hạn container.

## Sức khỏe và vận hành

- [ ] Health, liveness và readiness endpoint hoạt động; không tiết lộ health detail nhạy cảm.
- [ ] Readiness được dùng để quyết định nhận traffic; liveness không phụ thuộc dịch vụ ngoài.
- [ ] Graceful shutdown được kiểm tra khi container dừng.
- [ ] Timeout, retry giới hạn, backoff, circuit breaker và bulkhead đã được diễn tập.

## Quan sát và ứng phó

- [ ] Có dashboard checkout success rate, lỗi và p95/p99.
- [ ] Có log dùng correlation ID và hướng dẫn truy vết.
- [ ] Có cảnh báo cho lỗi dependency, circuit breaker, tài nguyên cạn và backlog sự kiện nếu có.
- [ ] Có runbook, người chịu trách nhiệm, quy trình rollback và đối soát thanh toán.
- [ ] Đã chạy diễn tập và lưu lại kết quả/action items.

Kafka/Outbox chưa được triển khai trong project hiện tại; các mục liên quan là tiêu chí thiết kế/vận hành khi tích hợp sau này.
