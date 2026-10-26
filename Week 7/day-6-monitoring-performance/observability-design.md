# Thiết kế khả năng quan sát — Order Service

## 1. Nhật ký (Logs)

- Ghi có cấu trúc: `timestamp`, `level`, `service`, `environment`, `traceId`, `correlationId`, `orderId` (khi cần), `event`, `message`.
- Không ghi mật khẩu, token/JWT, số thẻ, CVV, thông tin tài khoản ngân hàng hoặc payload nhạy cảm; che/mask dữ liệu cần thiết.
- Gateway hiện tạo hoặc giữ `X-Correlation-Id`, chuyển tiếp xuống service và ghi method/path/status. Dùng ID này để tìm log liên quan; không xem nó thay cho trace phân tán.

## 2. Chỉ số (Metrics)

- **HTTP:** request rate, 4xx/5xx, p50/p95/p99, request đang xử lý; phân nhóm bằng route/method/status có giới hạn.
- **JVM:** heap, GC pause, CPU, thread count/state, uptime và số lần restart.
- **Database:** chưa tích hợp trong project; khi thêm, theo dõi active/pending connection, query latency, lock wait và lỗi.
- **Kafka/Outbox/DLQ:** chưa tích hợp trong project; khi thêm, theo dõi consumer lag/throughput, retry, DLQ, số outbox chờ và tuổi event lâu nhất.
- **Circuit breaker/bulkhead:** trạng thái breaker, số call thành công/thất bại/chậm, retry, timeout và bulkhead rejection.
- **Business:** checkout thành công/thất bại, reservation lỗi, payment thành công nhưng order chưa xác nhận. Các chỉ số này cần instrument trong luồng nghiệp vụ; Actuator không tự tạo tất cả.

Order Service hiện expose `/actuator/metrics`, `/actuator/prometheus`, histogram HTTP và tag `application`. Không dùng `userId`, `orderId`, email hoặc correlation ID làm metric tag vì số lượng chuỗi thời gian sẽ tăng không giới hạn.

## 3. Truy vết (Traces)

- Hiện tại Gateway truyền `X-Correlation-Id`; chưa có cấu hình tracing phân tán, exporter hoặc UI trace trong project.
- Khi bổ sung Micrometer Tracing/OpenTelemetry, truyền context theo chuẩn W3C Trace Context qua Gateway → Order → Inventory → Payment và qua message header khi có Kafka.
- Bắt đầu với sampling thấp/định hướng theo lỗi ở production; tăng sampling khi điều tra có kiểm soát.
- Span cần quan sát: request vào Gateway, xử lý checkout, gọi Inventory/Payment, truy vấn DB và publish/consume event.

## 4. Dashboard

- **Business:** tỷ lệ checkout, payment/order mismatch, reservation lỗi, order bị kẹt.
- **API:** rate, 4xx/5xx, p95/p99, route, circuit breaker và retry.
- **Event processing:** Kafka lag, DLQ, outbox backlog (sau khi tích hợp).
- **Dependency:** Inventory/Payment latency và lỗi, DB pool/latency, JVM và thread pool.

## 5. Cảnh báo

- Điều kiện dựa trên tỷ lệ/thời lượng và ảnh hưởng người dùng, không chỉ dựa vào một mẫu CPU/log.
- Mỗi cảnh báo có mức độ, owner, tín hiệu liên quan và liên kết runbook.
- Ví dụ: checkout success < 99% trong 5 phút; p95 checkout vượt SLO; Inventory timeout tăng cùng circuit breaker mở; payment/order mismatch xuất hiện.
- Runbook bước đầu: xác nhận ảnh hưởng, xem dashboard API/business, tìm trace/log bằng correlation ID, giảm lưu lượng hoặc tắt luồng rủi ro theo bằng chứng.

## 6. Rủi ro hiệu năng

1. **Inventory chậm hoặc retry dồn dập:** cần trace dependency, timeout/retry count, breaker; mitigation là giới hạn retry, backoff, circuit breaker và bulkhead.
2. **Cạn tài nguyên HTTP/thread:** cần in-flight, thread state, bulkhead queue/rejection; cô lập luồng gọi dependency và giới hạn concurrency.
3. **DB/Kafka backlog khi tích hợp:** cần pool/query hoặc lag/outbox age/DLQ; tối ưu truy vấn/consumer và xử lý theo lô có kiểm soát.

Đo bằng metrics → drill-down bằng trace → xác minh bằng log/profiler trước khi thay đổi; sau đó kiểm tra bằng tải thử và theo dõi lại.
