## 1. Employee Context:
### 1.1. Bounded Context:
- EmployeeInfo: employeeCd, name, dateOfBirth, phoneNumber, nationalId, homeAddress, employmentStatus, joinDate, jobId, companyCd
- Job: jobId, jobName, jobRank
- LeaveBalance: employeeCd, leaveType, year, remainQuota, grantQuota
- LeaveRequest: employeeCd, leaveType, leaveDate, period(morning, afternoon, all day), approveStatus, approverId
### 1.2. Ubiquitous Language:
| Thuật ngữ | Ý nghĩa |
|-----------|---------|
| EmployeeInfo | Thông tin cá nhân của một nhân viên và ngày vào công ty |
| Job | Thông tin công việc và cấp bậc |
| LeaveBalance | Các loại ngày nghỉ phép được cấp cho nhân viên theo năm và quota còn lại |
| LeaveRequest | Yêu cầu nghỉ phép của một nhân viên trong một khoảng thời gian  |
| Approve | Hành động phê duyệt một yêu cầu nghỉ phép bởi người có thẩm quyền. |
| Cancel | Hành động hủy một yêu cầu nghỉ phép của nhân viên hoặc người có thẩm quyền |
| Grant Quota | Số ngày nghỉ phép được hệ thống cấp cho nhân viên theo năm |
| Remain Quota | Hạn mức còn lại theo quy tắc tính quota của hệ thống |
### 1.3. Aggregate Root and Invariant

#### Aggregate Root: EmployeeInfo
- Một nhân viên phải có duy nhất 1 Job.
- Khi đã có approver confirm request nghỉ phép thì không thể cancel.
- Mỗi yêu cầu nghỉ phép phải thuộc về một nhân viên hợp lệ trong cùng tenant.
- Không được tạo yêu cầu nghỉ phép làm vượt hạn mức Remain Quota.
- Khi tạo request nghỉ phải trừ remainQuota (all day trừ 1 quota, morning/afternoon trừ 0.5 quota).
- Khi cancel request nghỉ phải cộng lại remainQuota tương ứng
- Trạng thái phê duyệt chỉ được thay đổi bởi người có thẩm quyền.

## 2. Payroll Context:
### 2.1. Bounded Context:
- SalaryProfile: salaryProfileId, employeeCd, companyCd, baseSalary, effectiveFrom, effectiveTo
- DeductionPolicy: deductionCd, calculationType (FIXED_AMOUNT, PERCENTAGE), calculationValue
  + PersonalIncomeTaxPolicy: calculationType phần trăm tính lũy tiến theo lương cơ bản theo luật lao động
  + SocialInsurancePolicy: calculationType phần trăm theo luật
  + HealthInsurancePolicy: calculationType phần trăm theo luật
  + CompanyUnionPolicy: calculationType fix cứng khác nhau theo công ty
- IncomePolicy: incomeCd, calculationType (FIXED_AMOUNT, PERCENTAGE), calculationValue, effectiveFrom, effectiveTo
- DeductionPoliciesApply: payslipId, deductionCd, calculationBase, calculationType, calculationValueApplied, deductionAmount
- IncomePoliciesApply: payslipId, incomeCd, calculationBase, calculationType, calculationValueApplied, incomeAmount
- Payslip: payslipId, salaryProfileId, payrollRunId, payPeriod, deductionPoliciesApply, incomePoliciesApply, run, totalDeduction, totalIncome, netSalary, grossSalary, runStatus
- PayrollRun: payrollRunId, companyCd, payPeriod, totalEmployees, runDateTime, successCount, failureCount, runStatus
 
### 2.2 Ubiquitous Language
| Thuật ngữ | Ý nghĩa |
|-----------|---------|
| SalaryProfile | Lương và income của một nhân viên tại một thời điểm |
| DeductionPolicy | Các khoản thuế phí sẽ trừ vào tiền lương của nhân viên, tùy vào loại thuế phí sẽ có phần trăm hoặc số tiền fix cần trả khác nhau theo luật nhà nước hoặc công ty |
| IncomePolicy | Các khoản bonus được cộng vào tiền lương của nhân viên, tùy vào loại income sẽ có phần trăm hoặc số tiền fix cần trả khác nhau |
| DeductionPoliciesApply | Deduction Policy được apply vào Payslip trong lần run hiện tại |
| IncomePoliciesApply | Income Policy được apply vào Payslip trong lần run hiện tại |
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
- Kỳ hạn một lần chạy sẽ là 1 tháng/ một lần, payperiod phải có định dạng yyyy/MM
- Ghi lại thời gian chạy PayrollRun yyyyMMddHHmmss
- Trong trường hợp lần chạy payroll run failed vì bất kỳ lỗi nào phải có khả năng chạy lại tiếp từ payslip lỗi trở đi, không chạy lại các payslip đã xong.
- incomeAmount tính như sau: 
    + Với calculationType là percent: (base salary * calculationValue (số < 1))
    + Với calculationType là fix amount: calculationValue
- Gross salary sẽ bằng base salary + (tất cả các incomeAmount)
- deductionAmount tính như sau: 
    + Với calculationType là percent: gross salary * calculationValue (số < 1)
    + Với calculationType là fix amount: calculationValue
- netSalary sẽ bằng gross salary - (tất cả các deductionAmount)
- Phải lấy đúng SalaryProfile có effectiveFrom, effectiveTo trong kỳ hạn payPeriod chạy của PayrollRun

## 3. Identity Context:
### 3.1. Bounded Context:
- EmployeeInfo: employeeCd, name, dateOfBirth, phoneNumber, nationalId, homeAddress, employmentStatus, joinDate, jobId, companyCd

### 3.2. Ubiquitous Language:

### 3.3 Angregate Root and Invariant
#### Angregate Root: 