# Mock REST API examples

Start the Spring Boot app first (`mvn spring-boot:run`). All data below is synthetic.

## Credit assessment

```bash
curl -X POST http://localhost:8081/mock/credit-assessment \
  -H 'Content-Type: application/json' \
  -d '{"applicantId":"APP-100","monthlyIncome":70000,"requestedAmount":300000}'
```

Expected shape: `{"creditScore":740,"rating":"GOOD","assessmentId":"CR-..."}`. Use `applicantId: API_ERROR` to simulate HTTP 503.

## Payment

```bash
curl -X POST http://localhost:8081/mock/payments \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD-101","amount":2400,"currency":"INR","paymentToken":"MOCK-OK"}'
```

`MOCK-OK` returns `APPROVED`; `DECLINE` returns `DECLINED`; `FAIL_SERVICE` returns HTTP 503.

## Appointment slots

```bash
curl 'http://localhost:8081/mock/slots?preference=MORNING&category=Routine'
```

`MORNING`/`AFTERNOON` return synthetic slots; `NONE` returns an empty list; `API_ERROR` returns HTTP 503.
