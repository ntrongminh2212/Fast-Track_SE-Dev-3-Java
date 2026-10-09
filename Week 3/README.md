# Performance Test – Order Flow API

## 1. Test Environment

* **Tool:** Apache JMeter
* **Environment:** [Local Environment]
* **Target API:** [http://localhost:8080/api/orders, http://localhost:8080/api/product]

## 2. Test Configuration

* **Concurrent Users:** 50
* **Test Duration:** 270 seconds (4.5 minutes)
* **Test Type:** Load Testing
* **Target:** Validate API performance under concurrent requests.

## 3. Performance Thresholds

* **p95 Response Time:** Meets the defined performance threshold.
* **Test Result:** PASS

## 4. How to Run

1. Open `Fast-Track_SE-Dev-3-Java\Week 3\order-flow-test.jmx` in Apache JMeter.
3. Run the test plan.
4. Review the response times, throughput, error rate, and p95 latency in the test results.

## 5. Test Limitations

* Results depend on the test environment, network conditions, and available resources.
* The test covers only the configured workload and scenarios.
* Results may differ under production traffic and infrastructure conditions.
