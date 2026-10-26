# Chiến lược triển khai — Order Service

## 1. Chiến lược image

- **Tên/tag:** `order-service:<git-sha>`; tránh dùng `latest` để triển khai production.
- **Cách build:** Dockerfile multi-stage; build JAR ở stage Maven, runtime chỉ có JRE và JAR.
- **Base image:** Java 17 theo cấu hình hiện tại của project; pin tag hoặc digest đã được kiểm duyệt.
- **Quyền chạy:** dùng user non-root; loại `target`, `.git`, file môi trường và khóa khỏi build context.

## 2. Cấu hình lúc chạy

- **ConfigMap:** profile, port, URL service, DB host/name, Kafka bootstrap server, timeout và feature flag không nhạy cảm.
- **Secret:** DB/Kafka credential, JWT key, API key; lấy từ secret manager/Vault hoặc Kubernetes Secret được quản lý an toàn.
- **Không đưa lên Git:** file `.env` thật, mật khẩu, token, private key, dump dữ liệu và log nhạy cảm.

## 3. Kiểm tra sức khỏe

- **Liveness:** `/actuator/health/liveness` — không kiểm tra DB/Kafka hay service ngoài.
- **Readiness:** `/actuator/health/readiness` — quyết định pod có nhận traffic không; chỉ gồm dependency critical theo quyết định vận hành.
- **Startup:** `/actuator/health` — cho ứng dụng thời gian khởi động trước các probe khác.

## 4. Chiến lược rollout

- Dùng rolling update trước; với thay đổi rủi ro dùng canary.
- Canary ban đầu: 5% traffic hoặc 1 pod trong 10–15 phút, sau đó tăng dần nếu đạt tiêu chí.
- Theo dõi checkout success rate, 5xx, p95/p99, readiness, lỗi Inventory/Payment, circuit breaker và order bị kẹt.
- Mở rộng canary theo bước nhỏ và có người trực theo dõi.

## 5. Tiêu chí rollback

- Checkout 5xx vượt 1% trong 5 phút hoặc tăng rõ rệt so với baseline.
- p95 checkout vượt 800 ms trong 10 phút hoặc SLO hiện hành bị vi phạm.
- Readiness lỗi trên nhiều pod, hoặc số order bị kẹt/payment mismatch tăng bất thường.
- Dừng rollout trước; rollback image/traffic khi nguyên nhân liên quan bản phát hành. Không rollback schema phá hủy dữ liệu một cách tự động.

## 6. Migration cơ sở dữ liệu

- **Expand:** thêm cột/trạng thái mới theo cách không phá vỡ code cũ.
- **Tương thích:** code mới đọc được trạng thái cũ và mới; giữ cột cũ.
- **Backfill:** chuyển dữ liệu theo lô, có thể tiếp tục/chạy lại an toàn; kiểm tra số lượng và tính nhất quán.
- **Cutover:** canary code mới, chuyển đọc sang mô hình mới, theo dõi dữ liệu và lỗi.
- **Contract:** chỉ xóa cột/logic cũ sau khi hết thời hạn rollback và xác minh dữ liệu.

## 7. Kế hoạch rollback

- **Ứng dụng:** trở lại image theo Git SHA trước đó nếu schema vẫn tương thích.
- **Feature flag:** tắt luồng mới để giảm ảnh hưởng mà không cần build lại.
- **Sửa dữ liệu:** chuẩn bị script bù có kiểm soát, backup và đối soát trước/sau; không xóa/sửa dữ liệu trực tiếp tùy tiện.

> Đây là kế hoạch mẫu cho bài học. Project hiện tại chưa tích hợp PostgreSQL/Kafka; các biến cấu hình này chỉ có hiệu lực sau khi có phần tích hợp tương ứng.
