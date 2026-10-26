# 01 — RBAC Design

## 1. Overview

Sử dụng **Role-Based Access Control (RBAC)** để quản lý quyền truy cập trong hệ thống HR/Payroll SaaS.

Mô hình:

```text
User
  |
  v
UserTenantRole
  |
  v
Role
  |
  v
RolePermission
  |
  v
Permission
```

- User: Người dùng hệ thống.
- UserTenantRole: Gán role cho user trong một tenant cụ thể.
- Role: Nhóm quyền theo chức năng công việc.
- Permission: Quyền thực hiện một hành động cụ thể trên resource.

Một user có thể:
- Có nhiều role trong cùng một tenant.
- Có role khác nhau ở các tenant khác nhau.
- Không có quyền truy cập tenant nếu chưa được gán role tương ứng.

## 2. Roles & Permissions

### 2.1. Roles

| Role | Responsibility |
|---|---|
| EMPLOYEE | Xem thông tin cá nhân, payslip, gửi leave request |
| HR_SPECIALIST | Quản lý employee, xử lý leave request |
| HR_MANAGER | Quản lý HR, phê duyệt leave, quản lý salary |
| PAYROLL_ADMIN | Thực hiện và quản lý payroll |
| AUDITOR | Đọc audit logs và lịch sử payroll |
| SYSTEM_ADMIN | Quản trị platform, tenant và cấu hình hệ thống |

### 2.2. Permissions

| Permission | Description |
|---|---|
| EMPLOYEE_READ | Xem thông tin employee |
| EMPLOYEE_CREATE | Tạo employee |
| EMPLOYEE_UPDATE | Cập nhật employee |
| LEAVE_READ | Xem leave request |
| LEAVE_APPROVE | Phê duyệt leave request |
| SALARY_READ | Xem thông tin salary |
| SALARY_UPDATE | Cập nhật salary |
| PAYSLIP_READ | Xem payslip |
| PAYROLL_RUN | Khởi chạy payroll |
| PAYROLL_APPROVE | Phê duyệt payroll |
| AUDIT_READ | Xem audit logs |
| USER_ROLE_MANAGE | Quản lý user và role |
| TENANT_MANAGE | Quản lý tenant |
| SYSTEM_CONFIG_MANAGE | Quản lý cấu hình platform |

### 2.3. Role-Permission Mapping

| Role | Permissions |
|---|---|
| EMPLOYEE | PAYSLIP_READ |
| HR_SPECIALIST | EMPLOYEE_READ, EMPLOYEE_CREATE, EMPLOYEE_UPDATE, LEAVE_READ |
| HR_MANAGER | Kế thừa HR_SPECIALIST + LEAVE_APPROVE, SALARY_READ, SALARY_UPDATE |
| PAYROLL_ADMIN | SALARY_READ, PAYROLL_RUN, PAYROLL_APPROVE, PAYSLIP_READ |
| AUDITOR | AUDIT_READ |
| SYSTEM_ADMIN | USER_ROLE_MANAGE, TENANT_MANAGE, SYSTEM_CONFIG_MANAGE |

Lưu ý: EMPLOYEE có PAYSLIP_READ nhưng vẫn phải kiểm tra payslip thuộc chính employee đó.

## 3. Role Hierarchy

Thiết kế hierarchy:

```text
ROLE_HR_MANAGER
       |
       v
ROLE_HR_SPECIALIST
```

HR_MANAGER kế thừa tất cả permissions của HR_SPECIALIST.

Các role còn lại độc lập:

```text
ROLE_EMPLOYEE

ROLE_HR_MANAGER
       |
       v
ROLE_HR_SPECIALIST

ROLE_PAYROLL_ADMIN

ROLE_AUDITOR

ROLE_SYSTEM_ADMIN
```

Không thiết lập hierarchy giữa PAYROLL_ADMIN, HR_MANAGER, AUDITOR và SYSTEM_ADMIN.

Lý do:

- HR_MANAGER quản lý nghiệp vụ nhân sự.
- PAYROLL_ADMIN quản lý nghiệp vụ tính lương.
- AUDITOR cần quyền đọc nhưng không được thay đổi dữ liệu.
- SYSTEM_ADMIN quản trị platform, không mặc định có quyền truy cập dữ liệu nghiệp vụ nhạy cảm.

Trong Spring Security, có thể sử dụng `RoleHierarchy` để tự động kế thừa GrantedAuthority từ role cấp cao xuống role cấp thấp.

## 4. Why Not Use Only ROLE_ADMIN?

Không nên sử dụng một role `ROLE_ADMIN` cho tất cả actions.

Ví dụ:

```text
ROLE_ADMIN
    |
    +-- EMPLOYEE_UPDATE
    +-- SALARY_UPDATE
    +-- PAYROLL_RUN
    +-- PAYROLL_APPROVE
    +-- AUDIT_READ
    +-- USER_ROLE_MANAGE
```

Vấn đề:

