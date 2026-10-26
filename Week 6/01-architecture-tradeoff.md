# 01 — Architecture Trade-offs

## 1. Architecture Overview

Sử dụng **Microservices Architecture** kết hợp Domain-Driven Design (DDD) và Event-Driven Architecture (EDA).

Hệ thống được chia thành 6 Bounded Context nhưng triển khai thành 5 services.

| Service | Bounded Context | Responsibilities |
|---|---|---|
| Identity Service | Identity + Tenant | Quản lý tenant, employee, user, role, permission và authentication |
| Time Management Service | Time Management | Quản lý working time, leave request, leave balance và attendance |
| Payroll Service | Payroll | Quản lý salary, income policy, deduction policy, payroll run và payslip |
| Audit Service | Audit | Lưu trữ lịch sử thay đổi và audit records |
| Notification Service | Notification | Quản lý notification template và gửi email thông báo |

**Deployment Decision:**
- Identity và Tenant là hai Bounded Context riêng biệt nhưng được deploy chung trong Identity Service.
- 4 services còn lại được deploy độc lập.
- Payroll Worker là thành phần xử lý bất đồng bộ thuộc Payroll Service, không phải một business service riêng.

## 2. Architecture Decisions & Trade-offs

### 2.1 Microservices vs Modular Monolith

**Decision:** Microservices.

**Advantages:**
- Services có thể deploy và scale độc lập.
- Phân chia rõ trách nhiệm và quyền sở hữu dữ liệu.
- Một service gặp sự cố không nhất thiết làm toàn bộ hệ thống ngừng hoạt động.

**Trade-offs:**
- Tăng độ phức tạp trong deployment và monitoring.
- Cần xử lý network failure và distributed transactions.
- Tốn thêm infrastructure và operational cost.

### 2.2 Identity + Tenant Deployment

**Decision:** Deploy Identity và Tenant trong cùng một service.

**Advantages:**
- Giảm số lượng deployable services.
- Đơn giản hóa việc triển khai và vận hành.
- Thuận tiện khi các chức năng quản lý tenant và identity có liên quan chặt chẽ.

**Trade-offs:**
- Hai Bounded Context không thể scale và deploy độc lập.
- Cần giữ ranh giới domain rõ ràng để tránh coupling.

### 2.3 Database per Service

**Decision:** Mỗi service sở hữu database riêng.

**Advantages:**
- Đảm bảo data ownership.
- Có thể lựa chọn database strategy phù hợp với từng service.
- Tránh coupling thông qua database schema.

**Trade-offs:**
- Không thể sử dụng database transaction xuyên nhiều services.
- Cần xử lý data consistency thông qua API hoặc events.
- Tăng chi phí quản lý database.

### 2.4 REST vs gRPC vs Kafka

**Decision:**
- REST cho client-facing APIs.
- gRPC cho internal synchronous communication khi thực sự cần.
- Kafka cho asynchronous communication.

**Advantages:**
- REST dễ tích hợp với frontend và external clients.
- gRPC phù hợp với synchronous service-to-service calls.
- Kafka giúp giảm coupling và xử lý workload bất đồng bộ.

**Trade-offs:**
- REST/gRPC phụ thuộc vào availability của service được gọi.
- Kafka có eventual consistency và cần xử lý duplicate events.
- Tăng độ phức tạp trong debugging và monitoring.

### 2.5 Asynchronous Payroll Processing

**Decision:** Sử dụng Payroll Worker để tính payroll bất đồng bộ.

**Advantages:**
- Không giữ HTTP request trong thời gian tính payroll.
- Có thể scale worker độc lập theo workload.
- Có thể retry và resume các payslips thất bại.

**Trade-offs:**
- Kết quả không có ngay sau khi gửi request.
- Cần quản lý processing status và failure recovery.
- Cần đảm bảo không tính trùng payslip hoặc payroll run.

### 2.6 Transactional Outbox + Kafka

**Decision:** Sử dụng Transactional Outbox để publish events.

**Advantages:**
- Tránh mất event khi business transaction đã commit nhưng Kafka publish thất bại.
- Business data và Outbox record được lưu trong cùng transaction.

**Trade-offs:**
- Cần Outbox Relay để publish events.
- Có thể publish duplicate events.
- Consumer phải implement idempotency.
- Cần monitoring và cleanup Outbox records.

### 2.7 Redis Distributed Cache

**Decision:** Sử dụng Redis Cluster làm shared caching infrastructure.

**Advantages:**
- Giảm database load.
- Cải thiện read latency.
- Có thể chia sẻ caching infrastructure giữa các services.

**Trade-offs:**
- Cần xử lý cache invalidation.
- Có nguy cơ stale data.
- Redis trở thành shared infrastructure cần được monitoring và bảo vệ.
- Mỗi service cần quản lý namespace và quyền truy cập riêng.

### 2.8 Database Sharding

**Decision:** Shard dữ liệu theo `companyCd`, cân nhắc shard riêng cho enterprise tenant.

**Advantages:**
- Phân tán dữ liệu và workload giữa nhiều database instances.
- Hạn chế ảnh hưởng giữa các tenants lớn và nhỏ.
- Có thể scale database theo nhu cầu.

**Trade-offs:**
- Tăng độ phức tạp trong routing và migration dữ liệu.
- Cross-shard queries khó thực hiện.
- Cần cân nhắc phân bổ và rebalance shards.

## 3. Summary

| Decision | Main Benefit | Main Trade-off |
|---|---|---|
| Microservices | Independent deployment and scaling | Operational complexity |
| Identity + Tenant | Fewer deployable services | Cannot scale independently |
| Database per Service | Data ownership | Distributed consistency |
| REST/gRPC/Kafka | Flexible communication | Multiple communication patterns |
| Async Payroll | Scalable processing | Eventual completion and recovery |
| Transactional Outbox | Reliable event publishing | Duplicate handling and relay |
| Redis Cluster | Faster reads | Cache invalidation and stale data |
| Database Sharding | Database scalability | Routing and migration complexity |