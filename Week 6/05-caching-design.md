# 05 — Caching Design

Sử dụng **Redis Distributed Cache** dùng chung cho 5 microservices. Mỗi service quản lý cache namespace riêng, không truy cập hoặc ghi đè cache của service khác nếu không có quy ước rõ ràng.

| **Data** | **Cache hay không** | **Strategy** | **TTL** | **Invalidation** | **Stale acceptable?** |
| -------- | ------------------- | ------------ | ------- | ---------------- | --------------------- |
| EmployeeInfo | Yes | Cache-Aside | 30 ngày | Xóa cache khi EmployeeInfo được cập nhật | Có, trong thời gian ngắn, riêng việc tính payroll sẽ không lấy data từ cache mà lấy trực tiếp từ DB |
| Job | Yes | Cache-Aside | 365 ngày | Xóa cache khi Job được cập nhật | Có |
| Company / Tenant Info | Yes | Cache-Aside | 365 ngày | Xóa cache khi Tenant được cập nhật | Có, trừ TenantStatus |
| Payslip đã hoàn tất | Yes | Cache-Aside | 365 ngày | Xóa cache khi payslip bị điều chỉnh hoặc thay thế | Không, phải đảm bảo trả đúng phiên bản payslip |
| SalaryProfile | Yes | Cache-Aside | 365 ngày | Xóa cache khi SalaryProfile được tạo/cập nhật | Có, riêng việc tính payroll sẽ không lấy data từ cache mà lấy trực tiếp từ DB |
| IncomePolicy | Yes | Cache-Aside | 365 ngày | Xóa cache khi policy được tạo/cập nhật | Có, riêng việc tính payroll sẽ không lấy data từ cache mà lấy trực tiếp từ DB |
| DeductionPolicy | Yes | Cache-Aside | 365 ngày | Xóa cache khi policy được tạo/cập nhật | Có, riêng việc tính payroll sẽ không lấy data từ cache mà lấy trực tiếp từ DB |
| LeaveBalance | Yes | Cache-Aside | 30 ngày | Xóa cache khi tạo, duyệt hoặc hủy LeaveReq ngắn, khi Grant Quota | Không khi duyệt nghỉ phép |
| AbnormalCase | Yes | Cache-Aside | 30 ngày | Xóa cache khi batch cập nhật hoặc xử lý AbnormalCase | Không khi tính payroll |

