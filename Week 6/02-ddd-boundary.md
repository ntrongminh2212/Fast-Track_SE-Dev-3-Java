## 1. Time Management Context: 
### 1.1. Bounded Context: 
- WorkingTime: employeeCd, startDateTime, endDateTime. 
- LeaveBalance: employeeCd, leaveType, year, remainQuota, grantQuota. 
- LeaveRequest: employeeCd, leaveType, leaveDate, period(morning, afternoon, all day), approveStatus, approverId. 
- AbnormalCase: employeeCd, abnormalType, periodLost, date. 
### 1.2. Ubiquitous Language: 
| Thuật ngữ | Ý nghĩa | 
|-----------|---------| 
| WorkingTime | Định nghĩa 1 khoảng thời gian của nhân viên từ lúc vào office (startDateTime), cho tới khi rời khỏi office (endDateTime) | 
| LeaveBalance | Các loại ngày nghỉ phép được cấp cho nhân viên theo năm và quota còn lại | 
| LeaveRequest | Yêu cầu nghỉ phép của một nhân viên trong một khoảng thời gian  | 
| Approve | Hành động phê duyệt một yêu cầu nghỉ phép bởi người có thẩm quyền. | 
| Cancel | Hành động hủy một yêu cầu nghỉ phép của nhân viên hoặc người có thẩm quyền | 
| Grant Quota | Số ngày nghỉ phép được hệ thống cấp cho nhân viên theo năm | 
| Remain Quota | Hạn mức còn lại theo quy tắc tính quota của hệ thống | 
| AbnormalCase | Trường hợp nhân viên nghỉ không xin phép, không có thông tin WorkingTime trong một buổi hoặc cả ngày mà không có LeaveRequest tương ứng | 
| abnormalType | Trường hợp nghỉ không lý do gồm ALL_DAY_LOST, MORNING_LOST, AFTERNOON_LOST | 
### 1.3. Aggregate Root and Invariant 
#### Aggregate Root: LeaveRequest 
- Khi đã có approver confirm request nghỉ phép thì không thể cancel. 
- Mỗi yêu cầu nghỉ phép phải thuộc về một nhân viên hợp lệ trong cùng tenant. 
- Không được tạo yêu cầu nghỉ phép làm vượt hạn mức Remain Quota. 
- Khi tạo request nghỉ phải trừ remainQuota (all day trừ 1 quota, morning/afternoon trừ 0.5 quota). 
- Khi cancel request nghỉ phải cộng lại remainQuota tương ứng 
- Trạng thái phê duyệt chỉ được thay đổi bởi người có thẩm quyền. 
### Aggregate Root: WorkingTime 
- Phải ghi lại thời gian mỗi lần nhân viên vào và ra office dưới dạng yyyyMMddHHmmss 
- 01:00 AM mỗi ngày sẽ chạy batch tự động rà soát lại thời gian làm việc của nhân viên ngày trước đó và ghi lại trường hợp AbnormalCase 
- Nếu nhân viên không có đủ 3 giờ WorkingTime trong ngày từ 8h đến 17h và không có đơn xin nghỉ phép cả ngày được confirm, sẽ bị tính abnormalType ALL_DAY_LOST và periodLost là 1 
- Nếu nhân viên không có đủ 3 giờ WorkingTime trong buổi sáng từ 8h đến 13h và không có đơn xin nghỉ phép buổi sáng được confirm, sẽ bị tính abnormalType MORNING_LOST và periodLost là 0.5 
- Nếu nhân viên không có đủ 3 giờ WorkingTime trong buổi chiều từ 13h đến 18h và không có đơn xin nghỉ phép buổi chiều được confirm, sẽ bị tính abnormalType AFTERNOON_LOST và periodLost là 0.5 
- Có thể một nhân viên sẽ bị cả MORNING_LOST và AFTERNOON_LOST nhưng vẫn đủ 3h WorkingTime cả ngày, lúc này chỉ tính là MORNING_LOST 
- HR manager phải confirm/reject đơn nghỉ phép trước ngày 1 của tháng kế tiếp, trường hợp đơn nghỉ được confirm hệ thống sẽ xóa AbnormalCase của buổi/ngày tướng ứng cho nhân viên 
## 2. Payroll Context: 
### 2.1. Bounded Context: 
- SalaryProfile: salaryProfileId, employeeCd, companyCd, baseSalary, effectiveFrom, effectiveTo 
- DeductionPolicy: deductionCd, calculationType (FIXED_AMOUNT, PERCENTAGE), calculationValue 
 + PersonalIncomeTaxPolicy: calculationType phần trăm tính lũy tiến theo lương cơ bản theo luật lao động 
 + SocialInsurancePolicy: calculationType phần trăm theo luật 
 + HealthInsurancePolicy: calculationType phần trăm theo luật 
 + CompanyUnionPolicy: calculationType fix cứng khác nhau theo công ty 
