# 07. Security Threat Model
## 1. Security Threats
| **Asset** | **Threat** | **Attack path** | **Mitigation** | **Detection** |
| --------- | ---------- | --------------- | -------------- | ------------- |
| Employee, Payslip, SalaryProfile, PayrollRun | **Broken Access Control / IDOR** | Attacker thay `employeeCd`, `payslipId` hoặc `companyCd` trong request để truy cập dữ liệu của nhân viên hoặc Tenant khác. | Xác định trong JWT đã xác thực có `employeeCd`, `payslipId`, `companyCd` tương ứng hay chưa, nếu không có thì không cho truy cập. | Log các request truy cập resource không thuộc Tenant, alert khi phát hiện nhiều lần truy cập trái phép. |
| AccessToken, RefreshToken | **Token Theft** | Attacker lấy token từ trình duyệt, local storage, log hoặc thông qua XSS, sử dụng token để giả mạo người dùng. | Sử dụng AccessToken có thời hạn ngắn, lưu RefreshToken dạng hash trong DB, xóa token cũ sau khi sử dụng hoặc logout, không log token. | Theo dõi refresh token reuse, đăng nhập bất thường |
| Database, API | **Injection (SQL Injection, Command Injection)** | Attacker gửi payload độc hại qua API input, khiến câu SQL hoặc command bị thay đổi ngoài ý muốn. | Sử dụng Prepared Statement/Parameterized Query, không nối chuỗi input trực tiếp vào SQL, validate input | Log lỗi truy vấn bất thường, theo dõi các request chứa payload đáng ngờ và các lỗi DB tăng đột biến. |
| EmployeeInfo, SalaryProfile, Payslip, BankAccount | **Sensitive Data Exposure** | Attacker đọc dữ liệu nhạy cảm do API trả thừa field, truyền dữ liệu không mã hóa hoặc truy cập trái phép vào database/backup. | HTTPS/TLS, mã hóa dữ liệu nhạy cảm khi lưu trữ, masking thông tin ngân hàng, national ID, API chỉ trả field cần thiết, giới hạn quyền truy cập, không đưa thông tin nhạy cảm vào JWT. | Audit việc đọc/xuất dữ liệu nhạy cảm, alert khi có lượng truy cập hoặc export dữ liệu bất thường. |
| AuditLog, ApplicationLog | **Insufficient Logging & Monitoring** | Attacker thực hiện thay đổi lương, quyền hoặc PayrollRun nhưng hệ thống không ghi nhận đầy đủ actor, thời gian và hành động. | Ghi audit cho các thao tác nhạy cảm: thay đổi lương, policy, phân quyền, phê duyệt và chạy payroll, lưu actorId, companyCd, action, resourceId, timestamp, result, bảo vệ log khỏi sửa/xóa trái phép | Đối soát business event với audit record. |
| Login API, Refresh API, Payroll API | **Rate-limit Abuse / Brute Force / DoS** | Attacker gửi nhiều request đăng nhập, refresh token hoặc gọi API liên tục làm cạn tài nguyên và gây gián đoạn dịch vụ. | Rate limiting theo IP sử dụng redis cache, giới hạn 6 lần đăng nhập sai, timeout | Theo dõi request rate, tỷ lệ login failure, CPU, connection pool, alert khi vượt ngưỡng. |
| JWT, JWKS, Database, Kafka, Cloud Infrastructure | **Security Misconfiguration** | Attacker khai thác endpoint không được bảo vệ, quyền truy cập quá rộng, cấu hình CORS sai, secret bị hardcode hoặc service bỏ qua xác minh JWT. | Tắt debug endpoint ở production, least privilege cho DB/Kafka, cấu hình CORS allowlist, xác minh JWT signature, issuer, audience, expiration, chỉ tin cậy public key từ JWKS hợp lệ | Quét cấu hình bảo mật định kỳ, theo dõi truy cập endpoint nhạy cảm |
## 2. Security Requirements
### Authentication & Authorization
- Identity Service chịu trách nhiệm xác thực người dùng và phát hành JWT AccessToken và JWKS.
- API Gateway và các Backend Service xác minh JWT signature, expiration, issuer và audience sử dụng JWKS từ Identity Service.
- Backend Service phải kiểm tra permission và `companyCd` trước khi truy cập dữ liệu.
- Không tin tưởng `userId`, `companyCd` hoặc permission do Client tự gửi trong request, phải check từ JWT.
### Data Protection
- Mọi giao tiếp giữa Client và Backend sử dụng HTTPS/TLS.
- Mã hóa dữ liệu nhạy cảm khi lưu trữ và backup.
- Không lưu plaintext password mà phải hash bằng BCrypt.
- Không ghi JWT, RefreshToken, password hoặc dữ liệu nhạy cảm vào application log.
- Payslip và SalaryProfile chỉ được truy cập bởi người dùng có `userId`, `companyCd` tương ứng hoặc có quyền tương ứng trong cùng `companyCd`.
### Audit & Monitoring
- Ghi nhận đầy đủ các thao tác thay đổi dữ liệu lương, policy, phân quyền và PayrollRun.
- Audit record cần có actor, action, resource, Tenant, timestamp và kết quả.
- Audit log phải được bảo vệ khỏi việc chỉnh sửa hoặc xóa trái phép.
- Duy trì audit record liên quan salary/payroll trong 7 năm.
- Theo dõi authentication failure, authorization failure, API abuse và các hoạt động bất thường.
## 3. Security Priorities
| **Priority** | **Security Control** |
| ------------ | -------------------- |
| Critical | Tenant isolation, authorization check, JWT validation, encryption of sensitive data. |
| High | RefreshToken rotation, rate limiting, audit logging, secret management. |
| Medium | Security configuration scanning, anomaly detection, log monitoring, periodic access review. |