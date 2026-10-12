### **Assignment**

Tạo `06-chaos-engineering-basics.md`.

cần thiết kế một experiment cho scenario:

`Order Service gọi Inventory Service để reserve stock.`

#### 1. Steady state
Chạy Order Service, Inventory Service trong môi trường dev ổn định, chạy JMetter test 50 user parallel tạo search sản phẩm, tạo order, xem order trong vòng 30 phút và yêu cầu đạt những chỉ số dưới:

| Chỉ số | Steady state mong muốn |
|---|---|
| Tỷ lệ request reserve stock thành công | ≥ 99% |
| P95 | < 500 ms |
| Error Rate | < 1% |
| Connection pool | 70/100 |
| CPU App/ CPU DB/ Memory  | Không vượt quá 90% |

#### 2. Hypothesis

**Giả thuyết:** Nếu Inventory Service có latency tăng thêm 2 giây, Order Service vẫn limit số lần retry và timeout theo cấu hình mà không retry liên tục tới Inventory Service.

Kết quả nghiệp vụ được kỳ vọng:

- Chỉ xác nhận đơn hàng khi có dữ liệu tồn kho sản phẩm trả về từ Inventory Service.
- Nếu trong 10 giây không thể xác đinh dữ liệu tồn kho vì bất kỳ lý do gì mà Inventory Service không response thì Order Service sẽ trả lỗi về cho client và giữ Order ở trạng thái PENDING.
- Khi gửi request trừ số tồn kho sản phẩm để confirm order mà bị lỗi thì có thể retry 3 lần, nhưng mỗi lần cần idempotency key để tránh bị trừ nhiều lần, có thể dùng redis để lưu idempotency key

#### 3. Fault injection

1. Tăng latency xử lý request của Inventory Service thêm 2 giây trong vòng 60 giây.
2. Giữ nguyên traffic chạy JMetter test 50 user parallel, quan sát metrics, logs, traces.
3. Sau khi hết thời gian thì tự động gỡ Fault injection
4. Sau khi application về trạng thái ổn định, kiểm tra các đơn hàng phải có trạng thái đúng không, tồn kho các sản phẩm phải trừ đúng số lượng

#### 4. Blast radius

- **Môi trường:** Development, dùng thêm JMeter để giả lập môi trường nhiều người dùng
- **Phạm vi:** Chỉ traffic từ Order Service tới Inventory Service.
- **Dữ liệu:** Tạo data test order và product riêng để test.
- **Giới hạn thời gian:** Tối đa 60 giây fault injection.

#### 5. Metrics và quan sát
- Latency: Thời gian xử lý request <500ms.
- Response time
- Error rate: < 1%
- Concurrent users: 50 user
- Percentile: cần đạt p95 < 500 ms
- Resource utilization: Connection pool < 70/100, CPU App/ CPU DB/ Memory Không vượt quá 90%

#### 6. Abort condition

Dừng fault injection ngay nếu xảy ra bất kỳ tình huống nào dưới đây:

- Error rate vượt quá 5% trong vòng 1 phút.
- P95 latency vượt quá 3000ms trong vòng 1 phút.
- Connection pool pending tăng liên tục đến 90/100 và không có dấu hiệu giảm sau đó.
- Tồn kho trừ không đúng hoặc bị âm.
- CPU App/ CPU DB/ Memory vượt 90% và không có dấu hiệu giảm sau đó.

#### 8. Rollback procedure
1. Dừng fault injection.
2. Lưu lại tất cả các metric, report.
3. Kiểm tra data tồn kho và data đơn đặt hàng có trừ khớp không.
4. Kiểm tra CPU App/ CPU DB/ Memory đã phục hồi về trạng thái bình thường.

#### 9. Expected result

- Order Service request không bị treo liên tục khi Inventory Service phản hồi chậm.
- Retry nhiều nhất 3 lần và time out 10 giây và Inventory Service không bị tăng tải nhiều hơn 90%.
- Connection pool và tài nguyên CPU, memory không bị cạn kiệt.
- Tồn kho trừ đúng.