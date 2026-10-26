# 02 — ABAC Policy

| Action | Điều kiện RBAC | Điều kiện ABAC |
|---|---|---|
| Xem salary | Có permission `SALARY_READ` (ví dụ `HR_MANAGER` hoặc `PAYROLL_ADMIN`) | `user.tenantId == employee.tenantId`; user thuộc department được phép truy cập; hồ sơ employee không bị hạn chế theo policy |
| Sửa salary | Có permission `SALARY_UPDATE` (ví dụ `HR_MANAGER`) | `user.tenantId == employee.tenantId`; `user.departmentId == employee.departmentId`; `employee.status != TERMINATED`; thao tác trong thời gian được phép |
| Chạy payroll | Có permission `PAYROLL_RUN` (ví dụ `PAYROLL_ADMIN`) | `user.tenantId == payroll.tenantId`; payroll period đang mở và chưa được chạy/phê duyệt; user không tự phê duyệt payroll do mình khởi chạy |
| Xem audit log | Có permission `AUDIT_READ` (ví dụ `AUDITOR`) | `user.tenantId == auditEvent.tenantId`; chỉ xem phạm vi tenant được gán; truy cập nhạy cảm cần được ghi nhận/audit |
| Employee xem profile | Có permission `EMPLOYEE_READ` | `user.employeeId == employee.id`; `user.tenantId == employee.tenantId`; chỉ hiển thị các trường profile mà employee được phép xem |
