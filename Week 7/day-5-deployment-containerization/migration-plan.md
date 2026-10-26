# Kế hoạch migration trạng thái Order

Mục tiêu: chuyển từ `PENDING`, `CONFIRMED`, `CANCELLED` sang các trạng thái chi tiết hơn mà bản cũ và mới có thể cùng chạy trong rolling deployment.

## Giai đoạn 1 — Mở rộng (Expand)

- Thêm cột/giá trị trạng thái mới, không xóa hoặc đổi tên cột cũ.
- Giữ mặc định và ràng buộc tương thích với bản đang chạy.
- Kiểm tra migration trên bản sao dữ liệu và chuẩn bị backup.

## Giai đoạn 2 — Tương thích

- Triển khai code có thể đọc trạng thái cũ và mới.
- Nếu cần, ghi song song trạng thái mới và cũ trong thời gian chuyển tiếp.
- Đảm bảo bản cũ không đọc nhầm giá trị mới; không ghi trạng thái mà bản cũ không xử lý được khi rollback còn cần thiết.

## Giai đoạn 3 — Backfill và kiểm tra

- Chuyển các order cũ theo lô nhỏ, có thể tiếp tục hoặc chạy lại an toàn.
- Đối chiếu số lượng bản ghi, trạng thái, thời gian cập nhật và order đang xử lý.
- Theo dõi lỗi, dữ liệu không hợp lệ và order bị kẹt; dừng backfill nếu sai lệch tăng.

## Giai đoạn 4 — Cutover

- Canary code mới, chuyển dần luồng đọc sang state model mới.
- Theo dõi success rate, payment/order mismatch, trạng thái không được hỗ trợ và khả năng rollback.
- Giữ feature flag để quay lại logic cũ nếu cần.

## Giai đoạn 5 — Thu hẹp (Contract)

- Sau thời gian rollback đã thống nhất, xác nhận mọi instance chỉ dùng schema mới.
- Xác minh backup, migration và dữ liệu đã đối soát.
- Trong release riêng, xóa cột/logic cũ; không gộp thao tác phá hủy schema vào đợt triển khai đầu.

## Rollback

Rollback image chỉ an toàn khi code cũ đọc được schema và dữ liệu mới. Nếu dữ liệu mới không tương thích, tắt feature flag hoặc dùng kế hoạch sửa dữ liệu đã kiểm thử; không tự động drop schema hoặc charge lại thanh toán.
