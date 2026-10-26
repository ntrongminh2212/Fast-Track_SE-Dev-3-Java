# Capstone Architecture — Payroll SaaS

## 1. System Overview

Xây dựng hệ thống Payroll SaaS multi-tenant phục vụ nhiều công ty.

### Business Requirements

- 50,000 tenants.
- 5 million employees.
- Enterprise tenant có thể có 100,000 employees.
- HR quản lý employee, leave và salary.
- Payroll được tính hàng tháng và xử lý bất đồng bộ.
- Employee xem payslip với P95 dưới 2 giây.
- Notification được gửi khi payslip sẵn sàng.
- Audit records được lưu trữ trong 7 năm.
- Không được chạy trùng payroll cho cùng kỳ.
- Tenant A không được truy cập dữ liệu của Tenant B.

### Non-functional Requirements

- Availability: 99.9%.
- Horizontal scalability.
- Strict tenant isolation.
- Reliable event processing.
- Idempotent operations.
- Observability và khả năng phục hồi khi có lỗi.

## 2. Architecture Style

Sử dụng Microservices Architecture kết hợp:

- Domain-Driven Design để phân chia Bounded Context.
- Event-Driven Architecture để giao tiếp bất đồng bộ.
- REST API cho client-facing operations.
- Kafka cho domain/integration events.
- Redis cho distributed caching.
- Batch Worker cho payroll calculation.

Các nguyên tắc chính:

- Mỗi service sở hữu database riêng.
- Không truy cập trực tiếp database của service khác.
- Giao tiếp đồng bộ khi cần response ngay.
- Giao tiếp bất đồng bộ khi không cần kết quả tức thời.
- Đảm bảo tenant isolation tại backend.

## 3. Bounded Contexts & Services

Hệ thống có 6 Bounded Context, được triển khai thành 5 services.

| Bounded Context | Deployable Service | Responsibility |
|---|---|---|
| Tenant | Identity Service | Company information |
| Identity | Identity Service | Employee, User, Role, Permission |
| Time Management | Time Management Service | Working time, Leave, Attendance |
| Payroll | Payroll Service | Salary, Policy, Payslip, PayrollRun |
| Audit | Audit Service | Audit history |
| Notification | Notification Service | Email notification |

**Lưu ý:** Tenant và Identity là hai Bounded Context riêng biệt nhưng được deploy chung trong một service.

## 4. High-level Architecture

```text
                    Client
                      |
                 API Gateway
                      |
        +-------------+-------------+
        |             |             |
     Identity       TMS          Payroll
     Service      Service        Service
        |             |             |
        DB            DB            DB
                                    |
                               Payroll Worker
                                    |
                                  Kafka
                               /         \
                           Audit      Notification
                          Service       Service
                             |             |
                             DB            DB

                     Redis Cluster
                (Shared infrastructure)
```

### Main Components

- API Gateway: Routing, authentication validation, rate limiting.
- Identity Service: Tenant và identity management.
- Time Management Service: Attendance và leave management.
- Payroll Service: Payroll domain và payslip management.
- Payroll Worker: Asynchronous payroll calculation.
- Audit Service: Consume events và lưu audit history.
- Notification Service: Consume events và gửi notification.
- Kafka: Event broker.
- Redis: Distributed cache.

## 5. Database Strategy

Mỗi service sở hữu database riêng.

| Service | Main Data |
|---|---|
| Identity | Tenant, Employee, User, Role, Permission |
| Time Management | WorkingTime, LeaveRequest, LeaveBalance, AbnormalCase |
| Payroll | SalaryProfile, IncomePolicy, DeductionPolicy, Payslip, PayrollRun |
| Audit | AuditRecord |
| Notification | Notification, NotificationTemplate |

### Multi-tenant Data Isolation

- Các business records có `companyCd` hoặc `tenantId`.
- Backend luôn kiểm tra tenant scope trước khi truy cập dữ liệu.
- Database query phải giới hạn theo tenant.
- Có thể shard database theo `companyCd`.
- Enterprise tenant có thể được đặt trên shard riêng.

## 6. API & Communication Strategy

| Communication | Usage |
|---|---|
| REST | Client-facing APIs |
| gRPC | Internal synchronous calls khi cần |
| Kafka | Asynchronous events |
| Batch Worker | Payroll calculation |

### API Design

- REST API versioning: `/api/v1/...`
- Pagination cho danh sách dữ liệu lớn.
- Idempotency cho các operation quan trọng.
- Correlation ID để trace request.
- Không expose internal database structure qua API.

