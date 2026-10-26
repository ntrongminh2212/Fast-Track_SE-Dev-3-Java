# 03 — Method Security, Permission Evaluator và ACL

## Nguyên tắc chung

- **Request-level:** yêu cầu xác thực và áp dụng quyền thô theo endpoint trong `SecurityFilterChain`; đây là lớp bảo vệ đầu tiên, không thay thế kiểm tra ở business method.
- **Method-level:** bật method security và dùng `@PreAuthorize` trên service method để kiểm tra quyền mỗi lần nghiệp vụ được gọi, kể cả khi không đi qua controller.
- **Object-level:** kiểm tra quyền trên đúng employee/payroll/audit record, bao gồm tenant, ownership và department. Có thể tập trung logic trong custom `PermissionEvaluator` hoặc authorization service; dùng ACL khi cần quyền chi tiết theo từng object và kế thừa quyền.
- Mặc định **deny** nếu không có policy cho phép. Không dựa vào `@PostAuthorize` để bảo vệ thao tác ghi, vì method đã chạy trước khi biểu thức này được kiểm tra.

## Thiết kế theo API

| API | Request-level security | Method-level security | Permission evaluator / object-level check |
|---|---|---|---|
| `GET /employees/{id}` | Yêu cầu đã đăng nhập; có thể giới hạn endpoint cho `EMPLOYEE_READ` hoặc role nghiệp vụ phù hợp | `@PreAuthorize` gọi policy kiểm tra người dùng có quyền đọc employee | Kiểm tra employee thuộc cùng tenant; employee chỉ xem hồ sơ của mình, HR chỉ xem theo department/phạm vi được cấp. Dùng evaluator cho `hasPermission(#id, 'Employee', 'READ')` hoặc service policy tương đương |
| `PUT /employees/{id}/salary` | Yêu cầu đã đăng nhập và authority `SALARY_UPDATE` | `@PreAuthorize` trên service method; không chỉ dựa vào cấu hình HTTP | Custom evaluator kiểm tra quyền `EDIT` trên employee/salary object: cùng tenant, department được phép, employee chưa terminated; chỉ cho phép các trường salary được policy cho phép. Ghi audit cho thay đổi |
| `POST /payroll-runs` | Yêu cầu đã đăng nhập và authority `PAYROLL_RUN` | `@PreAuthorize` trên service method chạy payroll | Kiểm tra tenant và payroll period đang mở/chưa chạy; object-level check trên payroll run. Thực thi separation of duties: người chạy không được tự phê duyệt cùng run |
| `GET /audit/salary/{employeeId}` | Yêu cầu đã đăng nhập và authority `AUDIT_READ` | `@PreAuthorize` trên service method đọc audit | Evaluator kiểm tra tenant và phạm vi audit được cấp; chỉ trả sự kiện của employee được phép truy cập. Ghi nhận việc truy cập audit log nếu policy yêu cầu |

Ví dụ minh họa (tên bean và quyền cần thống nhất với cấu hình dự án):

```java
@PreAuthorize("hasAuthority('SALARY_UPDATE') and hasPermission(#employeeId, 'Employee', 'EDIT')")
public void updateSalary(UUID employeeId, UpdateSalaryRequest request) {
    // ...
}
```

`hasPermission` phải được cấu hình với `PermissionEvaluator` phù hợp. Đảm bảo method security được bật, và các method cần bảo vệ được gọi qua Spring proxy; không gọi nội bộ theo cách bỏ qua proxy.

## Test cases allow/deny

| API | Allow | Deny |
|---|---|---|
| `GET /employees/{id}` | Employee đọc profile của chính mình; HR có quyền đọc employee trong cùng tenant và phạm vi được giao | Chưa đăng nhập; không có quyền đọc; employee khác tenant; employee cố đọc profile của người khác |
| `PUT /employees/{id}/salary` | HR có `SALARY_UPDATE`, cùng tenant/department, employee còn active | Thiếu authority; khác tenant/department; employee đã terminated; user chỉ có quyền đọc; request cố tự phê duyệt thay đổi trái separation of duties |
| `POST /payroll-runs` | Payroll Admin có `PAYROLL_RUN`, tenant hợp lệ, period đang mở và chưa chạy | Thiếu authority; sai tenant; period đã đóng/đã chạy; người dùng cố phê duyệt run do chính mình khởi chạy |
| `GET /audit/salary/{employeeId}` | Auditor có `AUDIT_READ` và được phép xem employee trong tenant/phạm vi của mình | Chưa đăng nhập; thiếu authority; sai tenant; employee ngoài phạm vi được cấp |

Các test deny nên xác nhận request bị từ chối (thường `401` khi chưa xác thực, `403` khi đã xác thực nhưng không đủ quyền; dùng `404` thay cho `403` chỉ khi chính sách chống tiết lộ sự tồn tại của resource yêu cầu). Kiểm thử cả controller/API và gọi trực tiếp service method để bảo đảm method security thực sự được áp dụng.
