# API Gateway và routing

API Gateway là điểm vào chung của client. Gateway định tuyến request tới service phù hợp và xử lý các concern dùng chung như CORS, chuyển tiếp token, logging, correlation ID và rate limiting.

| Path | Service |
|---|---|
| `/api/orders/**` | Order Service |
| `/api/inventory/**` | Inventory Service |
| `/api/payments/**` | Payment Service |

Gateway nên giữ các xử lý ở tầng giao tiếp; không đặt logic nghiệp vụ như tính giá, kiểm tra tồn kho hoặc charge payment tại đây. Trong project này, Gateway chuyển tiếp `X-Correlation-Id` (tự tạo nếu request chưa có) và ghi log request.
