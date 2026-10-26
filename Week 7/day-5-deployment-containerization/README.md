# Week 7 — Day 5: Triển khai và đóng gói container

Tài liệu và cấu hình mẫu về Docker image, cấu hình theo môi trường, Kubernetes health probe, chiến lược triển khai và migration tương thích.

## Nội dung

- `Dockerfile`, `.dockerignore`, `.env.example`: ví dụ đóng gói an toàn.
- `docker-compose.yml`: ví dụ local với PostgreSQL và Kafka.
- `k8s/`: manifest Deployment, Service, ConfigMap và Secret mẫu.
- `deployment-strategy.md`: rollout, health check và rollback.
- `migration-plan.md`: quy trình expand/contract cho trạng thái Order.

## Lưu ý với project hiện tại

Dockerfile mẫu dùng build context `../project` và Java 17 theo project. Vì vậy khi Compose build project hiện tại, Docker dùng `.dockerignore` tại `project/.dockerignore`; file `.dockerignore` trong thư mục bài học là danh sách mẫu để tham khảo/copy khi cần.

Compose có PostgreSQL/Kafka để minh họa môi trường bài học, nhưng Order Service hiện chưa tích hợp các dependency này. Các biến DB/Kafka không tự làm ứng dụng kết nối tới chúng. Secret trong ví dụ chỉ là placeholder; không commit credential thật.

Để dùng Dockerfile trong bài với Compose, chạy từ thư mục này. Trước khi chạy, tạo `.env` local từ `.env.example` và thay giá trị local placeholder. Không dùng giá trị đó ở production.
