# PBJ-2 Practical Assignment — Camunda 8 starter implementation

This package contains corrected working copies of the three uploaded BPMN models, three DMN tables, Tasklist form files, and a Java/Spring Boot application that supplies the external workers and synthetic REST endpoints.

> **Evidence integrity:** The files are prepared for you, but they have not been deployed to your Camunda environment here. Capture your own screenshots from your running Modeler, Tasklist, and Operate. Do not treat the included test matrix as execution evidence until you run it.

## Contents

- `bpmn/`: corrected BPMN files for all three labs.
- `dmn/`: `loan-decision.dmn`, `order-priority.dmn`, `triage-category.dmn`.
- `forms/`: Tasklist forms referenced by user tasks.
- `src/`: Java/Spring Boot mock REST APIs and Zeebe external job workers.
- `examples/`: synthetic process variables and example API payloads.
- `report/`: implementation summary and test matrix template.

## Requirements

- Java 21
- Maven 3.9+
- A reachable Camunda 8 cluster / local runtime with Zeebe gateway at `localhost:26500`
- Camunda 8 Modeler and Tasklist/Operate access

The project uses Zeebe Java client `8.7.5` and Spring Boot `3.4.2`. Match the client version to your Camunda 8 runtime if you use a different version.

## Run the worker + mock API

From this directory:

```bash
mvn spring-boot:run
```

Default configuration: REST API on `http://localhost:8081`; Zeebe gateway at `localhost:26500` using plaintext. Override with environment variables `ZEEBE_GATEWAY_ADDRESS` and `ZEEBE_PLAINTEXT` as needed.

If Camunda is running in Docker and the connector runs in a different container, `localhost:8081` inside the Camunda container will not mean your host. Put both services on a shared Docker network and use the worker/API service DNS name, or use `host.docker.internal` where supported. Update the URL in each BPMN connector accordingly.

## Deploy models

1. Open each BPMN in Camunda Modeler and confirm no validation errors.
2. Deploy each BPMN together with its matching DMN file and relevant `.form` files. If using a deployment UI, select all resources together so deployment-bound forms resolve.
3. Start the process with the example variables in `examples/process-variables.json` (choose the relevant lab's object).
4. Use Tasklist to complete review tasks. Use Operate to inspect variables, activity history, incidents, and retries.
5. If your runtime does not support the connector template in this file, install/enable Camunda's HTTP JSON connector or adapt the connector to your runtime's supported connector configuration.

## Synthetic failure and recovery exercises

- **Worker failure:** set `applicantId` (Lab 1), `productId` (Lab 2), or `patientReference` (Lab 3) to `WORKER_FAIL`. The worker intentionally fails until the input is corrected. Let retries exhaust, inspect the incident in Operate, update the variable to a valid value, then retry the job/incident.
- **Credit API outage:** Lab 1 `applicantId = API_ERROR` returns HTTP 503 from the mock API.
- **Payment API outage:** Lab 2 `paymentToken = FAIL_SERVICE` returns HTTP 503. `paymentToken = DECLINE` returns a normal declined payment.
- **Scheduling API outage:** Lab 3 `appointmentPreference = API_ERROR` returns HTTP 503. `appointmentPreference = NONE` returns an empty slot list.

Connector failure behavior depends on the installed HTTP JSON connector version and runtime configuration. If an HTTP 503 is treated as a completed response rather than a failed job, inspect connector logs and configure its failure behavior before documenting an incident/retry test.

## Important educational assumptions

- All credit, payment, stock, and appointment information is synthetic. Do not use real payment credentials or patient information.
- Loan rules are deliberately simplified: valid applications with income at least ₹50,000, amount at most ₹500,000, and credit score at least 700 are approved; a less strict band routes to manual review; remaining cases are rejected.
- Order inventory and unit prices are hard-coded synthetic examples in `WorkerConfiguration.java`.
- Triage labels are educational workflow categories, not medical advice or real clinical triage criteria.
- The sample scheduling service returns dates that are examples, not live appointment availability.
