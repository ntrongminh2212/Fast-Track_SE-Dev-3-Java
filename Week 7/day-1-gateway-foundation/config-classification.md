# Phân loại cấu hình

Không hard-code endpoint, timeout hay thông tin nhạy cảm trong business code. Đưa cấu hình ra file cấu hình hoặc biến môi trường phù hợp.

| Loại | Ví dụ | Commit Git? |
|---|---|---|
| Application config | timeout, route, page size | Có thể |
| Environment config | host, port dev/staging/prod | Tùy chính sách; có thể dùng biến môi trường |
| Secret | DB password, API key, private key | Không; dùng secret manager hoặc biến môi trường được bảo vệ |
| Feature flag | bật luồng thanh toán mới | Có thể, cần quản lý rõ |

Các URL đích của Gateway đang lấy từ `ORDER_SERVICE_URL`, `INVENTORY_SERVICE_URL` và `PAYMENT_SERVICE_URL`. Docker Compose cấp địa chỉ theo DNS nội bộ, ví dụ `http://order-service:8081`; khi chạy trực tiếp trên máy, cấu hình mặc định dùng `localhost`.

Trong môi trường Kubernetes, Service/DNS thường đủ cho service discovery cơ bản; Eureka hoặc Consul phù hợp khi cần registry và discovery ngoài nền tảng đó. Với nhiều instance, client-side load balancing phân phối request giữa các instance.