- TMSViolationPenalty: payslipId, calculationBase, totalDayLost, penaltyAmount 
- IncomePolicy: incomeCd, calculationType (FIXED_AMOUNT, PERCENTAGE), calculationValue, effectiveFrom, effectiveTo 
- DeductionPoliciesApply: payslipId, deductionCd, calculationBase, calculationType, calculationValueApplied, deductionAmount 
- IncomePoliciesApply: payslipId, incomeCd, calculationBase, calculationType, calculationValueApplied, incomeAmount 
- Payslip: payslipId, salaryProfileId, payrollRunId, payPeriod, deductionPoliciesApply, incomePoliciesApply, tmsViolationPenalty, run, totalDeduction, totalIncome, netSalary, grossSalary, runStatus 
- PayrollRun: payrollRunId, companyCd, payPeriod, totalEmployees, runDateTime, successCount, failureCount, runStatus 
### 2.2 Ubiquitous Language 
| Thuật ngữ | Ý nghĩa | 
|-----------|---------| 
| SalaryProfile | Lương và income của một nhân viên tại một thời điểm | 
| DeductionPolicy | Các khoản thuế phí sẽ trừ vào tiền lương của nhân viên, tùy vào loại thuế phí sẽ có phần trăm hoặc số tiền fix cần trả khác nhau theo luật nhà nước hoặc công ty | 
| IncomePolicy | Các khoản bonus được cộng vào tiền lương của nhân viên, tùy vào loại income sẽ có phần trăm hoặc số tiền fix cần trả khác nhau | 
| DeductionPoliciesApply | Deduction Policy được apply vào Payslip trong lần run hiện tại | 
| IncomePoliciesApply | Income Policy được apply vào Payslip trong lần run hiện tại | 
| TMSViolationPenalty | Phạt trừ lương dựa trên lương cơ bản và thời gian nghỉ vô lý do lấy từ AbnormalCase ở Time Management Context | 
| Payslip | Lương sẽ nhận của nhân viên trong một lần chạy (theo tháng) đã áp dụng các khoản income policy và deduction policy | 
| PayrollRun | Một lần chạy tính lương của một công ty trong một kỳ hạn (tháng) cho nhiều nhân viên | 
| runStatus | Kết quả chạy payroll lần này COMPLETED, COMPLETED_WITH_ERROR, FAILED | 
| baseSalary | Lương cơ bản của nhân viên | 
| incomeAmount | Kết quả income thưởng thêm cho nhân viên sau khi apply policy này | 
| deductionAmount | Kết quả deduction khấu trừ vào lương nhân viên sau khi apply policy này | 
| calculationType | Xác định khi apply Income hoặc Deduction Policy này sẽ được tính theo phần trăm hay fix cứng | 
| netSalary | Số tiền thực lãnh của nhân viên trong lần chạy này sau khi lấy baseSalary trừ đi các khoản DeductionPoliciesApply và cộng các khoản IncomePoliciesApply | 
| grossSalary | Số tiền chưa tính các khoản thuế phí | 
### 2.3 Angregate Root and Invariant 
#### Angregate Root: PayrollRun 
- companyCd phải xác minh có tồn tại từ Tennant Context 
- Kỳ hạn một lần chạy sẽ là 1 tháng/ một lần vào ngày mùng 1 mỗi tháng, payperiod phải có định dạng yyyy/MM 
- Trường hợp đã tồn tại PayrollRun với (companyCd, payPeriod) tương ứng dưới DB thì không thực hiện chạy lại
- Ghi lại thời gian chạy PayrollRun yyyyMMddHHmmss 
- Trong trường hợp lần chạy payroll run failed vì bất kỳ lỗi nào phải có khả năng chạy lại tiếp từ payslip lỗi trở đi, không chạy lại các payslip đã xong. 
- incomeAmount tính như sau: 
    + Với calculationType là percent: (base salary * calculationValue (số < 1)) 
    + Với calculationType là fix amount: calculationValue 
- Gross salary sẽ bằng base salary + (tất cả các incomeAmount) 
- TMSViolationPenalty được tính như sau: 
    + Call API lấy ra tất cả AbnormalCase của nhân viên tương ứng trong tháng từ Time Management Context 
    + totalDayLost sẽ bằng tổng periodLost có được 
    + penaltyAmount sẽ bằng: (baseSalary/(số ngày làm việc trong tháng chạy payroll)) * totalDayLost 
- deductionAmount tính như sau: 
    + Với calculationType là percent: gross salary * calculationValue (số < 1) 
    + Với calculationType là fix amount: calculationValue 
