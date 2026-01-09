# Bond Portfolio Risk Analytics API

This project provides a small Spring Boot REST API to calculate simple risk metrics for plain-vanilla bonds.

Endpoints

- POST /api/bonds/ytm
  - Request: JSON `BondDTO` object
  - Response: double (YTM as decimal, e.g., 0.05 for 5%)

- POST /api/bonds/duration
  - Request: JSON `BondDTO` object
  - Response: double (Macaulay duration in years)

- POST /api/bonds/modified-duration
  - Request: JSON `BondDTO` object
  - Response: double (Modified duration)

- POST /api/bonds/portfolio/duration
  - Request: JSON array of `BondDTO`
  - Response: double (portfolio weighted average of modified durations, weights by marketPrice)

- POST /api/bonds/portfolio/load
  - Request: raw JSON string (array of bond objects)
  - Response: parsed `Portfolio` object

Sample Bond JSON

{
  "isin": "US0001",
  "maturityDate": "2030-01-01",
  "couponRate": 0.05,
  "faceValue": 1000.0,
  "marketPrice": 975.0,
  "frequency": "ANNUAL"
}

Notes

- The code currently supports annual and semi-annual coupon conventions.
- `couponDates` may be provided as an array of ISO date strings to specify exact cashflow dates.
 - `couponDates` may be provided as an array of ISO date strings to specify exact cashflow dates.

Why `frequency` (annual / semi-annual) matters

- Real-world bonds pay coupons at discrete intervals (typically annually or semi‑annually). The `frequency` field tells the calculator how many coupon periods occur per year.
- Frequency affects discounting: a 5% annual yield with semi‑annual compounding implies a periodic rate of 2.5% per half‑year. Our solver converts the annual yield into a periodic rate by dividing by `frequency` and discounts cashflows accordingly.
- Duration and modified duration depend on the timing of cashflows. Two bonds with identical coupon rates and yields but different payment frequencies can have different durations because cashflows are received at different times.
- YTM interpretation: the API returns an annualized yield. Internally we solve for the annual yield that, when converted to the periodic rate (annualYield / frequency), prices the bond correctly under the chosen convention.

Guidance for clients

- If a bond pays coupons twice per year, set `frequency` to `SEMI_ANNUAL` so the API computes discounting and durations correctly.
- If exact coupon dates are available, include `couponDates` — the `frequency` still controls periodic compounding assumptions when estimating yields and durations.
- For most plain‑vanilla corporate and government bonds, `SEMI_ANNUAL` is the common convention in many markets; use `ANNUAL` only when payments are annual.
