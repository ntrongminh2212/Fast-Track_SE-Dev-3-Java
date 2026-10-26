# Hợp đồng thông điệp OrderConfirmed

**Dịch vụ phát sự kiện:** dịch vụ Order  
**Dịch vụ nhận và xử lý sự kiện:** dịch vụ Notification  
**Đích nhận:** `order.events`  
**Loại sự kiện:** `OrderConfirmed`

## Ví dụ cấu trúc sự kiện

```json
{
  "eventId": "evt-001",
  "eventType": "OrderConfirmed",
  "aggregateId": "order-1001",
  "aggregateType": "Order",
  "occurredAt": "2026-10-26T12:00:00Z",
  "schemaVersion": 1,
  "payload": {
    "customerId": "customer-42",
    "email": "customer@example.test",
    "total": 120.50,
    "currency": "USD"
  }
}
```

## Quy tắc

- Bắt buộc có `eventId`, `eventType`, thông tin thực thể nghiệp vụ (aggregate), dấu thời gian, phiên bản lược đồ dữ liệu và `payload`.
- `eventId` phải duy nhất trên toàn hệ thống và được giữ nguyên khi gửi lại hoặc phát lại sự kiện.
- Dịch vụ Notification chỉ gửi tối đa một thông báo cho mỗi `eventId` (xử lý idempotent, tức là an toàn khi nhận trùng).
- Khi có thể, chỉ bổ sung thay đổi tương thích ngược. Thay đổi không tương thích cần phiên bản lược đồ dữ liệu mới và kế hoạch triển khai phối hợp.
- Không đưa bí mật hoặc dữ liệu cá nhân không cần thiết vào sự kiện.
