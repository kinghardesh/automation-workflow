# DMN rule descriptions and sample evaluations

All thresholds and categories below are synthetic educational rules only.

## Lab 1 — `loanDecision`

Inputs: `isValid` (boolean), `monthlyIncome` (number), `requestedAmount` (number), `creditResponse.creditScore` (number). Output: `Decision` (string). Hit policy: FIRST.

| Order | Rule | Output |
|---|---|---|
| 1 | `isValid = false` | `Rejected` |
| 2 | Valid; monthly income ≥ 50,000; requested amount ≤ 500,000; score ≥ 700 | `Approved` |
| 3 | Valid; monthly income ≥ 30,000; requested amount ≤ 750,000; score ≥ 600 | `Manual Review` |
| 4 | Otherwise | `Rejected` |

Sample evaluations: (true, 70,000, 300,000, 740) → Approved; (true, 40,000, 500,000, 630) → Manual Review; (true, 50,000, 200,000, 520) → Rejected.

## Lab 2 — `orderPriority`

Inputs: `orderTotal` (number), `customerCategory` (string), `deliveryPreference` (string). Output: `Priority` (string). Hit policy: FIRST.

| Order | Rule | Output |
|---|---|---|
| 1 | Order total ≥ 50,000 | `HIGH` |
| 2 | Customer category is `PREMIUM` | `HIGH` |
| 3 | Delivery preference is `EXPRESS` | `HIGH` |
| 4 | Otherwise | `NORMAL` |

Sample evaluations: (65,000, REGULAR, STANDARD) → HIGH; (2,400, REGULAR, STANDARD) → NORMAL; (1,200, PREMIUM, EXPRESS) → HIGH.

## Lab 3 — `triageCategory`

Input: `urgencyIndicators` (string). Output: `Triage category` (string). Hit policy: FIRST.

| Order | Rule | Output |
|---|---|---|
| 1 | Indicator is `URGENT` | `Urgent Assessment` |
| 2 | Indicator is `PRIORITY` | `Priority Review` |
| 3 | Otherwise | `Routine` |

This is a workflow demonstration only and is not a clinical triage tool.
