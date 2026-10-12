# Product Service Contract 

## Request 

- Method: GET 
- URL: /api/products/{productId} 
- Example: GET /api/products/P-100 

## Status code

- 200 OK 
    
    Required Fields: 

    | Field | Required | Type | 
    |---------|---------|---------| 
    | id | Yes | string | 
    | name | Yes | string | 
    | price | Yes | double | 
    | available | Yes | boolean | 

    Body Example: 

    ```
    { 
        "id": "P-100", 
        "name": "Mechanical Keyboard", 
        "price": 120.50, 
        "available": true 
    }
    ```
- 404 Not Found 

    Required Fields: 

    | Field | Required | Type | 
    |---------|---------|---------| 
    | errorCode | Yes | string | 
    | message | Yes | string | 

    Body Example: 
    ```
    { 
        "errorCode": "E0001", 
        "message": "Product P-100 not found" 
    }
    ```