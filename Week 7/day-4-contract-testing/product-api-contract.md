# Hợp đồng API sản phẩm

**Dịch vụ tiêu thụ:** Order Service  
**Dịch vụ cung cấp:** Product Service  
**Thao tác:** `GET /products/{id}`

## Yêu cầu

```http
GET /products/P-100
Accept: application/json
```

## Phản hồi thành công — `200 OK`

```json
{
  "id": "P-100",
  "name": "Keyboard",
  "price": 120.50,
  "available": true
}
```

## Quy tắc hợp đồng

- `id`: chuỗi, khớp với mã sản phẩm được yêu cầu.
- `name`: chuỗi không được để trống.
- `price`: số JSON lớn hơn hoặc bằng 0.
- `available`: giá trị boolean bắt buộc.
- Dịch vụ cung cấp có thể thêm trường tùy chọn, nhưng không được xóa, đổi tên hoặc đổi kiểu của trường bắt buộc nếu chưa có kế hoạch chuyển đổi phiên bản.
- Nếu không tìm thấy sản phẩm, API trả `404`; cần định nghĩa và giữ ổn định cấu trúc phản hồi lỗi.

Kiểm thử xác minh dịch vụ cung cấp phải thất bại nếu thiếu `available` hoặc trường bắt buộc khác, hay nếu kiểu dữ liệu của trường bị thay đổi.
