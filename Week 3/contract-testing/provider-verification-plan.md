# Provider Verification Plan 
Tất cả các field phải tồn tại, phải đúng kiểu dữ liệu được yêu cầu, phải đúng tên field, HTTP status phải đúng với mô tả như dưới đây.

## 1. Trường hợp 200 OK 

Request: GET /api/products/P-100 

Expected: Status 200 OK 

Required fields: 

- id -> string
- name -> string
- price -> double
- available -> boolean

--- 

## Trường hợp 404 Not Found 

Request: GET /api/products/P-999 

Expected: Status 404 

Response:
```
{ 
    "code": "PRODUCT_NOT_FOUND", 
    "message": "Product P-999 not found" 
} 
```