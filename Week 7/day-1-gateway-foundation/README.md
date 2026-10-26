# Day 1 — Gateway Foundation

Bài thực hành ngắn về API Gateway, externalized configuration và service communication.

- [Gateway routing](gateway-routing.md): vai trò và các route mẫu.
- [Phân loại cấu hình](config-classification.md): config thường, config theo môi trường và secret.
- [Sơ đồ hệ thống](gateway-diagram.png).

Trong project hiện tại, truy cập các service qua Gateway ở `http://localhost:8080`:

- `/api/orders/**` → Order Service
- `/api/inventory/**` → Inventory Service
- `/api/payments/**` → Payment Service

Chạy toàn bộ project từ thư mục gốc bằng `docker compose up --build`.
