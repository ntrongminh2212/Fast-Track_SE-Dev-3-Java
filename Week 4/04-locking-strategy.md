# 04 — Hibernate Locking Strategy

## Tình huống: hai HR manager cùng sửa salary của Employee E-100

Cả A và B cùng đọc salary `20M`. A muốn đổi thành `25M`, B đổi thành `22M`.

### 1. Lost update nếu không lock

```text
A đọc 20M                 B đọc 20M
A ghi 25M                 B ghi 22M
Kết quả cuối: 22M (thay đổi của A bị ghi đè)
```

Không có kiểm tra version hoặc khóa phù hợp nên lần ghi sau có thể âm thầm ghi đè lần ghi trước.

### 2. Thiết kế optimistic locking với `@Version`

Thêm version vào entity:

```java
@Entity
class EmployeeSalary {
    @Id
    private UUID id;

    private BigDecimal baseSalary;

    @Version
    private long version;
}
```

Cả A và B đọc version `7`. Khi A cập nhật, Hibernate thực hiện update có điều kiện theo version cũ và tăng version lên `8`. Update của B vẫn yêu cầu version `7`, không cập nhật row nào nên Hibernate phát hiện conflict và ném optimistic locking exception (thường là `OptimisticLockException` hoặc exception Spring bọc tương ứng).

### 3. Exception và UX

- Trả `409 Conflict` với thông báo “Salary đã được cập nhật bởi người khác; hãy tải lại dữ liệu”.
- UI tải lại salary/version mới để HR xem và xác nhận lại thay đổi; không tự động ghi đè giá trị mới.
- Chỉ retry tự động khi nghiệp vụ có thể merge an toàn; nếu không thì cần người dùng giải quyết conflict.
- Ghi nhận conflict để theo dõi, nhưng tránh log dữ liệu salary không cần thiết.

### 4. Khi nào cân nhắc pessimistic lock?

Cân nhắc `PESSIMISTIC_WRITE` khi conflict thường xuyên, tài nguyên khan hiếm cần tuần tự hóa, và critical section ngắn. Đặt lock timeout, xử lý timeout/deadlock, và chỉ khóa đúng row cần thiết. Với thao tác chỉnh salary thông thường, optimistic locking thường phù hợp hơn vì xung đột thường hiếm và không giữ row lock trong lúc người dùng suy nghĩ.

### 5. Không giữ pessimistic lock khi gọi Tax Service

Không gọi external Tax Service trong transaction đang giữ DB lock: network có thể chậm hoặc timeout không dự đoán được, khiến lock bị giữ lâu, các request khác chờ, pool cạn và deadlock dễ xảy ra. Đọc/khóa/cập nhật dữ liệu trong transaction ngắn; gọi service ngoài transaction hoặc dùng workflow phù hợp, rồi kiểm tra version/trạng thái lại trước khi commit nếu cần.
