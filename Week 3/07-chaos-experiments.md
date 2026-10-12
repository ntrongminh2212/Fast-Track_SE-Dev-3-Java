# **Assignment**

Thiết kế **2 chaos experiments** cho Order Service:

## **Experiment A — Latency Injection**

`Inject 3-second latency vào Inventory Service.`

## **Experiment B — Payment Service Returns 500**

`Payment Service trả HTTP 500 trong 20% request trong 5 phút.`

Mentee cần mô tả:
### 1. Experiment A — Latency Injection

| Nội dung | Thiết kế |
|---|---|
| Hypothesis | Khi Inventory Service bị thêm 3 giây latency,  Order Service vẫn limit số lần retry và timeout theo cấu hình mà không retry liên tục tới Inventory Service. |
| Fault | Thêm 3.000 ms latency khi Order Service request tới Inventory Service|
| Blast radius | Dùng data product giả trên môi trường dev cô lập|
| Expected behavior | Tồn kho trừ đúng, tài nguyên CPU memory connection pool không bị cạn kiệt, request không bị treo |
| Timeout / retry / circuit breaker | Timeout 10 giây, retry 3 lần, circuit breaker bật khi cpu memory vượt quá 90%|
| Abort condition | Error rate vượt quá 5% trong vòng 1 phút.<br> P95 latency vượt quá 3000ms trong vòng 1 phút.<br> Connection pool pending tăng liên tục đến 90/100 và không có dấu hiệu giảm sau đó.<br>Tồn kho trừ không đúng hoặc bị âm.<br>CPU App/ CPU DB/ Memory vượt 90% và không có dấu hiệu giảm sau đó.|
| Metrics | Latency: Thời gian xử lý request <500ms. <br>Response time<br>Error rate: < 1% <br>Concurrent users: 50 user <br>Percentile: cần đạt p95 < 500 ms<br>Resource utilization: Connection pool < 70/100, CPU App/ CPU DB/ Memory Không vượt quá 90% |
| Rollback | Dừng fault injection.<br>Lưu lại tất cả các metric, report.<br>Kiểm tra data tồn kho và data đơn đặt hàng có trừ khớp không.<br>Kiểm tra CPU App/ CPU DB/ Memory đã phục hồi về trạng thái bình thường.|

### 2. Experiment B — Payment Service Returns HTTP 500

| Nội dung | Thiết kế |
|---|---|
| Hypothesis | Khi 20% request từ Order Service tới Payment Service nhận HTTP 500 trong 5 phút, Order Service chỉ thực hiện retry nhiều nhất 3 lần và timeout 10 giây, không có trường hợp bị thanh toán nhiều lần|
| Fault | Mock server trả HTTP 500 cho 20% request tới Payment Service trong 5 phút. |
| Blast radius | Chạy Order Service trên dev cô lập, mock server Payment Service, dùng thẻ thanh toán giả |
| Expected behavior | Khi lỗi xảy ra sau 3 lần retry thì trả lỗi về cho client theo tài liệu API, không charge tiền 2 lần, Order bị lỗi sẽ có status FAILED/PENDING không phải PAID|
| Timeout / retry / circuit breaker |  Timeout 10 giây, retry 3 lần, circuit breaker bật khi cpu memory vượt quá 90% |
| Abort condition | Dừng ngay nếu phát hiện recharge 2 lần hoặc Order bị update sai status<br> P95 latency vượt quá 3000ms trong vòng 1 phút.<br> Connection pool pending tăng liên tục đến 90/100 và không có dấu hiệu giảm sau đó.<br>CPU App/ CPU DB/ Memory vượt 90% và không có dấu hiệu giảm sau đó. |
| Metrics | Latency: Thời gian xử lý request <500ms. <br>Response time<br>Error rate<br>Percentile: cần đạt p95 < 500 ms<br>Resource utilization: Connection pool < 70/100, CPU App/ CPU DB/ Memory Không vượt quá 90% |
| Rollback | Dừng fault injection.<br>Lưu lại tất cả các metric, report.<br>Kiểm tra status của Order, nếu status PAID xem log đảm bảo Order charge một lần duy nhất, nếu status PENDING/FAILED đảm xem log đảm bảo Order không bị charge <br>Kiểm tra CPU App/ CPU DB/ Memory đã phục hồi về trạng thái bình thường. |