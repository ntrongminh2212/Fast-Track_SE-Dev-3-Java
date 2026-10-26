# Thiết kế Actuator và kiểm tra sức khỏe

## Endpoint Order Service

- `/actuator/health`: trạng thái tổng quan.
- `/actuator/health/liveness`: tiến trình còn hoạt động; không phụ thuộc Inventory, Payment, Kafka hay dịch vụ ngoài để tránh khởi động lại dây chuyền.
- `/actuator/health/readiness`: service đã sẵn sàng nhận request; chỉ thêm kiểm tra dependency nếu việc không sẵn sàng thực sự cần ngừng nhận traffic.
- `/actuator/metrics`: xem các metric của Spring/Micrometer.
- `/actuator/prometheus`: xuất metric ở định dạng Prometheus.
- `/actuator/circuitbreakers`: quan sát trạng thái circuit breaker nếu endpoint được bật.

Health details đang ẩn với người dùng thông thường. Không mở actuator ra internet công khai; giới hạn truy cập bằng network/authentication ở môi trường triển khai.

## Probe và shutdown

Docker Compose kiểm tra `/actuator/health/readiness` và chỉ khởi động Gateway sau khi Order Service healthy. Với Kubernetes, cấu hình liveness/readiness probe riêng. Khi dừng service, graceful shutdown ngừng nhận request mới và chờ tối đa 20 giây cho request đang xử lý.

Liveness chỉ xác nhận ứng dụng còn sống; readiness xác nhận có thể nhận traffic. Tránh để liveness phụ thuộc hệ thống ngoài vì lỗi dependency có thể gây restart hàng loạt.