Không sử dụng GraphQL ở giai đoạn hiện tại vì các use cases chủ yếu là CRUD và business operations có cấu trúc rõ ràng.

## 7. Event-Driven Architecture

Sử dụng Kafka để truyền integration events giữa các services.

### Main Events

| Event | Producer | Consumer |
|---|---|---|
| SalaryUpdated | Payroll | Audit |
| PayslipReady | Payroll | Notification |
| LeaveRequestApproved | Time Management | Notification |
| LeaveRequestRejected | Time Management | Notification |

### Transactional Outbox

Để tránh mất event khi database commit thành công nhưng publish Kafka thất bại:

1. Business data và Outbox record được ghi trong cùng transaction.
2. Outbox Relay đọc các pending records.
3. Relay publish event lên Kafka.
4. Sau khi publish thành công, record được đánh dấu `PUBLISHED`.

### Idempotent Consumer

- Mỗi event có một `eventId` duy nhất.
- Consumer kiểm tra event đã xử lý hay chưa.
- Lưu processed event trong database cùng transaction với business changes.
- Chấp nhận duplicate delivery nhưng không tạo duplicate business effects.

### Failure Handling

- Retry với exponential backoff và jitter.
- Đưa message lỗi không thể xử lý vào Dead Letter Queue.
- Theo dõi Kafka consumer lag và pending Outbox records.

## 8. Payroll Processing Architecture

Payroll calculation được xử lý bất đồng bộ.

### Processing Flow

1. HR hoặc Admin gửi request tạo PayrollRun.
2. Payroll Service kiểm tra request và tạo PayrollRun.
3. Worker lấy danh sách employees cần tính.
4. Worker đọc SalaryProfile, IncomePolicy, DeductionPolicy và attendance data.
5. Worker tính toán từng employee.
6. Payslip được lưu vào database.
7. `PayslipReady` được publish qua Outbox.
8. Notification Service gửi email thông báo.

### Payroll Calculation

```text
Gross Salary = Base Salary + Total Income

Total Deduction = Sum of Deductions

Net Salary = Gross Salary
             - Total Deduction
             - TMS Violation Penalty
```

### Important Design Decisions

- Mỗi employee được xử lý độc lập.
- Không đặt toàn bộ 100,000 payslips trong một aggregate.
- PayrollRun có thể resume các payslips thất bại.
- Payslip đã hoàn thành không được tính lại khi retry.
- Unique constraint ngăn tạo PayrollRun trùng:

```text
(companyCd, payPeriod, payrollType, runVersion)
```

### Data Consistency

Tại thời điểm bắt đầu PayrollRun, cần xác định snapshot của:

- SalaryProfile.
- IncomePolicy.
- DeductionPolicy.
- Attendance data.

Điều này giúp các payslips trong cùng kỳ sử dụng dữ liệu nhất quán.

Payslip đã hoàn thành nên được xem là immutable. Nếu cần điều chỉnh, tạo version hoặc adjustment mới thay vì âm thầm sửa dữ liệu cũ.

## 9. Caching Strategy

Sử dụng một Redis Cluster dùng chung, nhưng mỗi service quản lý namespace và cache key riêng.

### Cache Candidates

| Data | Strategy |
|---|---|
| Tenant Information | Cache-Aside |
| Employee Information | Cache-Aside |
| Job Information | Cache-Aside |
| Payslip | Cache-Aside |
| LeaveBalance | Cache-Aside |

### Important Decisions

- TTL là cơ chế fallback, không thay thế cache invalidation.
- Các dữ liệu thay đổi cần có cơ chế invalidation phù hợp.
- Payroll calculation đọc dữ liệu từ database, không phụ thuộc Redis cache.
- Payslip chỉ được cache sau khi đã hoàn thành và commit.
- Khi payslip được điều chỉnh, cần invalidate hoặc version cache.

## 10. Security Architecture

### Authentication

- Client đăng nhập thông qua Identity Service.
- Access Token sử dụng signed JWT.
- Refresh Token là opaque random token.
- Chỉ lưu hash của Refresh Token trong database.
- Refresh Token được rotate khi sử dụng.
- JWKS endpoint cung cấp public keys để services verify JWT.

### Authorization

Sử dụng RBAC:

- EMPLOYEE
- HR_MANAGER
- AUDITOR
- TENANT_ADMIN

Backend kiểm tra permission và tenant scope trước khi thực hiện business operation.

### Service-to-Service Security

- Sử dụng private network.
- Sử dụng mTLS hoặc workload identity.
- Không chỉ dựa vào IP allowlist.
- Mỗi service chỉ được phép gọi các service cần thiết.

