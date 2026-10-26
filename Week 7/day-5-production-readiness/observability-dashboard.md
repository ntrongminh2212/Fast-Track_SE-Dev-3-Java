# Thiết kế dashboard giám sát

## Độ tin cậy checkout

- Tỷ lệ checkout thành công và tổng request theo thời gian.
- Tỷ lệ HTTP 4xx/5xx; phân biệt lỗi client và lỗi hệ thống.
- Độ trễ p50/p95/p99 cho checkout.
- Số order đang chờ xử lý và order bị kẹt ở `PAYMENT_PROCESSING`.

## Dependency và tài nguyên

- Độ trễ/lỗi Inventory, Payment; trạng thái circuit breaker.
- Số luồng và hàng đợi bulkhead đang sử dụng/bị từ chối.
- JVM heap, GC pause, CPU, memory và thread saturation.
- Connection pool DB, số kết nối đang dùng và chờ.

## Event và đối soát (khi có tích hợp)

- Consumer lag và số message ở DLQ.
- Số bản ghi Outbox chưa publish và tuổi bản ghi lâu nhất.
- Số trường hợp payment thành công nhưng order chưa xác nhận.
- Số lần ngăn thanh toán trùng.

Gắn nhãn service/environment và dùng correlation ID để nhảy từ metric cảnh báo sang log/trace. Tránh dùng ID người dùng hoặc order làm nhãn metric có độ đa dạng quá cao.