- netSalary sẽ bằng gross salary - (tất cả các deductionAmount) - penaltyAmount 
- Phải lấy đúng SalaryProfile có effectiveFrom, effectiveTo trong kỳ hạn payPeriod chạy của PayrollRun 
- Với mỗi hành động create/update lương liên quan đến SalaryProfile, DeductionPolicy, IncomePolicy cần publish event SalaryUpdate lên Kafka để Audit Context ghi lại
- Trong trường hợp payslip với (employeeCd, companyCd, payslip) tương ứng đã tồn tại dưới db với runStatus = COMPLETED, thì hiểu là payslip của nhân viên đã chạy cho tháng này nên skip qua payslip của nhân viên tiếp theo.
- Với mỗi payslip đã chạy xong runStatus = COMPLETED, cần publish event PayslipReady lên Kafka để Notification Context gửi mail.
## 3. Identity Context: 
### 3.1. Bounded Context: 
- EmployeeInfo: employeeCd, name, dateOfBirth, phoneNumber, nationalId, homeAddress, employmentStatus, joinDate, jobId, companyCd 
- Job: jobId, jobName, jobRank 
- User: userId, employeeCd, email, passwordHash, status 
- UserTenantRole: userId, companyCd, roleCd 
- Role: roleCd, roleName, description 
- RolePermission: roleCd, permissionCd 
- Permission: permissionCd, description 
### 3.2. Ubiquitous Language: 
| Thuật ngữ | Ý nghĩa | 
|-----------|---------| 
| EmployeeInfo | Thông tin cá nhân của một nhân viên và ngày vào công ty | 
| Job | Thông tin công việc và cấp bậc | 
| User | Thông tin tài khoản đăng nhập của nhân viên | 
| Role | Vai trò của một nhân viên là gì: EMPLOYEE, HR_MANAGER, AUDITOR, TENANT_ADMIN| 
| Permission | Các hành động cần cấp phép cho role làm trong hệ thống: EMPLOYEE_UPDATE, LEAVE_APPROVE, SALARY_READ, SALARY_UPDATE, PAYROLL_RUN| 
| RolePermission | Thể hiện các permission được cấp cho phép role làm  | 
### 3.3 Angregate Root and Invariant 
#### Angregate Root: EmployeeInfo 
- Một nhân viên phải có duy nhất 1 Job. 
- Một nhân viên phải có duy nhất 1 User 
#### Angregate Root: User 
- email của user phải là duy nhất 
- User phải có status active mới được đăng nhập và request tới hệ thống 
- Khi đăng nhập sẽ dùng email và password 
- roleCd phải là duy nhất 
- Role chỉ được chứa các permission hợp lệ 
- User chỉ được thực hiện request với tenant tương ứng và phải có Permission tương ứng 
## 4. Audit Context: 
### 4.1. Bounded Context: 
- AuditEvent: auditEventId, companyCd, actorId, action, resourceObject, resourceField, timestamp, valueBefore, valueAfter 
### 4.2. Ubiquitous Language: 
| Thuật ngữ | Ý nghĩa | 
|-----------|---------| 
| AuditEvent | Bản ghi cho mỗi hành động thay đổi liên quan dến lương cần kiểm toán | 
| auditEventId | Key và idempotent của auditEvent |
| actorId | userId của người thực hiện hành động thay dổi | 
| action | Hành động được thực hiện ví dụ: SALARY_UPDATE | 
| resourceObject | Đối tượng thay đổi ví dụ: SalaryProfile | 
| resourceField | Field được thay dổi ví dụ: baseSalary | 
| valueBefore, valueAfter | Giá trị trước và sau khi thay đổi | 
### 4.3 Angregate Root and Invariant 
#### Angregate Root: AuditEvent 
- Lắng nghe sự kiện `DeductionPolicyChanged`, `IncomePolicyChanged`, `SalaryProfileChanged` trên Kafka
    + Trường hợp `auditEventId` đã tồn tại    : dưới DB thì không lưu lại
    + Trường hợp `auditEventId` không tồn tại : lưu record tương ứng vào AuditEvent 
- Mỗi ngày vào 1:00 AM sẽ chạy batch check xóa các record có timestamp đã cũ hơn 7 năm 
- Chỉ người có role AUDITOR mới được truy xuất dữ liệu AuditEvent 
## 5. Notification Context: 
### 5.1. Bounded Context: 
- Notification: notificationId, companyCd, recipientId, templateCd, subject, body, templateId, status
- NotificationTemplate: templateCd, subjectTemplate, bodyTemplate, footerTemplate
### 5.2. Ubiquitous Language: 
| Thuật ngữ | Ý nghĩa | 
|-----------|---------| 
| Notification | Nội dung thông báo cần gửi đến nhân viên nhận |
| recipientId | Mã nhân viên userId sẽ nhận thông báo |
| status | Trạng thái gửi thông báo: PENDING, SUCCESS, FAILED |
| NotificationTemplate | Mẫu gửi thông báo: `Payslip Ready`, `Leave Request Confirm` |
### 5.3 Angregate Root and Invariant 
#### Angregate Root: Notification
- Lắng nghe PayslipReadyListener từ kafka nếu có thì thực hiện tạo Notification với status PENDING.
- Call API tới Identity Context để lấy về User email tương ứng với recipientId để gửi.
- Khi gửi thông báo thành công thì status là SUCCESS.
- Trường hợp gửi thất bại thì cho phép retry 3 lần, nếu cả 3 lần đều thất bại thì update status là FAILED.
## 6. Tenant Context: 
### 6.1. Bounded Context:
- Tenant: companyCd, companyName, taxCode, TenantStatus
### 6.2. Ubiquitous Language
| Thuật ngữ | Ý nghĩa | 
|-----------|---------| 
| Tenant | Quản lý thông tin công ty sử dụng hệ thống |
### 6.3. Aggregate Root and Invariant
#### Angregate Root: Tenant
- companyId phải duy nhất.