# 04 — System Design Process
## 1. Functional Requirements
### 1.1. Tenant Management
- Tạo và quản lý thông tin công ty (Tenant).
- Mỗi tenant có dữ liệu độc lập.
- Tenant Admin chỉ được quản lý dữ liệu thuộc tenant của mình.
### 1.2. Identity & Authorization
- Quản lý EmployeeInfo, Job và User.
- Đăng nhập và xác thực người dùng.
- Quản lý Role, Permission và UserTenantRole.
- Kiểm tra quyền truy cập theo tenant và permission.
### 1.3. Time Management
- Ghi nhận thời gian làm việc của nhân viên.
- Nhân viên tạo yêu cầu nghỉ phép.
- HR Manager phê duyệt hoặc từ chối nghỉ phép.
- Quản lý LeaveBalance.
- Batch job xác định AbnormalCase và thời gian nghỉ không hợp lệ.
### 1.4. Payroll
- HR Manager quản lý SalaryProfile, IncomePolicy và DeductionPolicy.
- Chạy payroll hàng tháng cho tenant tương ứng với công ty của HR Manager.
- Tính lương dựa trên SalaryProfile, IncomePolicy, DeductionPolicy và dữ liệu AbnormalCase.
- Cho phép retry các payslip thất bại mà không tính lại payslip đã hoàn thành.
- Nhân viên xem payslip của mình.
- Không cho phép tạo duplicate payroll run cho cùng tenant và kỳ lương.
### 1.5. Notification
- Gửi email thông báo khi payslip sẵn sàng.
- Gửi email thông báo khi đơn nghỉ phép đã được duyệt hoặc bị từ chối.
- Retry khi gửi email thất bại.
### 1.6. Audit
- Ghi nhận lịch sử thay đổi salary và payroll liên quan.
- Lưu thông tin người thực hiện, thời gian, giá trị trước và sau thay đổi.
- Cho phép Auditor truy cập audit thuộc tenant được phân quyền.
- Lưu audit tối thiểu 7 năm.
## 2. Non-Functional Requirements (NFR)
| Requirement | Target / Constraint |
|---|---|
| Scalability | 50.000 tenants, 5 triệu employees |
| Enterprise tenant | Hỗ trợ tenant có 100.000 employees |
| Payslip latency | P95 mục tiêu dưới 2 giây |
| Payroll processing | Xử lý bất đồng bộ, không block API |
| Availability | Mục tiêu ban đầu 99.9% cho các API chính |
| Data isolation | Tenant không thể truy cập dữ liệu tenant khác |
| Consistency | Không duplicate payroll run; dữ liệu payslip phải nhất quán |
| Reliability | Không mất event khi DB commit thành công |
| Audit retention | Tối thiểu 7 năm |
| Security | Authentication, authorization, tenant isolation, mã hóa dữ liệu nhạy cảm |
**Assumptions:**
- Mục tiêu latency dưới 2 giây áp dụng cho việc đọc payslip đã được tính xong, không áp dụng cho thời gian tính payroll.
- Payroll không yêu cầu hoàn thành tức thì, có thể xử lý trong một khoảng thời gian batch được quy định.
- Availability và latency cần được xác nhận lại bằng SLA thực tế.
## 3. Capacity Estimation (Rough Estimate)
### 3.1. Input assumptions
| Metric | Assumption |
|--------|------------|
| Total tenants | 50,000 |
| Total employees | 5,000,000 |
| Average employees / tenant | 100 |
| Largest tenant | 100,000 employees |
| Payroll frequency | 1 lần / tháng |
| Payslips / month | 5,000,000 |
| Payslip record size | 5 KB (ước tính) |
### 3.2. Payroll processing throughput
Giả sử cần hoàn thành 5 triệu payslips trong 4 giờ:
$$
Throughput = \frac{5,000,000}{4 \times 3600}
$$
$$
\approx 347 \text{ payslips/second}
$$
Nếu yêu cầu hoàn thành trong 2 giờ:
$$
\approx 694 \text{ payslips/second}
$$
Đây là throughput trung bình cần đạt. Thực tế cần capacity cao hơn để xử lý retry, tenant lớn và tải không đồng đều.
### 3.3. Storage estimation
Giả sử mỗi payslip chiếm khoảng 5 KB:
- Mỗi tháng: 5 triệu × 5 KB ≈ 25 GB.
- Mỗi năm: 25 GB × 12 ≈ 300 GB.
- Nếu giữ payslip 7 năm: khoảng 2.1 TB dữ liệu thô.

Chưa bao gồm index, backup, replica và các bảng liên quan.

Audit storage cần tính riêng vì phụ thuộc số lượng sự kiện thay đổi dữ liệu và kích thước mỗi event.

### 3.4. Payslip read traffic

Giả sử 10% employees truy cập payslip trong 8 giờ đầu sau khi payroll hoàn thành:

$$
\text{Requests} = 5,000,000 \times 10\% = 500,000
$$

$$
\text{Average RPS} = \frac{500,000}{8h \times 3600s} \approx 17.4
$$

Nếu peak traffic gấp 10 lần trung bình thì cần phục vụ khoảng 175 RPS.

Đây chỉ là giả định ban đầu, cần load test để xác định peak thực tế.

## 4. Main Components

| Component | Responsibility |
|---|---|
| API Gateway | Entry point, authentication routing, rate limiting |
| Identity Service | User, EmployeeInfo, Job, Role, Permission |
| Tenant Module | Quản lý thông tin tenant |
| Time Management Service | WorkingTime, LeaveRequest, LeaveBalance, AbnormalCase |
| Payroll Service | SalaryProfile, Policy, PayrollRun, Payslip |
| Payroll Worker | Xử lý payroll bất đồng bộ |
| Notification Service | Tạo và gửi notification |
| Audit Service | Lưu audit events |
| Kafka | Event broker giữa các context |
| Transactional Outbox | Lưu event cùng transaction với dữ liệu nghiệp vụ |
| RelayPublisher | Publish event từ Outbox lên Kafka |
| Redis | Cache dữ liệu đọc thường xuyên nếu cần |
| Database | Persistent storage cho từng context |
| External Email Provider | Gửi email |
**Deployment note:**
- Mỗi Context là một microservice độc lập ngoại trừ Tenant Context sẽ chung service với Identity Service.
- Tenant Context mặc dù triển khai chung với Identity nhưng vẫn giữ ranh giới nghiệp vụ riêng.
## 5. Data Flow
### 5.1. Employee records working time
1. Employee gửi `RecordWorkingTime`.
2. Time Management kiểm tra Employee và tenant.
3. Lưu WorkingTime vào database.
4. Theo lịch, batch job kiểm tra WorkingTime và LeaveRequest.
5. Tạo hoặc cập nhật AbnormalCase.
### 5.2. HR updates salary
1. HR Manager gửi `UpdateSalaryProfile`.
2. Identity kiểm tra authentication, tenant scope và permission.
3. Payroll kiểm tra dữ liệu và effective date.
4. Cập nhật SalaryProfile và insert event vào Outbox trong cùng transaction.
5. RelayPublisher publish `SalaryProfileChanged` lên Kafka.
6. Audit Consumer nhận event và lưu AuditEvent.
### 5.3. Monthly payroll
1. HR Manager gửi `RunPayroll`.
2. Payroll kiểm tra tenant, quyền và kỳ lương.
3. Tạo PayrollRun nếu chưa tồn tại.
5. Payroll Worker lấy danh sách employees cần tính lương.
6. Lấy SalaryProfile, IncomePolicy, DeductionPolicy và dữ liệu AbnormalCase phù hợp kỳ lương.
7. Tính toán và lưu Payslip.
8. Khi Payslip được lưu thành công, insert `PayslipReady` vào Outbox.
9. RelayPublisher gửi event lên Kafka.
10. Notification Consumer tạo Notification và gửi email.
11. Employee truy cập API để đọc payslip đã hoàn thành.
### 5.4. View payslip
1. Employee gửi request xem payslip.
2. Identity xác thực user và permission.
3. Payroll kiểm tra employee thuộc tenant và chỉ trả về payslip được phép truy cập.
4. Truy vấn database hoặc cache.
5. Trả kết quả cho Employee.
### 5.5. Audit flow
1. Các context phát sinh business event.
2. Event được lưu vào Outbox cùng transaction nghiệp vụ.
3. RelayPublisher publish lên Kafka.
4. Audit Consumer kiểm tra `eventId` để tránh duplicate.
5. Lưu AuditEvent vào Audit Database.
## 6. Failure Scenarios & Observability
| Failure scenario | Handling strategy | Observability |
|------------------|-------------------|---------------|
| DB update thành công nhưng Kafka publish thất bại | Transactional Outbox, retry publish | Outbox pending count, oldest pending age |
| Consumer crash khi xử lý event | Kafka redelivery, idempotent consumer | Consumer lag, retry count |
| Duplicate event | Deduplicate bằng eventId | Duplicate event count |
| Payroll Worker crash giữa batch | Resume các payslip chưa hoàn thành | PayrollRun progress, failed count |
| Hai request chạy payroll cùng lúc | Unique constraint và transaction/locking | Duplicate run reject |
| Email provider timeout | Retry có backoff, sau giới hạn đưa vào FAILED/DLQ | Email failure rate, retry count |
| Payslip query chậm | Index, read replica, cache nếu cần | API latency P95/P99 |
| Một tenant truy cập dữ liệu tenant khác | Enforce tenant scope tại service và query layer | Authorization failure, tenant mismatch |
| Kafka unavailable | Giữ event trong Outbox, publish lại khi Kafka phục hồi | Kafka availability, Outbox backlog |