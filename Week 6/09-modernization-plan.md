# 09. Modernization Plan
## 1. Scope & Migration Strategy
### 1.1. Scope
Hệ thống đã xác định kiến trúc Microservices với các Bounded Context:
| **Service** | **Responsibility** |
|-------------|--------------------|
| Identity + Tenant | User, EmployeeInfo, Job, Role, Permission, Tenant |
| Time Management | WorkingTime, LeaveRequest, LeaveBalance, AbnormalCase |
| Payroll | SalaryProfile, Policy, PayrollRun, Payslip, BankAccount |
| Audit | AuditEvent |
| Notification | Notification, NotificationTemplate |

Tenant vẫn giữ ranh giới nghiệp vụ riêng nhưng được deploy chung với Identity Service.

**Capability extraction priority:** Không áp dụng vì các Capability đã được phân chia thành Microservices trong kiến trúc mục tiêu.
**Migration priority:** Vẫn cần xác định thứ tự chuyển đổi, routing và cutover từng service để giảm rủi ro khi thay thế hệ thống cũ.

### 1.2. Migration Approach
Sử dụng **Strangler Fig Pattern** để thay thế hệ thống cũ theo từng giai đoạn:
- Không thay thế toàn bộ hệ thống trong một lần.
- Chuyển từng nhóm API/business flow sang Microservice tương ứng.
- API Gateway hoặc routing layer điều hướng request đến hệ thống cũ hoặc service mới.
- Mỗi lần migration phải có validation và rollback plan.
- Chỉ ngừng hệ thống cũ khi đã xác nhận dữ liệu và nghiệp vụ hoạt động chính xác.
## 2. Strangler Fig Flow
### 2.1. Migration Flow

```text
                 Client
                   |
                   v
              API Gateway
                   |
             Routing Layer
                   |
          +--------+--------+
          |                 |
       Legacy            New Service
       System           (Microservice)
          |                 |
          +--------+--------+
                   |
              Data Store
```
### 2.2. Migration Steps
1. **Legacy Assessment**
   - Kiểm tra API, database schema, business logic và dependency của hệ thống cũ.
   - Xác định các API và dữ liệu thuộc từng Bounded Context.
   - Xác định các luồng nghiệp vụ có thể bị ảnh hưởng khi migration.
2. **Characterization Testing**
   - Viết test ghi nhận behavior hiện tại của hệ thống cũ.
   - Lưu các input/output quan trọng để so sánh kết quả giữa Legacy và Microservice mới.
   - Đặc biệt kiểm thử công thức tính lương, LeaveBalance và AbnormalCase.
3. **Build Microservice**
   - Triển khai service mới theo Bounded Context đã xác định.
   - Sở hữu database riêng.
   - Thiết lập API, event contract và authentication.
4. **Data Migration**
   - Copy dữ liệu ban đầu từ Legacy Database.
   - Đồng bộ các thay đổi phát sinh trong thời gian migration.
   - Validate dữ liệu trước khi chuyển traffic.
5. **Traffic Switching**
   - Chuyển một nhóm API hoặc business flow sang service mới.
   - Theo dõi error rate, latency và business correctness.
   - Tăng dần traffic nếu kết quả đạt yêu cầu.
6. **Decommission Legacy**
   - Ngừng các API và module cũ đã được thay thế.
   - Xóa quyền ghi vào dữ liệu cũ sau khi xác nhận cutover thành công.
   - Giữ backup và dữ liệu phục vụ rollback/audit theo chính sách.
