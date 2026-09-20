# SOLID Refactoring Notes

## 1. Vi phạm SOLID trong code ban đầu

* **SRP:** `PaymentService` có quá nhiều trách nhiệm: validate, xử lý payment, lưu DB, gửi email và log.
* **OCP:** Dùng `if-else` để xử lý từng payment method. Thêm phương thức mới phải sửa `PaymentService`.
* **DIP:** `PaymentService` phụ thuộc trực tiếp vào các implementation cụ thể như `StripeAPI`, `PayPalAPI`, `BankAPI`, `SMTP` và JDBC.

## 2. Lý do thiết kế sau khi refactor

* **OrderValidator:** Tách riêng trách nhiệm validate.
* **PaymentProcessor:** Abstraction chung cho các phương thức thanh toán.
* **PaymentProcessorFactory:** Chịu trách nhiệm chọn processor phù hợp.
* **PaymentRepository:** Tách logic lưu database.
* **NotificationService:** Tách logic gửi notification/email.
* **PaymentLogger:** Tách logic logging.
* **Dependency Injection:** `PaymentServiceImpl` nhận dependency qua constructor, giúp giảm coupling và dễ test.

Sau refactor, `PaymentServiceImpl` chỉ đóng vai trò **điều phối quy trình thanh toán**, còn từng trách nhiệm được giao cho component phù hợp.
