# 06. NFR & Observability
## 1. Scope & Assumptions
Payroll Service chịu trách nhiệm:
- Quản lý SalaryProfile, IncomePolicy, DeductionPolicy.
- Khởi tạo và quản lý PayrollRun theo Company và PayPeriod.
- Tính toán payroll bất đồng bộ.
- Lưu Payslip và công bố event `PayslipReady`.
- Đảm bảo không tạo duplicate PayrollRun và không tính trùng Payslip.
**Assumptions:**
- Payroll được chạy theo tháng.
- Payroll calculation là asynchronous workload.
- Payslip chỉ được xem khi đã hoàn thành và lưu thành công.
- SLO được đo trong production, theo rolling 30-day window.
- Các tỷ lệ được tính trên request hoặc operation hợp lệ, loại trừ lỗi do client gửi request không hợp lệ.
## 2. Service Level Indicators (SLI)
| # | SLI | Cách đo |
|---|-----|---------|
| 1 | Payroll Run Success Rate | Tỷ lệ PayrollRun hoàn thành thành công (runStatus = COMPLETED) trên tổng PayrollRun hợp lệ `COMPLETED_payroll_runs / total_payroll_runs`|
| 2 | Payroll Processing Duration | Thời gian từ lúc PayrollRun bắt đầu đến khi tất cả Payslip được xử lý xong, `PayrollRun.startDateTime - PayrollRun.endDateTime` |
| 3 | Payslip Read Latency | Thời gian xử lý request lấy Payslip, đo thời gian từ lúc API nhận request đến khi trả response |
| 4 | Payslip Availability | Tỷ lệ request lấy Payslip thành công khi Payslip đã tính toán xong, `successful_reads / total_valid_request_reads_payslips` |
| 5 | Payroll Data Correctness | Tỷ lệ Payslip được kiểm tra và không phát hiện sai lệch dữ liệu hoặc tính toán |
## 3. Service Level Objectives (SLO)
| # | SLO | Target | Measurement Window |
|---|-----|--------|--------------------|
| 1 | Payroll Run Success Rate | >= 99.5% | Rolling 30 days |
| 2 | Payroll Processing Duration | 95% PayrollRun hoàn thành trong 4 giờ | Rolling 30 days |
| 3 | Payslip Read Latency | P95 < 2 seconds | Rolling 30 days |
| 4 | Payslip Availability | >= 99.9% | Rolling 30 days |
| 5 | Payroll Data Correctness | 100% Payslip được validate phải thỏa business rules | Mỗi PayrollRun |
**Notes:**
- SLO 4 chỉ áp dụng cho Payslip đã ở trạng thái READY.
- Các target cần được điều chỉnh sau khi có performance test và production baseline.
## 4. Alerting
| # | Alert | Condition | Severity | Action |
|---|---|---|---|---|
| 1 | Payroll Run Failure | PayrollRun chuyển sang FAILED | Critical | Kiểm tra lỗi calculation, DB và dependency |
| 2 | Payroll Processing Slow | PayrollRun chạy quá 4 giờ | Warning | Kiểm tra worker throughput, DB query và Kafka lag |
| 3 | Payslip API Latency High | P95 > 2 seconds trong 5 phút | Warning | Kiểm tra DB, Redis và API latency |
| 4 | Payroll Data Mismatch | Phát hiện sai lệch trong reconciliation | Critical | Điều tra data payslip |
| 5 | Payroll Event Backlog | Outbox pending hoặc Kafka consumer lag tăng cao liên tục | Warning | Kiểm tra RelayPublisher, Kafka và Consumer |
**Alerting principles:**
- Critical: cần xử lý ngay vì có thể ảnh hưởng đến tính đúng đắn hoặc tính toàn vẹn của payroll.
- Warning: cần điều tra trước khi ảnh hưởng đến SLO.
- Alert nên có `companyCd`, `payrollRunId`, `correlationId` nếu có thể, không được chứa thông tin lương nhạy cảm.
## 5. Dashboard Metrics
### 5.1 Business Metrics
| Metric | Description |
|---|---|
| `payroll_runs_total` | Tổng số PayrollRun được tạo |
| `payroll_runs_success_total` | Tổng số PayrollRun thành công |
| `payroll_runs_failed_total` | Tổng số PayrollRun thất bại |
| `payroll_employees_processed_total` | Tổng số employee đã được tính payroll |
| `payslips_ready_total` | Tổng số Payslip đã READY |
| `payslips_failed_total` | Tổng số Payslip tính toán thất bại |
| `payroll_processing_duration_seconds` | Thời gian xử lý một PayrollRun |
| `payroll_amount_total` | Tổng gross/net payroll amount theo kỳ và tenant, chỉ hiển thị với quyền phù hợp |
| `payroll_reconciliation_mismatch_total` | Tổng số Payslip phát hiện sai lệch |
### 5.2 Technical Metrics
| Metric | Description |
|--------|-------------|
| `http_requests_total` | Tổng số HTTP requests |
| `http_request_duration_seconds` | API latency |
| `http_requests_errors_total` | Tổng số HTTP errors |
| `jvm_memory_used_bytes` | JVM heap và non-heap usage |
| `jvm_gc_pause_seconds` | Thời gian JVM GC pause |
| `process_cpu_usage` | CPU usage của Payroll Service |
| `db_connection_pool_active` | Số DB connections đang sử dụng |
| `db_query_duration_seconds` | Database query latency |
| `redis_hit_ratio` | Tỷ lệ cache hit |
| `kafka_consumer_lag` | Kafka consumer lag |
| `outbox_pending_total` | Số lượng Outbox event chưa publish |
| `payroll_worker_active` | Số worker đang xử lý |
| `payroll_worker_queue_depth` | Số payroll tasks đang chờ xử lý |
| `retry_total` | Tổng số lần retry |
| `dlq_messages_total` | Tổng số message trong DLQ |
## 6. Correlation ID Flow
### Purpose
Correlation ID dùng để trace một business operation xuyên suốt nhiều service và asynchronous processing.
Một request khởi tạo PayrollRun có thể đi qua:
- API Gateway
- Payroll Service
- Payroll Worker
- Kafka
- Notification Service
- Audit Service
### Flow
1. Client gửi request tạo PayrollRun.
2. API Gateway nhận hoặc tạo `correlationId`.
3. Payroll Service đưa `correlationId` vào application log và Outbox event.
4. RelayPublisher publish event lên Kafka, giữ nguyên `correlationId`.
5. Payroll Worker consume event và tiếp tục sử dụng cùng `correlationId`.
6. Khi Payroll Worker publish `PayslipReady`, event tiếp tục mang `correlationId`.
7. Notification và Audit Consumer sử dụng cùng `correlationId` để trace toàn bộ flow.
**Note:**
- Khi một consumer retry event, phải giữ nguyên `correlationId`.
- Không tạo correlation ID mới ở mỗi service.
- Với mỗi employee calculation, có thể thêm `payslipId` hoặc `employeeId` vào structured log để debug.
- Không log salary, tax ID, bank account và các thông tin nhạy cảm.
## 8. Observability Checklist
- [ ] Các SLI được định nghĩa và đo lường.
- [ ] Các SLO với target và measurement window.
- [ ] Có alert cho payroll failure, latency, mismatch và event backlog.
- [ ] Có Business Dashboard.
- [ ] Có Technical Dashboard.
- [ ] Có Payroll Worker Dashboard.
- [ ] Correlation ID được truyền xuyên suốt flow.
- [ ] Structured logging có `correlationId`, `companyCd`, `payrollRunId`, `eventId`.
- [ ] Không log sensitive payroll data.
- [ ] Có dashboard theo dõi Outbox backlog, Kafka lag, retry và DLQ.