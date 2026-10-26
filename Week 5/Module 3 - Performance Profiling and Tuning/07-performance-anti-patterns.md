# 07 — Performance Anti-patterns trong Spring/Hibernate

## Đoạn code cần phân tích

```java
public List<EmployeeDashboardResponse> getDashboard(String tenantId) {
    List<Employee> employees = employeeRepository.findByTenantId(tenantId);

    return employees.stream()
        .map(employee -> {
            Salary salary = salaryRepository.findLatestByEmployeeId(employee.getId());
            TaxProfile tax = taxClient.getTaxProfile(employee.getId());
            return new EmployeeDashboardResponse(employee, salary, tax);
        })
        .toList();
}
```

## Anti-pattern và rủi ro

- **N+1 query DB:** truy vấn employee một lần, sau đó gọi `findLatestByEmployeeId` cho từng employee. Với `N` employees, tối thiểu khoảng `1 + N` truy vấn (không tính các truy vấn phụ).
- **N+1 remote call:** `taxClient.getTaxProfile` được gọi riêng cho mỗi employee, tức `N` HTTP request đồng bộ.
- **Tenant có 10,000 employees:** có thể tải toàn bộ entity vào heap, phát sinh khoảng 10,001 truy vấn DB và 10,000 remote calls; latency, connection/thread usage và tải lên Tax Service tăng mạnh. Payload lớn, timeout hoặc lỗi giữa chừng cũng làm kết quả khó xử lý.
- **Nếu dùng WebFlux:** repository JPA/JDBC vẫn blocking; `taxClient` nếu là client đồng bộ (ví dụ `RestTemplate`) cũng blocking. Đặt chúng trong reactive chain không biến chúng thành non-blocking và có thể chiếm event-loop.

## Refactor / optimization strategy

1. **Đo baseline trước:** dùng tracing/APM, query count, DB slow query log và load test để xác định phần tốn thời gian; ghi lại p95/p99, throughput, lỗi và mức sử dụng tài nguyên.
2. **Phân trang:** không lấy toàn bộ nhân viên cùng lúc. Dùng pagination/cursor và giới hạn kích thước trang; thiết kế API để client có thể lấy tiếp.
3. **Batch lấy salary:** truy vấn latest salary cho toàn bộ employee ID trong trang bằng một batch query, hoặc dùng projection/window query phù hợp DB; thêm index phù hợp, ví dụ trên employee ID và thời điểm hiệu lực, rồi xác nhận bằng `EXPLAIN`.
4. **Batch Tax Service nếu hỗ trợ:** gửi danh sách employee IDs theo batch. Nếu không hỗ trợ batch, gọi song song có giới hạn concurrency, timeout và bulkhead; không tạo 10,000 request đồng thời.
5. **Cache có điều kiện:** cache Tax Profile hoặc dữ liệu salary chỉ khi chấp nhận được độ mới, có TTL/invalidation rõ ràng và key bao gồm tenant/phạm vi cần thiết để tránh rò dữ liệu giữa tenant.
6. **Chọn stack đúng:** với JPA/JDBC và client blocking, giữ Spring MVC có thể hợp lý. Nếu chuyển WebFlux, dùng R2DBC/reactive repository và `WebClient` non-blocking xuyên suốt; nếu không thể thay blocking dependency, cô lập chúng khỏi event-loop và giới hạn tài nguyên.
7. **Giảm dữ liệu trả về:** dùng DTO/projection chỉ lấy trường cần thiết; tránh serialize entity graph hoặc payload quá lớn.
8. **Giữ transaction ngắn:** không giữ transaction DB mở trong lúc chờ HTTP call đến Tax Service. Tách bước đọc dữ liệu DB và gọi remote service theo thiết kế phù hợp.

## Metrics cần theo dõi sau fix

- API dashboard: request rate, throughput, p50/p95/p99 latency, timeout/error rate.
- Database: số query trên mỗi request, query latency, slow query count, rows scanned/returned, connection pool active/pending và DB CPU.
- Tax Service/client: số request trên mỗi dashboard request, latency, timeout/error/retry rate, in-flight calls, connection pool và rate limit.
- Ứng dụng: CPU, heap sau GC, allocation/GC pause, thread count, thread pool queue/rejection, event-loop lag nếu dùng WebFlux.
- Kiểm tra theo tenant/page size và tải đại diện; so sánh với baseline để xác nhận hiệu quả, tính đúng đắn và không tạo bottleneck mới.
