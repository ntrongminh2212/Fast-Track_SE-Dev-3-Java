### **Assignment**

#### 1. Giải thích heap, stack, GC pause, allocation rate.
- Heap: bộ nhớ chứa các object đã được cấp phát, gồm Young gen (Object mới được cấp phát), Old gen (các Object sống lâu hơn được promote từ young gen)
- Stack: có stack frame chứa các lời gọi hàm, các reference tới object trong heap, các biến local kiểu nguyên thủy
- GC pause: khoảng thời gian dừng application threads để GC chạy thực hiện task như marking và thu hồi bộ nhớ
- Allocation rate: tốc độ cấp phát bộ nhớ cho object, thường được biểu diễn bằng dung lượng trên thời gian (MB/s, GB/s)

#### 2. So sánh G1 và ZGC:

| So sánh | G1 | ZGC |
|---------|----|-------|
| Mục tiêu | Cân bằng giữa pausetime, throughput và tài nguyên hệ thống | GCPausetime có độ trễ thấp nhất có thể, xử lý được heap size data rất lớn màkhông làm tăng GC pausetime |
| Trade-off | Data càng lớn, pausetime càng cao, không phù hợp với hệ thốngscale up data heap lớn | Vì cho phép việc marking, di chuyển object và dọndẹp vùng nhớ chạy parallel với application nên sẽ tạo gánh nặng cho CPU khiapp vừa allocate object vừa chạy GC ở dưới  |
| Khi nào cần benchmark thay vì đoán.| Trường hợp heap lớn, allocation ratecao hoặc GC có pause dài làm latency tăng thì có thể benchmark để dùng loạiGC phù hợp |
    
#### 3. Phân tích tình huống: 

`Payroll API có p99 tăng từ 400ms lên 8 giây.
Application CPU chỉ 45%.
Heap usage saw-tooth liên tục.
Full GC xuất hiện nhiều lần trong giờ cao điểm.`

Mentee phải nêu:

- Hypothesis ban đầu: Heap usage saw-tooth liên tục và Full GC xuất hiện nhiều lần trong giờ cao điểm thể hiện rằng Payroll API có thể đang bị allocation presure làm GC phải chạy liên tục để thu hồi bộ nhớ, dẫn đến việc GC pausetime nhiều làm tăng latency của Payroll API.
- Metric/log cần xem thêm:
    - GC Logs: kiểm tra tần suất và thời gian chạy của gc, pausetime
    - Heap metric: kiểm tra heap used và allocation rate vào giờ cao điểm, các object và class tạo nhiều vào giờ cao điểm
    - JFR