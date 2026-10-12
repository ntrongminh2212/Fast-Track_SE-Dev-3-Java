# Consumer Expectations 

Consumer: Order Service 

Order Service cần được cung cấp response sau từ Product Service: 

1. HTTP 200 khi có sản phẩm. 
  - id -> string 
  - name -> string 
  - price -> double
  - available -> boolean   
    
    VD:
    ```
    { 
      "id": "P-100", 
      "name": "Mechanical Keyboard", 
      "price": 120.50, 
      "available": true 
    }
    ```
2. HTTP 404 khi sản phẩm không tồn tại . 
  - errorCode -> string
  - message -> string
    
    VD:
    ```
    { 
        "errorCode": "E0001", 
        "message": "Product P-100 not found" 
    }
    ```