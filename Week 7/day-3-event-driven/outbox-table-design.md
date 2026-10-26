# Transactional Outbox

## Vì sao cần Outbox?

Ghi DB rồi publish Kafka là hai thao tác riêng: DB có thể commit nhưng publish thất bại, hoặc event được publish dù DB rollback. Outbox ghi thay đổi nghiệp vụ và event trong **cùng một DB transaction**.

```text
BEGIN
  INSERT/UPDATE orders
  INSERT outbox_events (event_id, aggregate_id, event_type, payload, created_at)
COMMIT

Publisher/CDC đọc outbox → Kafka → consumers
```

## Bảng đề xuất

| Cột | Ý nghĩa |
|---|---|
| `event_id` | Khóa duy nhất của event |
| `aggregate_type`, `aggregate_id` | Entity phát sinh event |
| `event_type`, `schema_version` | Loại và phiên bản schema |
| `payload` | JSON event envelope/payload |
| `created_at` | Thời điểm tạo |
| `published_at` | Thời điểm publish thành công; null nếu còn chờ |
| `attempts`, `last_error` | Hỗ trợ vận hành/retry |

Publisher lấy bản ghi chưa publish theo lô, gửi Kafka rồi cập nhật `published_at`. Crash sau khi publish nhưng trước khi cập nhật có thể gửi trùng, vì vậy consumer vẫn phải idempotent. Có thể dùng CDC (ví dụ Debezium) thay cho polling publisher.
