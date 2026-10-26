# 05 — Spring MVC vs WebFlux & R2DBC

## API A — `GET /employees/{id}`

**Chọn Spring MVC.** Traffic vừa phải, truy vấn dùng JPA/Hibernate và có cache; MVC phù hợp với stack blocking sẵn có, đơn giản hơn để vận hành.

- **Blocking source:** JPA/Hibernate và JDBC khi cache miss; cache cũng cần được kiểm tra nếu thao tác truy cập cache có thể block.
- **Nếu chọn WebFlux:** cần thay JPA/JDBC bằng driver reactive như R2DBC và dùng cache/client không blocking. Chỉ bọc lời gọi JPA trong reactive type không biến nó thành non-blocking.
- **Timeout/fallback:** đặt timeout cho truy vấn và cache theo SLA; khi cache miss hoặc lỗi DB, trả lỗi phù hợp (ví dụ 404 nếu không có nhân viên, 503 khi dependency unavailable). Không trả dữ liệu cũ nếu nghiệp vụ không cho phép.

## API B — `GET /payroll-dashboard`

**Chọn WebFlux**, nếu Payroll, Tax và Benefits được gọi bằng `WebClient` (non-blocking) và không có blocking I/O trong pipeline. Các gọi độc lập có thể chạy song song; kết hợp kết quả bằng `Mono.zip`.

- **Blocking source cần tránh:** gọi HTTP bằng client blocking, JDBC/JPA, hoặc xử lý đồng bộ nặng trên event-loop. Nếu các dependency chỉ có API blocking, MVC là lựa chọn an toàn hơn hoặc phải cô lập công việc blocking trên scheduler phù hợp; điều đó không biến dependency thành non-blocking.
- **Timeout:** đặt timeout riêng cho từng downstream theo SLA, cùng deadline tổng cho dashboard. Giá trị cụ thể cần dựa trên SLA và đo đạc thực tế.
- **Fallback:** xác định theo nghiệp vụ. Có thể trả dashboard một phần nếu một dịch vụ phụ (ví dụ Benefits) lỗi; nếu thiếu Payroll hoặc Tax khiến số liệu không đáng tin cậy, trả lỗi thay vì tạo kết quả sai.
- **Concurrency limit:** giới hạn số request đồng thời và concurrency khi gọi downstream; cấu hình connection pool, rate limit và bulkhead để một dependency chậm không làm cạn tài nguyên. Chỉ retry lỗi tạm thời với giới hạn, backoff và trong deadline tổng.

## Kết luận

| API | Lựa chọn | Lý do chính |
|---|---|---|
| `GET /employees/{id}` | Spring MVC | JPA/JDBC blocking, traffic vừa phải, có cache |
| `GET /payroll-dashboard` | WebFlux nếu toàn bộ I/O là non-blocking; nếu không, ưu tiên MVC hoặc cô lập phần blocking | Nhiều remote call chậm, độc lập và có thể chạy song song |

Lựa chọn dựa trên workload và tính chất blocking/non-blocking của toàn bộ dependency, không phải vì WebFlux mặc định nhanh hơn.
