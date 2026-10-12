# Những thay đổi nào là breaking change.

| Provider change | Breaking? | Lý do | 
| ------------- | --------- | ---------- | 
| Thêm field **description** | No | Thừa field consumer không dùng tới nên không gây lỗi | 
| Đổi name thành productName | Yes | Vì consumer đang get field product.name nên đổi sẽ gây Null | 
| Đổi **price** từ number sang string| Yes | Thay đổi kiểu dữ liệu sẽ khiến việc tính giá order bị lỗi | 
| Xóa available| Yes | Consumer cần field này để check lỗi product còn tồn kho, nếu xóa business sẽ sai | 
| Thêm optional field|  No | Thừa field consumer có thể dùng hoặc không nên không gây lỗi | 
| Đổi status code 200 → 201|  Yes | Consumer check status trả về, nếu không phải 200 thì trả lỗi -> sai business | 
| Đổi status code 404 → 400|  Yes | Consumer check status trả về, nếu không phải 404 thì không thể response cho client đúng lỗi và message kỳ vọng -> sai business | 