## 3. Data Migration Strategy
### 3.1. Migration Approach
Sử dụng kết hợp **Initial Data Load + CDC (Change Data Capture)** hoặc Outbox tùy khả năng của hệ thống cũ.
| **Phase** | **Strategy** | **Description** |
|-----------|--------------|-----------------|
| Initial Load | Bulk Migration | Copy dữ liệu hiện tại từ Legacy DB sang database của Microservice tương ứng. |
| Incremental Sync | CDC / Outbox | Đồng bộ các thay đổi phát sinh sau Initial Load. |
| Validation | Data Reconciliation | So sánh record count, checksum và business data giữa hai hệ thống. |
| Cutover | Single Writer | Chuyển quyền ghi sang Microservice mới. |
| Cleanup | Legacy Read-only | Chuyển database/module cũ sang read-only sau khi migration thành công. |
### 3.2. Migration Validation
- So sánh số lượng Employee, SalaryProfile, LeaveRequest và Payslip.
- Kiểm tra tính toàn vẹn của `companyCd`, `employeeCd` và các quan hệ dữ liệu.
- So sánh kết quả tính lương giữa Legacy và Payroll Service trên cùng input.
- Kiểm tra LeaveBalance và AbnormalCase sau migration.
- Kiểm tra các AuditEvent quan trọng và dữ liệu có thời hạn lưu trữ 7 năm.
- Chỉ cutover khi các sai lệch đã được giải thích và xử lý.
## 4. Rollback Plan
| **Failure Scenario** | **Rollback Strategy** |
|----------------------|-----------------------|
| Microservice mới có error rate cao | Chuyển routing về Legacy System bằng API Gateway. |
| API latency tăng bất thường | Tắt feature mới hoặc chuyển traffic về Legacy. |
| Data migration sai lệch | Dừng cutover, tiếp tục sử dụng Legacy và sửa dữ liệu migration. |
| Business calculation sai | Dừng PayrollRun mới, đối chiếu kết quả và quay lại phiên bản xử lý đã được xác nhận. |
| Kafka unavailable | Giữ event trong Outbox, retry khi Kafka phục hồi, không rollback business transaction đã commit chỉ vì Kafka tạm thời unavailable. |
| Microservice gặp lỗi sau khi đã ghi dữ liệu | Khôi phục từ backup hoặc thực hiện compensating migration, không tự động chuyển quyền ghi về Legacy nếu dữ liệu mới chưa được đồng bộ ngược. |
### Rollback Principles
- Sử dụng Feature Flag hoặc API Gateway Routing để chuyển traffic.
- Giữ Legacy System hoạt động trong thời gian migration.
- Không xóa dữ liệu cũ ngay sau cutover.
- Tránh để Legacy và Microservice cùng ghi một business data.
- Nếu đã phát sinh dữ liệu mới sau cutover, cần có reverse migration hoặc reconciliation trước khi chuyển quyền ghi về Legacy.
- Với Payroll, không chạy lại Payslip đã hoàn thành, chỉ resume các Payslip thất bại theo trạng thái PayrollRun.
## 5. Technical Debt Register
| **ID** | **Technical Debt** | **Impact** | **Priority** | **Resolution Plan** |
|---|---|---|---|---|
| TD-01 | Legacy business logic chưa có characterization tests đầy đủ | Migration có thể làm thay đổi behavior hiện tại | High | Viết characterization tests trước khi chuyển business flow |
| TD-02 | Database coupling giữa các Context | Service phụ thuộc schema và transaction của service khác | High | Tách database ownership, giao tiếp qua API/Event |
| TD-03 | Thiếu Transactional Outbox | Có thể mất event khi DB commit thành công nhưng Kafka publish thất bại | Critical | Implement Outbox + RelayPublisher |
| TD-04 | Consumer chưa có idempotency đầy đủ | Duplicate event có thể gây gửi email hoặc ghi audit trùng | High | Deduplicate bằng eventId |
| TD-05 | Payroll chưa có checkpoint/recovery hoàn chỉnh | Worker crash có thể khiến PayrollRun không hoàn thành hoặc tính lại Payslip | Critical | Lưu trạng thái từng Payslip và hỗ trợ resume |
| TD-06 | Dữ liệu đầu vào Payroll có thể thay đổi trong lúc tính lương | Các Payslip trong cùng PayrollRun có thể sử dụng dữ liệu không đồng nhất | Critical | Snapshot/version dữ liệu đầu vào tại thời điểm bắt đầu PayrollRun |
| TD-07 | Cache invalidation chưa đảm bảo đồng bộ | Có thể trả SalaryProfile, Policy hoặc Payslip cũ | High | Event-driven invalidation, TTL fallback và monitoring |
| TD-08 | Tenant isolation chưa được kiểm chứng toàn bộ API | Nguy cơ truy cập dữ liệu chéo Tenant | Critical | Enforce tenant scope tại Backend và database query layer |
| TD-09 | Audit event có thể bị thiếu hoặc xử lý trùng | Không đảm bảo khả năng kiểm toán thay đổi salary/payroll | High | Outbox, idempotent consumer và reconciliation |
| TD-10 | Service-to-service authentication chưa hoàn chỉnh | Internal API có nguy cơ bị truy cập trái phép | High | Private Network, mTLS và service-level authorization |
**Priority:**
- Critical: Phải xử lý trước khi đưa production.
- High: Phải có kế hoạch xử lý trước hoặc trong quá trình migration.
- Medium: Có thể xử lý sau khi hệ thống ổn định.
## 6. Migration Risks
| **Risk** | **Impact** | **Mitigation** |
|---|---|---|
| Business logic bị thay đổi trong quá trình migration | Kết quả nghiệp vụ khác Legacy | Characterization tests và parallel validation |
| Data migration thiếu hoặc sai dữ liệu | Sai Payslip, LeaveBalance hoặc EmployeeInfo | CDC/Outbox, reconciliation và backup |
| Dual-write inconsistency | Hai hệ thống có dữ liệu khác nhau | Single Writer và xác định rõ ownership |
| Service dependency chưa được xử lý | Service mới phụ thuộc Legacy quá nhiều | Dependency mapping, API contract và event-driven communication |
| Event loss hoặc duplicate | Audit/Notification bị thiếu hoặc trùng | Transactional Outbox và idempotent consumer |
| Rollback không đồng bộ dữ liệu mới | Mất dữ liệu phát sinh sau cutover | Reverse migration hoặc reconciliation |
| Tenant data leakage | Lộ dữ liệu giữa các công ty | Tenant isolation test và authorization validation |
| Payroll calculation mismatch | Sai tiền lương nhân viên | Parallel calculation và reconciliation trước cutover |
## 7. Success Metrics
| **Metric** | **Success Criteria** |
|---|---|
| Data Migration Accuracy | 100% dữ liệu quan trọng được đối soát, không còn sai lệch chưa giải thích |
| Payroll Calculation Correctness | 100% Payslip được xác nhận thỏa mãn business rules |
| Payroll Duplicate Run | Không tạo duplicate PayrollRun cho cùng Tenant và kỳ lương |
| Tenant Isolation | 0 trường hợp truy cập dữ liệu chéo Tenant trong security testing |
| Payslip API Latency | P95 dưới 2 giây |
| Main API Availability | Mục tiêu 99.9% |
| Event Reliability | Không mất event khi business transaction đã commit thành công |
| Rollback Readiness | Có thể chuyển routing về Legacy theo runbook đã kiểm thử |
| Legacy Decommission | Không còn request hoặc dependency nghiệp vụ cần Legacy sau cutover |