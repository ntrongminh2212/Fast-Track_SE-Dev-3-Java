# Week 7 — Day 6: Giám sát và hiệu năng

Tài liệu tập trung vào logs, metrics, traces, dashboard, cảnh báo và phân tích sự cố Inventory chậm. Không thay đổi mã ứng dụng.

- [Thiết kế observability](observability-design.md)
- [Thiết kế dashboard](dashboard-design.md)
- [Cảnh báo và runbook](alert-runbook.md)
- [Phân tích diễn tập sự cố](incident-analysis.md)
- [Metric tùy chỉnh](custom-metrics.md)
- [Mẫu cấu hình Actuator](application-observability.yml)

## Trạng thái trong project hiện tại

Order Service đã có Spring Boot Actuator, Prometheus registry, endpoint health/metrics/prometheus, histogram HTTP và tag `application`. Gateway truyền `X-Correlation-Id`. Tracing phân tán/exporter, metric nghiệp vụ, PostgreSQL, Kafka, Outbox và DLQ chưa được tích hợp. Các phần đó trong tài liệu là thiết kế cho bước phát triển tiếp theo, không phải tính năng đã chạy.
