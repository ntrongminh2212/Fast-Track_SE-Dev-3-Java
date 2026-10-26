# 05 — EmployeeSalary Audit Design

## 1. Mục tiêu và cách triển khai

Theo dõi ai thay đổi salary, giá trị trước/sau, thời điểm và request gây ra thay đổi; có thể dựng lại giá trị tại một thời điểm. Có thể dùng Hibernate Envers (`@Audited`) cho lịch sử entity và custom revision entity để bổ sung `actorId`, `tenantId` và `correlationId`. Chỉ audit các trường nghiệp vụ cần thiết.

## 2. Schema tối giản

| Bảng | Cột chính | Mục đích |
|---|---|---|
| `employee_salary` | `id`, `employee_id`, `base_salary`, `effective_date`, `status`, `version` | Bản ghi hiện tại; `version` hỗ trợ optimistic locking |
| `employee_salary_aud` | `id`, `rev`, `revtype`, `employee_id`, `base_salary`, `effective_date`, `status` | Snapshot các trường salary tại từng revision |
| `revinfo` (custom revision entity) | `rev`, `revision_timestamp`, `actor_id`, `tenant_id`, `correlation_id` | Metadata người thực hiện, thời điểm, tenant và request liên quan |

`revtype` biểu thị tạo/sửa/xóa theo quy ước Envers. Index tối thiểu nên hỗ trợ `(employee_id, rev)` và truy vấn theo tenant/thời gian nếu thiết kế lưu tenant trong audit record. Với hệ thống lớn, partition/retention cần dựa trên volume và yêu cầu pháp lý.

## 3. Dữ liệu audit và không audit

| Audit | Không audit / hạn chế |
|---|---|
| `employeeId`, salary trước/sau, `effectiveDate`, `status`, loại hành động | Password, password hash, token, secret, thông tin xác thực |
| Actor/user ID, tenant ID, timestamp UTC, correlation/request ID | Toàn bộ request/response body nếu chứa dữ liệu cá nhân không cần thiết |
| Lý do thay đổi hoặc mã phê duyệt nếu nghiệp vụ yêu cầu | Dữ liệu nhạy cảm không liên quan đến salary hoặc không có mục đích lưu trữ rõ ràng |

Hạn chế quyền đọc audit, mã hóa khi lưu/truyền theo chuẩn hệ thống, tránh sửa/xóa tùy tiện, đặt retention và quy trình xóa/ẩn danh phù hợp với yêu cầu pháp lý. Không ghi bí mật hoặc PII không cần thiết vào application log.

## 4. Tra cứu giá trị tại thời điểm bất kỳ

Dùng Envers `AuditReader` để truy vấn revision của `EmployeeSalary` tại thời điểm yêu cầu. Chọn revision mới nhất có timestamp không sau thời điểm đó; nếu chưa có revision nào thì trả “chưa có dữ liệu tại thời điểm này”. Truy vấn phải giới hạn theo tenant và kiểm tra quyền truy cập. Kiểm thử cả thời điểm giữa hai revision và trường hợp entity bị xóa.

## 5. Hiển thị lịch sử cho HR/Admin

- Màn hình chỉ dành cho người có quyền audit phù hợp; kiểm tra tenant và phạm vi employee ở backend.
- Hiển thị timeline: thời gian, người thay đổi, lý do/mã phê duyệt, request/correlation ID và các trường salary thay đổi theo dạng before → after.
- Phân trang theo thời gian/revision, mặc định giới hạn số dòng; không tải toàn bộ lịch sử.
- Không cung cấp chức năng sửa audit history; ghi nhận việc truy cập dữ liệu audit nhạy cảm nếu policy yêu cầu.
