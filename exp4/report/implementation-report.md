# PBJ-2 implementation report (fill in actual execution results)

## Architecture
Each lab uses BPMN for orchestration, a DMN decision table for rule-based classification, Zeebe external job workers in Java/Spring Boot, and a local mock REST API for integration. Tasklist handles human review; Operate is used for instance inspection and incident recovery.

## Lab 1 — Loan approval
Validation worker sets `isValid` and `validationMessage`. The credit mock returns `creditScore` and `rating`, mapped under `creditResponse`. `loanDecision` classifies the request as Approved, Rejected, or Manual Review. The manual-review form returns `reviewApproved`.

## Lab 2 — E-commerce
The order worker sets `isValid`, `stockAvailable`, `orderTotal`, `requiresVerification`, and `orderId`. The `orderPriority` DMN determines HIGH/NORMAL priority. High-value orders can be sent to the verification task. Payment status and reference are stored in `paymentResponse`; only `APPROVED` proceeds to fulfilment.

## Lab 3 — Hospital triage
Registration worker sets `isRegistrationValid`. `triageCategory` classifies the educational urgency indicator. Urgent requests go to immediate human assessment. Other requests look up mock slots; no-slot cases go to scheduling staff. Booking worker sets `bookingStatus`, `bookedSlot`, and `bookingReference`.

## Test results
Do not mark any scenario as passed until it has been run in your own Camunda environment. Complete the accompanying `test-results.csv` with actual instance IDs, outcomes, and screenshot filenames.

## Limitations
All API responses, stock data, credit scores, and appointment slots are synthetic. Rules are educational, not suitable for real lending, payments, or clinical use. Connector behavior and deployment steps can vary with Camunda 8 runtime/version. Capture genuine screenshots from Modeler, Tasklist, and Operate after execution.
