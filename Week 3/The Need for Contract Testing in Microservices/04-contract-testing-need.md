### **Assignment**

Tạo file `04-contract-testing-need.md`.

cần phân tích:

`Order Service phụ thuộc vào Product Service và Payment Service.`

Viết bảng:

| **Dependency** | **Consumer expectation** | **Nếu provider đổi sai** | **Unit test có phát hiện không?** | **E2E test có hạn chế gì?** |
| --- | --- | --- | --- | --- |
| Product Service | productId: int,</br> productName: String,</br> price: double,</br> stock: int | Thiếu field, sai type sẽ khiến consummer xử lý bị lỗi, tính toán sai | Không thể phát hiện vì Munit sẽ mock response giả định từ provider nên không thể phát hiện data sai | Chậm và không thế phát hiện sớm nguyên nhân lỗi |
| Payment Service | status: int,</br> paymentMessage: String | Thiếu field, sai type sẽ khiến consummer xử lý bị lỗi, tính toán sai | Không thể phát hiện vì Munit sẽ mock response giả định từ provider nên không thể phát hiện data sai | Chậm và không thế phát hiện sớm nguyên nhân lỗi |

Sau đó trả lời:

1. Vì sao mock-based unit test không đủ?
- Mock-based unit test sẽ mock data để giả định response trả về từ provider, nên chỉ có thể cover được nghiệp vụ của service hiện tại, trong trường hợp provider thay đổi data trả về (xóa field, đổi type) thì Mock-based unit test vẫn pass với bộ data mock cũ.
2. Vì sao chỉ dùng end-to-end test không đủ?
- End-to-end test có khả năng đi toàn bộ luồng business tuy nhiên nó phụ thuộc vào môi trường nhiều server cùng chạy, trong trường hợp phát hiện lỗi cũng mất thời gian đi tracing bug mà không chắc nguyên nhân đã là do provider sai data, có thể là lỗi khác.
3. Contract test đứng ở đâu trong testing pyramid/microservice test strategy?
- Contract test sẽ là một tầng riêng nằm ở trên Unit test, vì contract test tập trung vào khía cạnh giao tiếp giữa các server với nhau mà không có test strategy nào khác thay thế được, và nó cũng không thay thế công việc của các testing strategy khác.

Sequence diagram: Order Service gọi Product Service.
- [Sequence diagram](./SequenceDiagram.drawio.drawio.png)

Một ví dụ contract breaking change.
- Ví dụ trường hợp Product Service đang có response GET Product byproductId như sau:
   
    ```
    {
        "productId": 101,
        "productName": "Keyboard",
        "price": 3.5,
        "stock": 20
    }
- Sau đó bị thay đổi field price thành String và có định dạng tiền tệnhư sau:
   
    ```
    {
        "productId": 101,
        "productName": "Keyboard",
        "price": "3.5 USD",
        "stock": 20
    }
- Trường hợp Order service dùng trực tiếp price * quantity để tính tiềnthì sẽ bị lỗi 500 String * Int -> contract breaking change