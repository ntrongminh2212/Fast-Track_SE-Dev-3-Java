# Quy tắc cảnh báo và hướng dẫn xử lý

| Cảnh báo | Điều kiện ví dụ | Mức độ | Bước xử lý đầu tiên |
|---|---|---|---|
| Checkout giảm độ tin cậy | Tỷ lệ thành công < 99% trong 5 phút | Nghiêm trọng | Xem dashboard nghiệp vụ, 5xx và thay đổi triển khai; xác định phạm vi ảnh hưởng. |
| Checkout chậm | p95 vượt SLO liên tục 10 phút | Cảnh báo/Nghiêm trọng | Xem p99, trace và độ trễ Inventory/Payment; kiểm tra retry, thread/bulkhead. |
| Inventory breaker mở | Mở liên tục > 2 phút hoặc reservation lỗi tăng | Cảnh báo | Kiểm tra Inventory latency/timeout và request bị từ chối; giữ retry có giới hạn. |
| Payment/order không khớp | Có payment thành công nhưng order chưa xác nhận | Nghiêm trọng | Ngăn charge lại tự động; đối soát bằng idempotency key và trạng thái provider. |
| Tài nguyên cạn | Thread/bulkhead/DB pending tăng liên tục | Cảnh báo | Xác định dependency hoặc truy vấn gây chờ; giới hạn tải và xử lý theo runbook. |
| Kafka lag/Outbox/DLQ | Lag tăng kéo dài, event critical vào DLQ | Cảnh báo/Nghiêm trọng | Chỉ áp dụng khi đã tích hợp; kiểm tra consumer, publisher và xử lý DLQ có kiểm soát. |

## Quy trình chung

1. Xác nhận điều kiện còn xảy ra và có ảnh hưởng người dùng/business không.
2. Xem dashboard liên quan, so với baseline và kiểm tra deploy gần nhất.
3. Chọn request lỗi, lần theo `traceId`/`X-Correlation-Id` qua log và trace nếu có.
4. Mitigate theo bằng chứng: giảm traffic, tắt feature flag, giới hạn retry, mở circuit hoặc rollback canary.
5. Theo dõi sau mitigation, thông báo owner và ghi lại timeline/nguyên nhân/hành động tiếp theo.

Không page chỉ vì một lần CPU cao, một mã 404 hoặc một log ERROR. Alert cần có người chịu trách nhiệm, ngưỡng đủ ổn định và liên kết hướng dẫn xử lý.

**Lưu ý:** Kafka, Outbox, DB và payment mismatch chưa có metric trong project mẫu hiện tại; các cảnh báo tương ứng chỉ dùng sau khi tích hợp instrumentation.
