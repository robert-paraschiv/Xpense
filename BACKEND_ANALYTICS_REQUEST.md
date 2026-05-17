# Backend Analytics API Enhancement Request

> **Goal:** Move all analytics/statistics computation to the backend so the Android client doesn't need to fetch all transactions and compute statistics locally.

---

## Current Problem

The Android app currently:
1. Calls `GET /api/wallets/{walletId}/transactions` to fetch **all** transactions
2. Filters them by month/year on the client
3. Computes `totalAmountSpent`, `amountByCategory`, `categories` (grouped transactions), `transactionsByDay`, etc. locally
4. Builds a `StatisticsDoc` object from raw transaction data

This is inefficient because:
- Downloads **all** transactions regardless of the date range needed
- Duplicates business logic between frontend and backend
- Doesn't scale as transaction count grows
- Makes the client responsible for aggregation that the database can do faster

---

## Requested Backend Endpoints

### 1. `GET /api/wallets/{walletId}/statistics`

Get computed statistics for a wallet in a specific month.

**Query Parameters:**
| Parameter | Type   | Required | Default         | Description                          |
|-----------|--------|----------|-----------------|--------------------------------------|
| `year`    | int    | No       | Current year    | Year to compute stats for            |
| `month`   | int    | No       | Current month   | Month (1-12) to compute stats for    |

**Example:** `GET /api/wallets/abc-123/statistics?year=2026&month=5`

**Response `200 OK`:**
```json
{
  "walletId": "abc-123",
  "year": 2026,
  "month": 5,
  "totalAmountSpent": 1450.50,
  "totalIncome": 3000.00,
  "transactionCount": 42,
  "amountByCategory": {
    "Food": 450.00,
    "Transport": 200.00,
    "Bills": 350.00,
    "Entertainment": 150.00,
    "Shopping": 300.50
  },
  "transactionsByCategory": {
    "Food": [
      {
        "id": "tx-1",
        "title": "Grocery shopping",
        "amount": 45.50,
        "date": "2026-05-17T14:30:00",
        "user_name": "John Doe",
        "pic_url": null,
        "isCashTransaction": true
      }
    ],
    "Transport": [...]
  },
  "dailyTotals": {
    "2026-05-01": { "expense": 45.00, "income": 0.0, "count": 2 },
    "2026-05-02": { "expense": 120.00, "income": 3000.0, "count": 3 },
    "...": "..."
  },
  "dailyAverage": 34.50,
  "biggestExpense": {
    "id": "tx-5",
    "title": "Rent",
    "amount": 800.00,
    "category": "Bills",
    "date": "2026-05-01T10:00:00"
  },
  "peakSpendDay": "Saturday",
  "savingsRate": 51.65
}
```

**Error `404 Not Found`:** `"Wallet not found or no access"`

---

### 2. `GET /api/wallets/{walletId}/statistics/yearly`

Get aggregated statistics for a full year (all 12 months summarized).

**Query Parameters:**
| Parameter | Type | Required | Default      | Description        |
|-----------|------|----------|--------------|--------------------|
| `year`    | int  | No       | Current year | Year to aggregate  |

**Example:** `GET /api/wallets/abc-123/statistics/yearly?year=2026`

**Response `200 OK`:**
```json
{
  "walletId": "abc-123",
  "year": 2026,
  "totalAmountSpent": 12500.00,
  "totalIncome": 36000.00,
  "transactionCount": 380,
  "amountByCategory": {
    "Food": 5400.00,
    "Transport": 2400.00,
    "Bills": 3200.00,
    "Entertainment": 1500.00
  },
  "monthlyBreakdown": [
    {
      "month": 1,
      "totalExpense": 1100.00,
      "totalIncome": 3000.00,
      "transactionCount": 28
    },
    {
      "month": 2,
      "totalExpense": 980.00,
      "totalIncome": 3000.00,
      "transactionCount": 25
    }
  ],
  "transactionsByCategory": {
    "Food": [...],
    "Transport": [...]
  },
  "dailyAverage": 34.25,
  "biggestExpense": {
    "id": "tx-100",
    "title": "Vacation",
    "amount": 2000.00,
    "category": "Travel",
    "date": "2026-07-15T09:00:00"
  }
}
```

---

### 3. `GET /api/wallets/{walletId}/statistics/compare`

Compare current period vs previous period for change metrics.

**Query Parameters:**
| Parameter | Type   | Required | Default       | Description                     |
|-----------|--------|----------|---------------|---------------------------------|
| `year`    | int    | No       | Current year  | Current period year             |
| `month`   | int    | No       | Current month | Current period month (1-12)     |
| `type`    | String | No       | `MONTHLY`     | `MONTHLY` or `YEARLY`           |

**Example:** `GET /api/wallets/abc-123/statistics/compare?year=2026&month=5&type=MONTHLY`

