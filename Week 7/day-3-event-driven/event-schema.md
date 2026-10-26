# Event schema — OrderConfirmed

**Command** yêu cầu thực hiện việc gì (ví dụ `ReserveInventory`, `ChargePayment`). **Event** mô tả việc đã xảy ra (ví dụ `OrderConfirmed`) và có thể được nhiều consumer xử lý độc lập.

Envelope mẫu:

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
    "total": 49.99,
    "currency": "USD"
  }
}
```

- `eventId` duy nhất, dùng nhận biết message trùng.
- `eventType` và `schemaVersion` giúp consumer diễn giải phiên bản event.
- `aggregateId`/`aggregateType` xác định entity phát sinh event.
- `occurredAt` dùng để audit và sắp xếp theo thời gian.
- `payload` chỉ chứa dữ liệu cần thiết cho consumer; không đưa secret vào event.

Spring Cloud Stream dùng binding để nối function `Supplier`, `Function`, `Consumer` tới destination/topic và consumer group.
