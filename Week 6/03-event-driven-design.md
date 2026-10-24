# 03 — Event-Driven Design

## 1. Events Commands:

| Command | Service xử lý — Aggregate | Event publish | Consumer |
|---|---|---|---|
| `ApproveLeaveRequest` | Time Management Service — `LeaveRequest` | `LeaveRequestApproved` | Notification Service — `Gửi mail thông báo LeaveRequest đã được approve` |
| `CancelLeaveRequest` | Time Management Service — `LeaveRequest` | `LeaveRequestCancelled` | Notification Service — `Gửi mail thông báo LeaveRequest bị từ chối` |
| `AbnormalWorkingTime` | Time Management Service — `AbnormalCase` | `AbnormalCaseRecorded` | Notification Service — `Gửi mail nhắc nhở nhân viên về ngày nghỉ không lý do cần request` |
| `CreateSalaryProfile` / `UpdateSalaryProfile` | Payroll Service — `SalaryProfile` | `SalaryProfileChanged` | Audit — `Lưu audit trail` |
| `CreateIncomePolicy` / `UpdateIncomePolicy` | Payroll Service — `IncomePolicy` | `IncomePolicyChanged` | Audit — `Lưu audit trail` |
| `CreateDeductionPolicy` / `UpdateDeductionPolicy` | Payroll Service — `DeductionPolicy` | `DeductionPolicyChanged` | Audit — `Lưu audit trail` |
| `CalculatePayslip` | Payroll Service — `Payslip` | `PayslipReady` |  Notification Service — `Gửi mail thông báo Payslip tháng này đã có thể xem` |

## 2. Transactional Outbox
- Tạo bảng Outbox cho Payroll Service để tránh trường hợp DB cập nhật thành công nhưng publish Kafka thất bại, dẫn đến Audit không nhận được message.

| Outbox fields | Ý nghĩa | 
|---------------|---------| 
| auditEventId | khóa | 
| payload | JSON object chứa nội dung cần gửi cho Audit Consumer: `companyCd`, `actorId`, `action`, `resourceObject`, `resourceField`, `timestamp`, `valueBefore`, `valueAfter` | 
| publishStatus | trạng thái publish lên kafka: `PENDING`, `PUBLISHED` | 

- Tạo `RelayPublisher` trên cùng Payroll Service, query từ table Outbox mỗi 10s, nếu có `PENDING` thì thực hiện publish.

### 2.1 Events ứng dụng: 
- `CreateSalaryProfile`
- `UpdateSalaryProfile`
- `CreateIncomePolicy`
- `UpdateIncomePolicy`
- `CreateDeductionPolicy`
- `UpdateDeductionPolicy`

### 2.2 Flow chạy:
1. Các service ở 2.1 cập nhật dữ liệu nghiệp vụ và insert bản ghi vào bảng `Outbox` trong cùng một DB transaction.
2. `RelayPublisher` đọc các bản ghi `PENDING` mỗi 10s và gửi event lên Kafka.
3. Khi Kafka xác nhận gửi thành công, `RelayPublisher` update bảng Outbox set publishStatus = `PUBLISHED`.
4. Nếu publish thất bại, thực hiện retry.

Ví dụ:
1. Trường hợp bình thường:
`UpdateSalaryProfile` → DB transaction cập nhật `SalaryProfile` + insert `SalaryProfileChanged` vào Outbox → commit → `RelayPublisher` gửi Kafka → Audit Consumer nhận event → `RelayPublisher` update bảng Outbox set publishStatus = `PUBLISHED` .

2. Trườn hợp Payroll Service crash sau khi `RelayPublisher` đã publish Kafka nhận event:
Audit Consumer nhận event → Payroll Service crash → Payroll Service rerun và `RelayPublisher` retry publish Kafka → Audit Consumer kiểm tra auditEventId đã tồn tại → response kết quả hiện tại → `RelayPublisher` update bảng Outbox set publishStatus = `PUBLISHED`.

## 2. Retry và Dead Letter Queue (DLQ)
- Kafka Consumer: Retry lỗi tạm thời có giới hạn và backoff; lỗi dữ liệu không hợp lệ hoặc lỗi không thể phục hồi chuyển sang DLQ.
- Notification: Retry gửi email tối đa 3 lần theo chính sách hiện tại, hết lượt thì đánh dấu FAILED và cho phép vận hành xử lý lại.
- DLQ: Lưu event gốc, eventId, loại lỗi, số lần retry và thời điểm lỗi. Có công cụ replay sau khi khắc phục nguyên nhân.