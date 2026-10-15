# **Assignment**

Tạo `02-jvm-diagnostics.md` cho 2 scenario:

## **Scenario A — Memory leak**

`Payroll export tăng memory từ 1GB lên 6GB.
Sau GC, heap vẫn gần đầy.
Cuối cùng service OOM.`

### 1. Lấy artifact nào trước: 
lấy heap dump trước, có thể dùng lệnh sau:
    `jcmd <PID> GC.heap_dump /path/to/heapdump.hprof`
PID: Nếu test local có thể lấy PID bằng cách bật window Task Manager -> tab Details -> PID của JVM đang chạy app

### 2. Quan sát gì trong artifact: 
Cần check class histogram xem class nào được dùng nhiều, xem Dominator Tree object nào đang chiếm bộ nhớ lớn, cần điều tra Path to GC Roots để biết lý do tại sao GC không giải phóng object, có thể là vẫn đang dùng nên không thể xóa, xem Retained Heap để biết nếu loại bỏ referece thì tiết kiệm được bao nhiêu bộ 

### 3. Root cause hypothesis: 
Có thể khi export data payroll đang giữ lại collection data cho đến khi xử lý hết toàn bộ mới write xuống file export, cần xử lý theo chunk n - làm đến đâu write vào file đến đấy, rồi clear collection data n record để thực hiện chunk n tiếp theo cho đến hết toàn bộ 

### 4. Mitigation ngắn hạn và fix dài hạn.
Ngắn hạn: 
- Giảm kích thước mỗi đợt xử lý dữ liệu (batch size hoặc chunk size).
- Giới hạn số tác vụ xuất dữ liệu chạy song song.
- Tăng heap size.

Dài hạn:
- Đọc dữ liệu từ database theo từng chunk có kích thước giới hạn.
- Xử lý từng chunk thay vì tải toàn bộ dữ liệu vào một collection.

## **Scenario B — Thread starvation**

`API chậm nhưng CPU chỉ 30%.
Thread pool 200 threads đều đang chờ HTTP call đến Tax Service.`

### 1. Lấy artifact nào trước.
Lấy Thread dump trước, có thể dùng lệnh sau 
    `jcmd <PID> Thread.print > thread_dump.txt`
PID: Néu test local có thể lấy PID bằng cách bật window Task Manager -> tab Details -> PID của JVM đang chạy app

### 2. Quan sát gì trong artifact.
Cần quan sát state của 200 threads và class stack trace hiện tại xem đang stuck ở chỗ nào, nếu đang chờ HTTP call đến Tax Service thì chắc chắn các threads đang chờ ở service call Tax Service tương ứng

### 3. Root cause hypothesis.
Tax Service có thể đang gặp traffic lớn khiến nhất thời không phản hồi kịp, mỗi request tới ứng dụng gọi tới Tax Service sẽ chiếm 1 thread, trong trường hợp thời gian chờ kéo dài sẽ khiến toàn bộ 200 thread bị chiếm dụng

### 4. Mitigation ngắn hạn và fix dài hạn.
Ngắn hạn:
- Cấu hình timeout khi request Tax Service
- Giảm số lần retry
- Giới hạn số request call tới Tax Service để tránh chiếm dụng thread.

Dài hạn:
- Circuit Breaker tạm ngưng service khi tỷ lệ lỗi vượt ngưỡng cho phép.

## Checklist “khi nào lấy heap dump, khi nào lấy thread dump”.

| Dấu hiệu quan sát được | Artifact ưu tiên | Mục đích |
|---|---|---|
| Heap tăng liên tục, sau GC vẫn gần đầy | Heap Dump | Tìm object chiếm bộ nhớ và nguồn tham chiếu giữ object |
| Service gặp `OutOfMemoryError: Java heap space` | Heap Dump, nếu còn khả năng thu thập an toàn | Điều tra nguyên nhân OOM |
| API chậm nhưng CPU thấp | Thread Dump | Xác định thread đang chờ ở đâu |
| Nhiều thread chờ HTTP/RPC call | Thread Dump | Xác định dependency hoặc vị trí I/O đáng nghi |
| Thread pool bị cạn | Thread Dump + metrics của thread pool | Tìm vị trí các thread bị chiếm dụng và nguyên nhân |
| Nhiều thread ở trạng thái `BLOCKED` | Thread Dump | Điều tra tranh chấp monitor lock |
| Nghi ngờ deadlock | Thread Dump | Tìm chu trình chờ lock giữa các thread |
| CPU cao bất thường | Thread Dump và JFR | Xác định thread hoạt động nhiều; dùng JFR để điều tra CPU profiling |
| API chậm nhưng chưa rõ nguyên nhân | Thread Dump trước, sau đó bổ sung metrics/JFR tùy dấu hiệu | Khoanh vùng thread đang chạy hay chờ |

## Một runbook ngắn gồm lệnh chẩn đoán an toàn.
- Liệt kê PID: `jcmd -l`
- Heap dump: `jcmd <PID> GC.heap_dump /path/to/heapdump.hprof`
- Thread dump: `jcmd <PID> Thread.print > thread_dump.txt`