1. **Violation of Least Privilege**

   Một HR Manager không nhất thiết được phép khởi chạy payroll hoặc quản trị toàn bộ user.

2. **Separation of Duties**

   Người khởi chạy payroll không nhất thiết được phép phê duyệt payroll. Việc tách quyền giúp giảm nguy cơ gian lận và sai sót.

3. **Difficult to Maintain**

   Khi thêm chức năng mới, ROLE_ADMIN thường tiếp tục được cấp quyền mặc định, khiến phạm vi truy cập ngày càng lớn và khó kiểm soát.

## 5. Risks of Role Explosion

Role Explosion xảy ra khi số lượng role tăng quá nhiều do cố gắng biểu diễn mọi trường hợp truy cập bằng role.

### Risk 1 — Too Many Roles

Ví dụ:

```text
HR_MANAGER_HCM
HR_MANAGER_HN
HR_MANAGER_DN
HR_MANAGER_SALARY_READ
HR_MANAGER_SALARY_UPDATE
```

Mỗi biến thể nghiệp vụ lại tạo thêm một role mới.

### Risk 2 — Difficult to Maintain

Khi permissions thay đổi, phải cập nhật nhiều role tương tự nhau.

Điều này làm tăng nguy cơ cấp thiếu hoặc cấp thừa quyền.

### Risk 3 — Difficult to Understand

Một user có thể sở hữu hàng chục role, khiến việc kiểm tra và giải thích tại sao user có quyền truy cập một resource trở nên phức tạp.

**Solution:** Giữ role theo trách nhiệm công việc, sử dụng ABAC để xử lý các điều kiện truy cập phụ thuộc vào dữ liệu và ngữ cảnh.

## 6. RBAC Limitations & ABAC

RBAC chỉ xác định user có permission nào thông qua role.

Tuy nhiên, trong HR/Payroll có nhiều điều kiện nghiệp vụ mà RBAC không thể biểu diễn đầy đủ.

Ví dụ:

### Case 1 — Employee chỉ được xem dữ liệu của chính mình

Hai employee đều có:

```text
PAYSLIP_READ
```

Nhưng Employee A không được xem payslip của Employee B.

Cần kiểm tra:

```text
resource.employeeCd == currentUser.employeeCd
```

### Case 2 — HR chỉ được quản lý dữ liệu tenant của mình

HR Manager có:

```text
SALARY_READ
SALARY_UPDATE
```

Nhưng chỉ được thao tác trên tenant mà user được gán role.

Cần kiểm tra:

```text
resource.companyCd == currentUser.companyCd
```

### Case 3 — Payroll approval

PAYROLL_ADMIN có:

```text
PAYROLL_APPROVE
```

Nhưng không được phê duyệt payroll do chính mình khởi chạy nếu business policy yêu cầu separation of duties.

Cần kiểm tra thêm:

```text
payrollRun.createdBy != currentUser.userId
```

Những điều kiện này thuộc phạm vi ABAC (Attribute-Based Access Control).

## 7. Authorization Layers

### Request-level Authorization

Kiểm tra quyền ở tầng HTTP request.

Ví dụ:

```text
PUT /employees/{id}
    -> EMPLOYEE_UPDATE

GET /payroll/{id}
    -> SALARY_READ
```

Mục đích: Chặn request không có permission ngay từ đầu.

### Method-level Authorization

Kiểm tra quyền tại business method.

Ví dụ:

```text
updateSalary(employeeId)
    -> SALARY_UPDATE

approvePayroll(payrollRunId)
    -> PAYROLL_APPROVE
```

Mục đích:
- Bảo vệ business logic.
- Tránh trường hợp method bị gọi từ một API hoặc internal flow khác mà không qua request-level authorization.
- Kiểm tra thêm các điều kiện ABAC.

Nên kết hợp cả hai tầng, không chỉ dựa vào request-level authorization.

## 8. Final Design

Sử dụng:

- RBAC: Xác định user được phép thực hiện action nào.
- ABAC: Xác định user có được thực hiện action trên resource cụ thể trong ngữ cảnh hiện tại hay không.
- Tenant Isolation: Đảm bảo user không truy cập dữ liệu của tenant khác.

Authorization flow:

```text
Incoming Request
       |
       v
Verify JWT
       |
       v
Extract User & Tenant
       |
       v
Check Permission (RBAC)
       |
       v
Check Resource Attributes (ABAC)
       |
       v
Execute Business Logic
       |
       v
Return Response
```

Nguyên tắc:

1. Deny by default.
2. Least privilege.
3. Separation of duties.
4. Tenant isolation.
5. Không tin tưởng tenantId hoặc employeeId do client tự truyền lên mà chưa xác thực quyền truy cập.

**Conclusion:**

RBAC phù hợp để quản lý quyền theo trách nhiệm công việc. Khi cần kiểm soát quyền theo ownership, tenant, trạng thái nghiệp vụ hoặc người thực hiện hành động, cần kết hợp ABAC thay vì tiếp tục tạo thêm role.