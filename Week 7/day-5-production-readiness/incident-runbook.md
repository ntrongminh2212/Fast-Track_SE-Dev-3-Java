# Runbook diễn tập sự cố

## Tình huống: Inventory phản hồi chậm

1. Xác nhận cảnh báo: tỷ lệ checkout thành công, p95/p99, 5xx, độ trễ Inventory và circuit breaker.
2. Chọn một request lỗi, lần theo correlation ID qua Gateway → Order → Inventory; kiểm tra timeout, retry và bulkhead.
3. Giảm tác động: để timeout ngắn, ngắt/mở circuit khi cần, giới hạn lưu lượng checkout hoặc tạm tắt chức năng phụ thuộc Inventory. Không retry liên tục không giới hạn.
4. Kiểm tra các request GET order có còn đáp ứng không; xác nhận bulkhead đã cô lập luồng gọi Inventory.
5. Khôi phục dần, theo dõi tỷ lệ thành công và độ trễ sau khi dependency ổn định.
6. Ghi timeline, phạm vi ảnh hưởng, nguyên nhân, hành động và việc cần làm sau sự cố.

## Checklist điều tra

- Mốc thời gian bắt đầu/kết thúc và thay đổi triển khai gần nhất.
- Chỉ số trước/trong/sau sự cố; log và trace có correlation ID.
- Ảnh hưởng khách hàng, order/payment cần đối soát.
- Người phụ trách cập nhật trạng thái và người quyết định rollback/mitigation.

Nếu gặp tình huống thanh toán đã trừ tiền nhưng order chưa xác nhận: không tự động charge lại; đối soát theo idempotency key và trạng thái nhà cung cấp thanh toán trước khi xử lý bù.