### Data Protection

- HTTPS/TLS cho network communication.
- Encryption at rest cho dữ liệu nhạy cảm.
- Không ghi password, token hoặc sensitive information vào logs.
- Audit các thao tác nhạy cảm.

## 11. Deployment Architecture

- Deploy các services độc lập.
- Identity và Tenant deploy chung.
- Payroll Worker có thể scale độc lập với Payroll API.
- Kafka và Redis là shared infrastructure.
- Database được quản lý riêng theo service.
- API Gateway là entry point cho client.

### Scaling Strategy

| Component | Scaling Strategy |
|---|---|
| API Gateway | Horizontal scaling |
| Identity | Horizontal scaling |
| Time Management | Horizontal scaling |
| Payroll API | Horizontal scaling |
| Payroll Worker | Scale theo workload |
| Notification | Scale theo Kafka lag |
| Audit Consumer | Scale theo Kafka lag |

Enterprise tenant có thể được phân bổ database shard riêng để tránh ảnh hưởng các tenants khác.

## 12. Observability & Reliability

### Metrics

- API latency và error rate.
- Payroll processing duration.
- Payroll success/failure count.
- Kafka consumer lag.
- Outbox pending count.
- Notification success/failure rate.
- Cache hit ratio.
- Database connection pool usage.

### Logging & Tracing

- Structured logging.
- Correlation ID xuyên suốt các services.
- Distributed tracing cho request liên service.
- Không ghi sensitive data vào logs.

### Reliability

- Timeout cho synchronous calls.
- Retry có giới hạn.
- Circuit breaker khi cần.
- Idempotent event consumers.
- Outbox để tránh mất event.
- Monitoring và alerting cho các failure scenarios.

## 13. Modernization & Migration Strategy

Nếu thay thế một hệ thống legacy hiện có, sử dụng Strangler Fig Pattern.

### Migration Flow

1. Assess legacy system và dependencies.
2. Viết characterization tests cho business logic quan trọng.
3. Xây dựng các services mới.
4. Bulk migrate dữ liệu ban đầu.
5. Đồng bộ thay đổi bằng CDC hoặc Outbox.
6. Reconcile dữ liệu giữa hệ thống cũ và mới.
7. Chuyển traffic qua API Gateway.
8. Chuyển hệ thống cũ sang read-only.
9. Decommission legacy components sau khi xác nhận ổn định.

### Rollback Strategy

- Sử dụng Gateway routing hoặc feature flags để chuyển traffic về hệ thống cũ.
- Dừng payroll processing nếu phát hiện sai lệch dữ liệu.
- Không rollback mù quáng nếu hệ thống mới đã phát sinh writes.
- Cần reconciliation hoặc reverse migration trước khi chuyển quyền ghi trở lại.

## 14. Key Architecture Trade-offs

| Decision | Trade-off |
|---|---|
| Microservices | Independent deployment nhưng tăng operational complexity |
| Kafka | Loose coupling nhưng cần xử lý eventual consistency |
| Outbox | Reliable event publishing nhưng tăng storage và relay complexity |
| Redis | Giảm database load nhưng cần cache invalidation |
| Database per service | Data ownership rõ ràng nhưng cần xử lý distributed workflows |
| Async Payroll | Scale tốt nhưng cần tracking, retry và recovery |
| Shared Redis Cluster | Tiết kiệm infrastructure nhưng cần namespace và access isolation |

## 15. Architecture Decision Summary

- 6 Bounded Contexts, 5 deployable services.
- Identity và Tenant được deploy chung.
- Database per service.
- REST cho client-facing APIs.
- Kafka cho asynchronous integration events.
- Transactional Outbox để đảm bảo event không bị mất sau database commit.
- Idempotent consumers để xử lý duplicate events.
- Payroll calculation chạy bất đồng bộ qua Worker.
- Payroll sử dụng consistent input snapshots.
- Redis dùng cho read-heavy data.
- JWT cho user authentication.
- RBAC và tenant scope cho authorization.
- mTLS/workload identity cho service-to-service security.
- Observability bao gồm logs, metrics và distributed tracing.

## Conclusion

Kiến trúc hướng đến khả năng mở rộng, tenant isolation và độ tin cậy trong quá trình xử lý payroll.

Các quyết định quan trọng nhất là phân chia domain rõ ràng, kiểm soát quyền truy cập theo tenant, xử lý payroll bất đồng bộ, đảm bảo event delivery bằng Outbox và thiết kế retry/idempotency cho các workflow quan trọng.