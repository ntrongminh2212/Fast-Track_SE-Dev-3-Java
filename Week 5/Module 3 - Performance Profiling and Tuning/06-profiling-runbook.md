# 06 — Profiling Runbook: Payroll Dashboard

## Scenario

- Payroll Dashboard p99: **8 giây**.
- CPU ứng dụng: **35%**.
- Heap: **ổn định**.
- Database CPU: **40%**.
- Thread count: **450**.
- **300 thread đang `WAITING`** tại HTTP client call đến Tax Service.

## 1. Giả thuyết nguyên nhân gốc

Giả thuyết ưu tiên là Dashboard bị **I/O-bound**, chờ Tax Service hoặc chờ connection/response từ HTTP client, không phải CPU-bound hay memory-bound. Số lượng lớn thread chờ phù hợp với việc lời gọi downstream chậm, timeout dài, connection pool cạn hoặc request bị dồn hàng.

Đây mới là giả thuyết: cần kiểm tra thread dump, HTTP client metrics và distributed trace để phân biệt các khả năng. CPU và heap ổn định không chứng minh Tax Service là nguyên nhân duy nhất.

## 2. Artifact và metrics cần lấy

| Artifact / metric | Mục đích |
|---|---|
| **JFR recording** trong lúc p99 tăng (profile cấu hình phù hợp, thời lượng có giới hạn) | Xem thread states, socket/HTTP I/O, monitor contention, CPU samples, allocation và GC pause; đối chiếu timestamp với latency cao |
| **3 thread dumps** cách nhau vài giây trong cùng đợt chậm | Xác nhận các thread có tiếp tục chờ tại cùng call stack hay không; xem trạng thái `WAITING`/`TIMED_WAITING`, connection pool và lock contention |
| **Distributed traces** của request dashboard | Đo thời gian từng span; xác định Tax Service có chiếm phần lớn thời gian hay còn dependency khác chậm |
| **Application/HTTP client metrics** | Tax Service latency p50/p95/p99, timeout/error rate, số request in-flight, connection pool active/idle/pending, connection acquisition time, retry count |
| **Service và hạ tầng metrics** | Dashboard request rate/latency, thread pool active/queue/rejection, CPU, GC pause, heap after GC, network/socket; Tax Service latency, saturation, error rate; database pool/query latency |
| **Logs có correlation/trace ID** | Đối chiếu lỗi timeout/retry và request cụ thể giữa Dashboard với Tax Service |

Thu thập trong cửa sổ có vấn đề và so sánh với một cửa sổ bình thường. Khi production profiling, giới hạn thời lượng và mức thu thập, đánh giá overhead, bảo vệ dữ liệu nhạy cảm trong artifact và lưu trữ theo chính sách vận hành.

## 3. Vì sao chưa ưu tiên heap dump?

Heap đang ổn định và triệu chứng chính là nhiều thread chờ HTTP I/O trong khi CPU không cao. Heap dump thường hữu ích khi điều tra nghi ngờ leak, retained objects hoặc memory pressure; nó không cho biết trực tiếp vì sao Tax Service call chậm. Tạo heap dump có thể tốn thời gian, dung lượng và gây pause/overhead, nên trước tiên lấy thread dump, JFR, traces và metrics. Xem xét heap dump nếu có thêm bằng chứng memory tăng, GC bất thường hoặc nghi ngờ giữ object/queue quá lớn.

## 4. Immediate mitigation

1. Kiểm tra trạng thái Tax Service và dashboard dependency metrics; thông báo/điều phối với đội sở hữu service.
2. Đặt hoặc điều chỉnh **timeout hữu hạn** cho connect, connection acquisition và response, phù hợp với SLA và deadline tổng của Dashboard.
3. Giới hạn concurrency/bulkhead cho Tax calls để tránh chiếm hết thread và connection; trả partial response/fallback chỉ khi nghiệp vụ cho phép và phải thể hiện dữ liệu thiếu rõ ràng.
4. Tạm giảm traffic hoặc áp dụng rate limit/load shedding nếu Tax Service đang quá tải.
5. Tắt hoặc giới hạn retry khuếch đại tải; chỉ retry lỗi tạm thời, có backoff, jitter, giới hạn số lần và nằm trong deadline tổng.
6. Nếu có thể, scale Dashboard để xử lý tải mà không làm Tax Service quá tải thêm; không tăng thread vô hạn như giải pháp mặc định.

## 5. Long-term fix

- Dùng client và execution model phù hợp: non-blocking HTTP client với WebFlux khi toàn bộ pipeline có thể non-blocking; nếu giữ MVC/blocking, giới hạn thread và cô lập dependency bằng pool/bulkhead phù hợp.
- Thiết kế timeout theo ngân sách latency end-to-end; dùng circuit breaker, bulkhead, rate limit và fallback theo yêu cầu nghiệp vụ.
- Tối ưu Tax Service và giao tiếp liên dịch vụ; cân nhắc cache dữ liệu Tax chỉ khi độ mới dữ liệu và quy tắc tính lương cho phép.
- Giảm số lần gọi bằng batching hoặc hợp nhất API nếu phù hợp; chạy song song các call độc lập nhưng giới hạn concurrency.
- Đặt SLO và alert cho p99, downstream latency, timeout/error, pending connection acquisition, thread pool saturation và queue depth.
- Duy trì tracing xuyên service và runbook để xác nhận nguyên nhân bằng evidence trước khi thay đổi cấu hình.

## Kết luận

Dữ kiện hiện tại nghiêng về **I/O-bound do chờ Tax Service hoặc HTTP client resource**, nhưng cần xác nhận bằng traces, thread dumps, JFR và metrics. Ưu tiên xử lý timeout/concurrency và bảo vệ hệ thống; chưa lấy heap dump trừ khi xuất hiện bằng chứng về memory pressure hoặc leak.
