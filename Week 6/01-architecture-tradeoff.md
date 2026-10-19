# 01 — Architecture Trade-offs

## 1. Bối cảnh và yêu cầu nghiệp vụ

Thiết kế hệ thống Payroll SaaS đa tenant với các yêu cầu:

- Hỗ trợ 50.000 tenants và 5 triệu employees.
- HR quản lý thông tin nhân viên, nghỉ phép và lương.
- Payroll calculation chạy bất đồng bộ (asynchronous) hàng tháng.
- Nhân viên có thể xem payslip trong vòng 2 giây.
- Gửi notification sau khi payslip sẵn sàng.
- Lịch sử kiểm toán liên quan đến lương và payroll phải được lưu giữ trong 7 năm.
- Một enterprise tenant có thể có tối đa 100.000 employees.
- Không được tạo trùng payroll run.
- Admin của tenant A không được phép truy cập dữ liệu của tenant B.

Mục tiêu thiết kế là đáp ứng các yêu cầu trên, đồng thời đảm bảo khả năng mở rộng, bảo mật, tính nhất quán dữ liệu và khả năng vận hành khi xảy ra sự cố.

## 2. Quyết định kiến trúc tổng thể

Lựa chọn kiến trúc microservice gồm 6 services có thể triển khai độc lập:

1. Tenant Service
2. Employee Service
3. Identity Service
4. Payroll Service
5. Audit Service
6. Notification Service

Mỗi service chịu trách nhiệm cho một nhóm nghiệp vụ và sở hữu dữ liệu riêng. Các service giao tiếp với nhau thông qua API hoặc sự kiện bất đồng bộ.

Không cho phép một service truy cập trực tiếp database của service khác.

Kafka được sử dụng để truyền sự kiện, điều phối các công việc nền và tách biệt các tác vụ không cần hoàn thành đồng bộ với request của người dùng.

### 2.1. Lý do lựa chọn microservice

- Các domain có trách nhiệm tương đối độc lập.
- Có thể mở rộng riêng Payroll khi khối lượng tính lương tăng.
- Có thể mở rộng Notification khi lượng thông báo tăng.
- Lỗi gửi thông báo không nhất thiết làm gián đoạn quá trình tính lương.
- Có thể phân chia quyền sở hữu dữ liệu và trách nhiệm vận hành theo từng service.

**Đánh đổi:**

- Tăng độ phức tạp trong giao tiếp giữa các service.
- Phát sinh chi phí triển khai, giám sát, bảo mật và vận hành.
- Phải xử lý eventual consistency, retry, duplicate message và lỗi mạng.
- Các thao tác trước đây có thể thực hiện trong một transaction phải được thiết kế lại bằng quy trình phân tán.

Với quy mô 50.000 tenants và 5 triệu employees, microservice là một lựa chọn hợp lý nếu hệ thống có nhu cầu mở rộng độc lập và các domain thực sự cần ownership riêng. Tuy nhiên, số lượng tenant lớn tự nó không bắt buộc phải sử dụng microservice.

## 3. Trách nhiệm và quyền sở hữu dữ liệu của từng service

| Service | Trách nhiệm | Dữ liệu sở hữu |
|---|---|---|
| Tenant | Quản lý công ty, cấu hình và vòng đời tenant | Thông tin tenant, cấu hình công ty |
| Employee | Quản lý hồ sơ nhân viên, chức vụ và nghỉ phép | Employee, job information, leave records |
| Identity | Xác thực, quản lý tài khoản và phân quyền | Account, password hash, role, permission |
| Payroll | Quản lý lương, tính lương, tạo payslip, phê duyệt và trạng thái thanh toán | Salary records, payroll runs, payslips, calculation results |
| Audit | Lưu lịch sử kiểm toán và phục vụ truy vết | Audit events, actor, timestamp, change history |
| Notification | Gửi thông báo và theo dõi kết quả gửi | Notification jobs, delivery status, retry metadata |

## 4. Chiến lược database và khả năng mở rộng

### 4.1. Sharding theo companyCd

Đề xuất sử dụng `companyCd` làm shard key cho dữ liệu employee và payroll thuộc từng tenant.

Với 50.000 tenants, nếu ban đầu phân bổ khoảng 10.000 tenants trên mỗi shard thì hệ thống có khoảng 5 shards.

Tuy nhiên, đây chỉ là ước lượng ban đầu, không phải giới hạn cố định.

Số lượng employees, khối lượng tính lương, dung lượng lưu trữ và lưu lượng truy cập có thể khác nhau rất lớn giữa các tenants.

Ví dụ:

- Tenant A có 50 employees.
- Tenant B có 500 employees.
- Tenant C có 100.000 employees.

Nếu chỉ phân bổ dựa trên số lượng tenants, một shard có thể chứa nhiều tenant nhỏ nhưng shard khác lại bị quá tải vì một tenant lớn.

Do đó, cần có cơ chế theo dõi tải và phân bổ shard linh hoạt.

Đối với enterprise tenant có tối đa 100.000 employees, có thể cấp shard riêng hoặc tài nguyên database riêng tùy theo mức tải và yêu cầu cách ly.

**Ưu điểm:**

- Hỗ trợ mở rộng dữ liệu theo chiều ngang.
- Giảm phạm vi ảnh hưởng khi một shard gặp sự cố.
- Dễ định tuyến truy vấn theo tenant.
- Có thể cô lập tài nguyên của enterprise tenant.

**Nhược điểm:**

- Phải quản lý shard routing và phân bổ dữ liệu.
- Có thể xảy ra phân bố tải không đồng đều.
- Việc chuyển tenant giữa các shards phức tạp.
- Các truy vấn xuyên shard và báo cáo tổng hợp cần chiến lược riêng.

Cần xây dựng cơ chế cấu hình ánh xạ `companyCd -> shard`, tránh hard-code quy tắc phân bổ shard trong business logic.

### 4.2. Tách đọc và ghi cho Payroll

Payroll sử dụng primary database để ghi dữ liệu và quản lý trạng thái tính lương. Read replica có thể được sử dụng để phục vụ truy vấn payslip.

Về mặt logic, mô hình này là:

- Primary: ghi salary, tạo payroll run, cập nhật trạng thái và lưu kết quả tính lương.
- Read replica: phục vụ truy vấn payslip và lịch sử trả lương.
- Cache: tăng tốc các truy vấn đọc phổ biến khi cần thiết.

Nếu Payroll được sharding, mỗi shard có thể có primary và read replica riêng. Việc có bao nhiêu replica cần được quyết định dựa trên tải thực tế.