**Response `200 OK`:**
```json
{
  "walletId": "abc-123",
  "currentPeriod": {
    "totalExpense": 1450.50,
    "totalIncome": 3000.00,
    "transactionCount": 42
  },
  "previousPeriod": {
    "totalExpense": 1200.00,
    "totalIncome": 3000.00,
    "transactionCount": 38
  },
  "spendingChangePercent": 20.88,
  "spendingTrendUp": true,
  "categoryChanges": [
    {
      "category": "Food",
      "currentAmount": 450.00,
      "previousAmount": 380.00,
      "changePercent": 18.42,
      "trendUp": true
    },
    {
      "category": "Transport",
      "currentAmount": 200.00,
      "previousAmount": 250.00,
      "changePercent": -20.00,
      "trendUp": false
    }
  ]
}
```

---

### 4. `GET /api/wallets/{walletId}/transactions/recent`

Get the N most recent transactions (avoids fetching everything).

**Query Parameters:**
| Parameter | Type | Required | Default | Description                    |
|-----------|------|----------|---------|--------------------------------|
| `limit`   | int  | No       | `5`     | Number of recent transactions  |

**Example:** `GET /api/wallets/abc-123/transactions/recent?limit=5`

**Response `200 OK`:** Array of `TransactionDTO` objects, sorted by date descending.

---

### 5. `GET /api/wallets/{walletId}/transactions/latest`

Get only the single latest transaction for a wallet.

**Response `200 OK`:** Single `TransactionDTO` object (or `204 No Content` if none exist).

---

## Summary of What Moves to Backend

| Currently on Client                     | Proposed Backend Endpoint                              |
|-----------------------------------------|--------------------------------------------------------|
| Filter transactions by month/year       | `GET /statistics?year=&month=`                         |
| Sum expenses by category                | `GET /statistics` → `amountByCategory`                 |
| Group transactions by category          | `GET /statistics` → `transactionsByCategory`           |
| Calculate total spent                   | `GET /statistics` → `totalAmountSpent`                 |
| Calculate total income                  | `GET /statistics` → `totalIncome`                      |
| Find biggest expense                    | `GET /statistics` → `biggestExpense`                   |
| Calculate daily average                 | `GET /statistics` → `dailyAverage`                     |
| Find peak spending day of week          | `GET /statistics` → `peakSpendDay`                     |
| Compute savings rate                    | `GET /statistics` → `savingsRate`                      |
| Compare with previous period            | `GET /statistics/compare`                              |
| Fetch all txns just to get latest       | `GET /transactions/latest`                             |
| Fetch all txns for recent list          | `GET /transactions/recent?limit=5`                     |
| Aggregate yearly from monthly docs      | `GET /statistics/yearly`                               |
| Group transactions by day for bar chart | `GET /statistics` → `dailyTotals`                      |

---

## Benefits

1. **Performance** — Single API call returns pre-computed stats vs. fetching hundreds of transactions
2. **Scalability** — Database-level aggregation (SQL `GROUP BY`, `SUM`) is orders of magnitude faster
3. **Consistency** — Single source of truth for calculations
4. **Bandwidth** — Client downloads kilobytes instead of potentially megabytes of transaction data
5. **Battery/Memory** — No heavy client-side filtering, mapping, or sorting

---

## Migration Path

Once the backend implements these endpoints:

1. Replace `StatisticsRepo.buildStatisticsDocFromTransactions()` with a direct call to `GET /statistics`
2. Replace `StatisticsRepo.buildStatisticsDocFromHistory()` with `GET /statistics/yearly`
3. Replace `TransactionRepo.loadLatestTransaction()` (which fetches ALL transactions) with `GET /transactions/latest`
4. Replace the home screen's "recent transactions" derivation from stats with `GET /transactions/recent?limit=5`
5. Replace `loadTransactionsDateInterval()` (bar chart) with `GET /statistics` → `dailyTotals`
6. Replace comparison insights with `GET /statistics/compare`

---

## Frontend Implementation Status ✅

The Android client is **already wired up** to use these endpoints. The implementation uses a fallback strategy:

1. **Primary**: Calls the new `/statistics`, `/statistics/yearly`, `/statistics/compare`, `/transactions/recent`, `/transactions/latest` endpoints
2. **Fallback**: If those return 404 (not yet implemented), it falls back to fetching all transactions and computing locally

**Files already updated:**
- `StatisticsApiService.kt` — Retrofit interface with all 5 new endpoints
- `StatisticsModels.kt` — Response DTOs (`StatisticsResponse`, `YearlyStatisticsResponse`, `CompareResponse`, etc.)
- `ApiServiceProvider.kt` — Added `statisticsService`
- `StatisticsRepo.kt` — Uses `/statistics` endpoint first, falls back to `/transactions` + local computation
- `TransactionRepo.kt` — Uses `/transactions/latest` and `/transactions/recent` with fallback
- `StatisticsViewModel.kt` — Exposes `getCompareStatistics()` to UI
- `TransactionViewModel.kt` — Exposes `loadRecentTransactions()` to UI

Once the backend implements these endpoints, the client will automatically start using them (no app update needed for the fallback logic to switch).

---

*Document created: May 17, 2026*


