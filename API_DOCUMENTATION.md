# Xpense Backend – Full API Documentation

> **Base URL:** `http://<host>:8080/api`  
> All endpoints (except Auth) require a JWT bearer token in the `Authorization` header:  
> `Authorization: Bearer <token>`

---

## Table of Contents

1. [Authentication](#1-authentication)
2. [Users](#2-users)
3. [Wallets](#3-wallets)
4. [Transactions](#4-transactions)
5. [Invitations](#5-invitations)
6. [Predictions & Analytics](#6-predictions--analytics)
7. [Bank Integration (Nordigen)](#7-bank-integration-nordigen)
8. [File Upload](#8-file-upload)
9. [Data Models](#9-data-models)
10. [Android Integration Guide](#10-android-integration-guide)

---

## 1. Authentication

**Base path:** `/api/auth`  
All auth endpoints are **public** (no token required).

---

### POST `/api/auth/register`

Register a new user account.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "phoneNumber": "+1234567890"
}
```

**Response `201 Created`:**
```json
{
  "token": "<jwt_access_token>",
  "refreshToken": "<jwt_refresh_token>",
  "user": {
    "uid": "uuid-string",
    "name": "John Doe",
    "email": "john@example.com",
    "phoneNumber": "+1234567890",
    "pictureUrl": null,
    "fcmToken": null,
    "createdAt": "2026-05-17T10:00:00",
    "updatedAt": "2026-05-17T10:00:00",
    "walletIds": []
  },
  "message": "Registration successful"
}
```

**Error `400 Bad Request`:** `"Registration failed: <reason>"`

---

### POST `/api/auth/login`

Authenticate an existing user.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response `200 OK`:**
```json
{
  "token": "<jwt_access_token>",
  "refreshToken": "<jwt_refresh_token>",
  "user": { ... },
  "message": "Login successful"
}
```

**Error `401 Unauthorized`:** `"Login failed: <reason>"`

---

### POST `/api/auth/refresh`

Refresh an expired JWT token.

**Header:**
```
Authorization: Bearer <existing_token>
```

**Response `200 OK`:**
```json
{
  "token": "<new_jwt_access_token>"
}
```

**Error `401 Unauthorized`:** `"Token refresh failed: <reason>"`

---

## 2. Users

**Base path:** `/api/users`  
🔒 All endpoints require authentication.

---

### GET `/api/users/me`

Get the currently authenticated user's profile.

**Response `200 OK`:**
```json
{
  "uid": "uuid-string",
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "+1234567890",
  "pictureUrl": "https://example.com/photo.jpg",
  "fcmToken": "firebase-cloud-messaging-token",
  "createdAt": "2026-05-17T10:00:00",
  "updatedAt": "2026-05-17T10:00:00",
  "walletIds": ["wallet-id-1", "wallet-id-2"]
}
```

**Error `404 Not Found`:** `"User not found: <reason>"`

---

### GET `/api/users/{userId}`

Get a specific user's profile by their ID.

**Path Parameters:**
| Parameter | Type   | Description           |
|-----------|--------|-----------------------|
| `userId`  | String | The target user's UID |

**Response `200 OK`:** Same as `/api/users/me`

**Error `404 Not Found`:** `"User not found: <reason>"`

---

### PUT `/api/users/me`

Update the current user's profile.

**Request Body (all fields optional):**
```json
{
  "name": "John Updated",
  "phoneNumber": "+9876543210",
  "pictureUrl": "https://example.com/new-photo.jpg"
}
```

**Response `200 OK`:** Updated `UserDTO` object.

**Error `400 Bad Request`:** `"Update failed: <reason>"`

---

### POST `/api/users/fcm-token`

Register or update the device's Firebase Cloud Messaging token for push notifications.

**Request Body:**
```json
{
  "fcmToken": "firebase-device-token-here"
}
```

**Response `200 OK`:** `"FCM token updated successfully"`

**Error `400 Bad Request`:** `"Update failed: <reason>"`

---

## 3. Wallets

**Base path:** `/api/wallets`  
🔒 All endpoints require authentication.

---

### POST `/api/wallets`

Create a new wallet.

**Request Body:**
```json
{
  "title": "My Wallet",
  "amount": 1000.00,
  "currency": "USD"
}
```

**Response `201 Created`:**
```json
{
  "id": "wallet-uuid",
  "title": "My Wallet",
  "creator_id": "user-uuid",
  "amount": 1000.00,
  "currency": "USD",
  "creation_date": "2026-05-17T10:00:00",
  "users": ["user-uuid"],
  "walletUsers": [ { ...UserDTO... } ],
  "bAccount": null
}
```

**Error `400 Bad Request`:** `"Creation failed: <reason>"`

---

### GET `/api/wallets`

Get all wallets the authenticated user belongs to.

**Response `200 OK`:**
```json
[
  {
    "id": "wallet-uuid",
    "title": "My Wallet",
    "creator_id": "user-uuid",
    "amount": 1000.00,
    "currency": "USD",
    "creation_date": "2026-05-17T10:00:00",
    "users": ["user-uuid"],
    "walletUsers": [ { ...UserDTO... } ],
    "bAccount": null
  }
]
```

---

### GET `/api/wallets/{walletId}`

Get a specific wallet by its ID. The user must be a member of the wallet.

**Path Parameters:**
| Parameter  | Type   | Description    |
|------------|--------|----------------|
| `walletId` | String | The wallet's ID |

**Response `200 OK`:** `WalletDTO` object.

**Error `404 Not Found`:** `"Wallet not found: <reason>"`

---

### PUT `/api/wallets/{walletId}`

Update a wallet's details. Only the wallet creator can update it.

**Request Body:**
```json
{
  "title": "Updated Wallet Name",
  "amount": 2000.00,
  "currency": "EUR"
}
```

**Response `200 OK`:** Updated `WalletDTO` object.

**Error `400 Bad Request`:** `"Update failed: <reason>"`

---

### DELETE `/api/wallets/{walletId}`

Delete a wallet. Only the wallet creator can delete it.

**Response `200 OK`:** `"Wallet deleted successfully"`

**Error `400 Bad Request`:** `"Delete failed: <reason>"`

---

### POST `/api/wallets/{walletId}/users/{userIdToAdd}`

Add a user to a wallet (shared wallet). The requester must be the wallet owner.

**Path Parameters:**
| Parameter     | Type   | Description            |
|---------------|--------|------------------------|
| `walletId`    | String | The wallet's ID        |
| `userIdToAdd` | String | The ID of user to add  |

**Response `200 OK`:** `"User added to wallet successfully"`

**Error `400 Bad Request`:** `"Operation failed: <reason>"`

---

### DELETE `/api/wallets/{walletId}/users/{userIdToRemove}`

Remove a user from a wallet. The requester must be the wallet owner.

**Path Parameters:**
| Parameter        | Type   | Description               |
|------------------|--------|---------------------------|
| `walletId`       | String | The wallet's ID           |
| `userIdToRemove` | String | The ID of user to remove  |

**Response `200 OK`:** `"User removed from wallet successfully"`

**Error `400 Bad Request`:** `"Operation failed: <reason>"`

---

## 4. Transactions

**Base path:** `/api/wallets/{walletId}/transactions`  
🔒 All endpoints require authentication. The user must be a member of the wallet.

---

### POST `/api/wallets/{walletId}/transactions`

Create a new transaction in a wallet.

**Request Body:**
```json
{
  "type": "EXPENSE",
  "category": "Food",
  "title": "Grocery shopping",
  "amount": 45.50,
  "currency": "USD",
  "date": "2026-05-17T14:30:00",
  "pic_url": "https://example.com/receipt.jpg",
  "isCashTransaction": true
}
```

> **`type` values:** `EXPENSE` | `INCOME`

**Response `201 Created`:**
```json
{
  "id": "transaction-uuid",
  "walletId": "wallet-uuid",
  "type": "EXPENSE",
  "category": "Food",
  "user_id": "user-uuid",
  "user_name": "John Doe",
  "title": "Grocery shopping",
  "amount": 45.50,
  "currency": "USD",
  "date": "2026-05-17T14:30:00",
  "pic_url": "https://example.com/receipt.jpg",
  "isCashTransaction": true,
  "alreadyAdded": false
}
```

**Error `400 Bad Request`:** `"Creation failed: <reason>"`

---

### GET `/api/wallets/{walletId}/transactions`

Get all transactions for a wallet.

**Response `200 OK`:** Array of `TransactionDTO` objects.

---

### GET `/api/wallets/{walletId}/transactions/{transactionId}`

Get a specific transaction by ID.

**Response `200 OK`:** `TransactionDTO` object.

**Error `404 Not Found`:** `"Transaction not found: <reason>"`

---

### PUT `/api/wallets/{walletId}/transactions/{transactionId}`

Update a transaction.

**Request Body:** Same fields as the create transaction request (partial updates accepted).

**Response `200 OK`:** Updated `TransactionDTO` object.

**Error `400 Bad Request`:** `"Update failed: <reason>"`

---

### DELETE `/api/wallets/{walletId}/transactions/{transactionId}`

Delete a transaction.

**Response `200 OK`:** `"Transaction deleted successfully"`

**Error `400 Bad Request`:** `"Delete failed: <reason>"`

---

## 5. Invitations

**Base path:** `/api/invitations`  
🔒 All endpoints require authentication.

---

### POST `/api/invitations`

Send an invitation to a phone number to join a wallet.

**Request Body:**
```json
{
  "wallet_title": "Family Budget",
  "invited_person_phone_number": "+1234567890"
}
```

**Response `201 Created`:**
```json
{
  "id": "invitation-uuid",
  "creator_id": "user-uuid",
  "creator_name": "John Doe",
  "wallet_title": "Family Budget",
  "invited_person_phone_number": "+1234567890",
  "creator_pic_url": "https://example.com/photo.jpg",
  "date": "2026-05-17T10:00:00",
  "status": "PENDING"
}
```

> **`status` values:** `PENDING` | `ACCEPTED` | `DECLINED`

**Error `400 Bad Request`:** `"Creation failed: <reason>"`

---

### GET `/api/invitations/phone/{phoneNumber}`

Get all invitations sent to a specific phone number.

**Path Parameters:**
| Parameter     | Type   | Description             |
|---------------|--------|-------------------------|
| `phoneNumber` | String | The phone number to query (URL-encoded if needed) |

**Response `200 OK`:** Array of `InvitationDTO` objects.

---

### GET `/api/invitations/status/{status}`

Get all invitations filtered by status.

**Path Parameters:**
| Parameter | Type   | Description                              |
|-----------|--------|------------------------------------------|
| `status`  | String | `PENDING`, `ACCEPTED`, or `DECLINED`    |

**Response `200 OK`:** Array of `InvitationDTO` objects.

---

### POST `/api/invitations/{invitationId}/accept`

Accept an invitation. The authenticated user is added to the wallet.

**Path Parameters:**
| Parameter      | Type   | Description          |
|----------------|--------|----------------------|
| `invitationId` | String | The invitation's ID  |

**Response `200 OK`:** Updated `InvitationDTO` with `"status": "ACCEPTED"`.

**Error `400 Bad Request`:** `"Operation failed: <reason>"`

---

### POST `/api/invitations/{invitationId}/decline`

Decline an invitation.

**Response `200 OK`:** Updated `InvitationDTO` with `"status": "DECLINED"`.

**Error `400 Bad Request`:** `"Operation failed: <reason>"`

---

## 6. Predictions & Analytics

**Base path:** `/api/predictions`  
🔒 All endpoints require authentication.

---

### GET `/api/predictions/wallet/{walletId}/next-month`

Forecast the total spending for the **next month** on a specific wallet.

**Response `200 OK`:**
```json
{
  "walletId": "wallet-uuid",
  "currency": "USD",
  "period_start": "2026-06-01",
  "period_end": "2026-06-30",
  "point_estimate": 1250.00,
  "lower_bound": 1100.00,
  "upper_bound": 1400.00,
  "confidence_level": 0.95,
  "model_name": "MOVING_AVERAGE",
  "data_points_used": 6,
  "breakdown": [
    {
      "category": "Food",
      "point_estimate": 400.00,
      "lower_bound": 350.00,
      "upper_bound": 450.00,
      "data_points_used": 6,
      "model_name": "MOVING_AVERAGE"
    }
  ]
}
```

**Error `400 Bad Request`:** `"Forecast failed: <reason>"`

---

### GET `/api/predictions/wallet/{walletId}/end-of-month-projection`

Project the **end-of-month** spending based on month-to-date spending and historical averages.

**Response `200 OK`:**
```json
{
  "walletId": "wallet-uuid",
  "currency": "USD",
  "current_month": "2026-05-01",
  "days_elapsed": 17,
  "days_in_month": 31,
  "mtd_spend": 680.00,
  "naive_projection": 1239.00,
  "blended_projection": 1180.00,
  "historical_mean": 1100.00,
  "lower_bound": 950.00,
  "upper_bound": 1350.00,
  "confidence_level": 0.95
}
```

**Error `400 Bad Request`:** `"Projection failed: <reason>"`

---

### GET `/api/predictions/user/me/categories`

Get per-category spending forecasts for the current user across all their wallets.

**Response `200 OK`:**
```json
[
  {
    "category": "Food",
    "point_estimate": 400.00,
    "lower_bound": 350.00,
    "upper_bound": 450.00,
    "data_points_used": 6,
    "model_name": "MOVING_AVERAGE"
  },
  {
    "category": "Transport",
    "point_estimate": 150.00,
    "lower_bound": 120.00,
    "upper_bound": 180.00,
    "data_points_used": 6,
    "model_name": "MOVING_AVERAGE"
  }
]
```

**Error `500 Internal Server Error`:** `"Category forecast failed: <reason>"`

---

### GET `/api/predictions/user/me/recurring`

Detect and return recurring transactions for the current user.

**Response `200 OK`:**
```json
[
  {
    "id": "recurring-uuid",
    "wallet_id": "wallet-uuid",
    "category": "Subscriptions",
    "label": "Netflix",
    "avg_amount": 15.99,
    "currency": "USD",
    "cadence_days": 30,
    "cadence_label": "Monthly",
    "last_seen_date": "2026-04-17",
    "next_expected_date": "2026-05-17",
    "occurrences": 6,
    "confidence": 0.92
  }
]
```

**Error `500 Internal Server Error`:** `"Recurring detection failed: <reason>"`

---

### GET `/api/predictions/user/me/anomalies`

Detect spending anomalies for the current user compared to historical averages.

**Response `200 OK`:**
```json
[
  {
    "category": "Dining",
    "currency": "USD",
    "wallet_id": "wallet-uuid",
    "current_spend": 450.00,
    "historical_mean": 200.00,
    "historical_std_dev": 50.00,
    "z_score": 5.0,
    "severity": "HIGH",
    "message": "Your Dining spending this month is unusually high."
  }
]
```

> **`severity` values:** `LOW` | `MEDIUM` | `HIGH`

**Error `500 Internal Server Error`:** `"Anomaly detection failed: <reason>"`

---

### GET `/api/predictions/wallet/{walletId}/history`

Retrieve spending history data suitable for charts.

**Query Parameters:**
| Parameter    | Type   | Default    | Description                           |
|--------------|--------|------------|---------------------------------------|
| `periodType` | String | `MONTHLY`  | Aggregation period (`MONTHLY`, `WEEKLY`) |
| `months`     | int    | `12`       | Number of periods to retrieve         |

**Example:** `GET /api/predictions/wallet/wallet-uuid/history?periodType=MONTHLY&months=6`

**Response `200 OK`:**
```json
{
  "wallet_id": "wallet-uuid",
  "currency": "USD",
  "period_type": "MONTHLY",
  "data": [
    {
      "period_start": "2025-12-01",
      "total_expense": 1100.00,
      "total_income": 3000.00,
      "tx_count": 25,
      "category": null
    },
    {
      "period_start": "2026-01-01",
      "total_expense": 980.00,
      "total_income": 3000.00,
      "tx_count": 22,
      "category": null
    }
  ]
}
```

**Error `400 Bad Request`:** `"History fetch failed: <reason>"`

---

## 7. Bank Integration (Nordigen)

**Base path:** `/api/bank`  
🔒 All endpoints require authentication.  
Uses the **GoCardless (Nordigen) Open Banking API** for linking real bank accounts.

---

### GET `/api/bank/institutions`

Get a list of supported banks/institutions for a given country.

**Query Parameters:**
| Parameter | Type   | Required | Description                     |
|-----------|--------|----------|---------------------------------|
| `country` | String | ✅        | ISO 3166-1 alpha-2 country code (e.g., `GB`, `DE`, `PL`) |

**Example:** `GET /api/bank/institutions?country=GB`

**Response `200 OK`:**
```json
[
  {
    "id": "MONZO_MONZGB2L",
    "name": "Monzo",
    "bic": "MONZGB2L",
    "transaction_total_days": "90",
    "countries": ["GB"],
    "logo": "https://cdn.nordigen.com/ais/MONZO_MONZGB2L.png"
  }
]
```

---

### POST `/api/bank/agreements/enduser`

Create an end-user agreement (consent) for a specific bank.

**Query Parameters:**
| Parameter       | Type   | Required | Description              |
|-----------------|--------|----------|--------------------------|
| `institutionId` | String | ✅        | Institution ID from `/api/bank/institutions` |

**Request Body:**
```json
["details", "balances", "transactions"]
```

**Response `201 Created`:** `EndUserAgreement` object from Nordigen.

---

### POST `/api/bank/requisitions`

Create a requisition (authorization link) that the user must open in a browser to authorize bank access.

**Query Parameters:**
| Parameter       | Type   | Required | Description                            |
|-----------------|--------|----------|----------------------------------------|
| `institutionId` | String | ✅        | Institution ID                         |
| `redirect`      | String | ✅        | Redirect URL after user authorization  |
| `agreement`     | String | ✅        | Agreement ID from the previous step    |

**Response `201 Created`:**
```json
{
  "id": "requisition-uuid",
  "status": "CR",
  "link": "https://ob.nordigen.com/ob/link/<...>",
  "accounts": []
}
```

> Open the `link` URL in a WebView or browser for the user to authenticate with their bank.

---

### GET `/api/bank/requisitions/{requisitionId}`

Get the status of a requisition. After user authorizes, `accounts` will be populated.

**Path Parameters:**
| Parameter        | Type   | Description          |
|------------------|--------|----------------------|
| `requisitionId`  | String | The requisition's ID |

**Response `200 OK`:**
```json
{
  "id": "requisition-uuid",
  "status": "LN",
  "link": "...",
  "accounts": ["account-id-1", "account-id-2"]
}
```

---

### DELETE `/api/bank/requisitions/{requisitionId}`

Delete (revoke) a requisition and all associated bank consent.

**Response `200 OK`:** `"Requisition deleted successfully"`

---

### GET `/api/bank/accounts/{accountId}/details`

Get details for a specific bank account (name, IBAN, currency, etc.).

**Path Parameters:**
| Parameter   | Type   | Description          |
|-------------|--------|----------------------|
| `accountId` | String | The bank account ID  |

**Response `200 OK`:** `AccountDetails` object (from Nordigen).

---

## 8. File Upload

**Base path:** `/api/files`  
🔒 All endpoints require authentication.  
Used for uploading transaction receipts/images.

---

### POST `/api/files/upload`

Upload a file (e.g., a receipt image).

**Content-Type:** `multipart/form-data`

**Form Data:**
| Field  | Type | Description        |
|--------|------|--------------------|
| `file` | File | The file to upload (max 10 MB) |

**Response `201 Created`:**
```json
{
  "fileUrl": "uploads/unique-filename.jpg",
  "message": "File uploaded successfully"
}
```

> Store the returned `fileUrl` in the transaction's `pic_url` field.

**Error `400 Bad Request`:** `"Upload failed: <reason>"`

---

### GET `/api/files/{filename}`

Download or view a previously uploaded file.

**Path Parameters:**
| Parameter  | Type   | Description          |
|------------|--------|----------------------|
| `filename` | String | The filename returned by `/upload` |

**Response `200 OK`:** File binary data with `Content-Disposition: attachment`.

**Error `404 Not Found`:** `"File not found: <reason>"`

---

### DELETE `/api/files/{filename}`

Delete an uploaded file.

**Response `200 OK`:** `"File deleted successfully"`

**Error `400 Bad Request`:** `"Delete failed: <reason>"`

---

## 9. Data Models

### `UserDTO`
| Field        | Type              | Description                    |
|--------------|-------------------|--------------------------------|
| `uid`        | String            | Unique user ID (UUID)          |
| `name`       | String            | Full name                      |
| `email`      | String            | Email address                  |
| `phoneNumber`| String            | Phone number (E.164 format)    |
| `pictureUrl` | String            | Profile picture URL            |
| `fcmToken`   | String            | FCM push notification token    |
| `createdAt`  | LocalDateTime     | Account creation timestamp     |
| `updatedAt`  | LocalDateTime     | Last update timestamp          |
| `walletIds`  | List\<String\>    | IDs of wallets user belongs to |

---

### `WalletDTO`
| Field          | Type           | Description                    |
|----------------|----------------|--------------------------------|
| `id`           | String         | Unique wallet ID               |
| `title`        | String         | Wallet display name            |
| `creator_id`   | String         | UID of the wallet creator      |
| `amount`       | Double         | Current wallet balance         |
| `currency`     | String         | ISO 4217 currency code (e.g., `USD`) |
| `creation_date`| LocalDateTime  | Wallet creation timestamp      |
| `users`        | List\<String\> | List of member user IDs        |
| `walletUsers`  | List\<UserDTO\>| Full user objects for members  |
| `bAccount`     | BAccountDTO    | Linked bank account (nullable) |

---

### `TransactionDTO`
| Field              | Type          | Description                         |
|--------------------|---------------|-------------------------------------|
| `id`               | String        | Unique transaction ID               |
| `walletId`         | String        | Wallet this transaction belongs to  |
| `type`             | String        | `EXPENSE` or `INCOME`               |
| `category`         | String        | Category (e.g., `Food`, `Transport`)|
| `user_id`          | String        | ID of the user who created it       |
| `user_name`        | String        | Name of the user who created it     |
| `title`            | String        | Transaction description             |
| `amount`           | Double        | Transaction amount (positive)       |
| `currency`         | String        | ISO 4217 currency code              |
| `date`             | LocalDateTime | Date/time of the transaction        |
| `pic_url`          | String        | URL of attached receipt image       |
| `isCashTransaction`| Boolean       | Whether it's a cash transaction     |
| `alreadyAdded`     | Boolean       | Used for bank import deduplication  |

---

### `InvitationDTO`
| Field                        | Type          | Description                       |
|------------------------------|---------------|-----------------------------------|
| `id`                         | String        | Unique invitation ID              |
| `creator_id`                 | String        | UID of the inviting user          |
| `creator_name`               | String        | Name of the inviting user         |
| `wallet_title`               | String        | Title of the wallet to join       |
| `invited_person_phone_number`| String        | Phone number that was invited     |
| `creator_pic_url`            | String        | Profile picture of the inviter    |
| `date`                       | LocalDateTime | Invitation creation time          |
| `status`                     | String        | `PENDING`, `ACCEPTED`, `DECLINED` |

---

### `BAccountDTO`
| Field           | Type   | Description                        |
|-----------------|--------|------------------------------------|
| `id`            | String | Internal bank account record ID    |
| `requisitionId` | String | Nordigen requisition ID            |
| `accountId`     | String | Nordigen account ID                |
| `institutionId` | String | Bank/institution ID                |
| `status`        | String | Account linking status             |
| `accountName`   | String | Account display name               |
| `accountNumber` | String | IBAN or account number             |
| `bankName`      | String | Bank display name                  |
| `currency`      | String | Account currency                   |

---

## 10. Android Integration Guide

### Setup

#### Dependencies (`build.gradle`)
```groovy
// Retrofit + OkHttp
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
```

---

### Network Client Setup

```kotlin
// ApiClient.kt
object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8080/api/" // Android emulator localhost
    // For production: "https://your-domain.com/api/"

    private var token: String? = null

    fun setToken(newToken: String) {
        token = newToken
    }

    fun clearToken() {
        token = null
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        token?.let { requestBuilder.addHeader("Authorization", "Bearer $it") }
        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
```

---

### API Service Interfaces

```kotlin
// AuthApiService.kt
interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Header("Authorization") token: String): Response<AuthResponse>
}

// UserApiService.kt
interface UserApiService {
    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserDTO>

    @GET("users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<UserDTO>

    @PUT("users/me")
    suspend fun updateCurrentUser(@Body user: UserDTO): Response<UserDTO>

    @POST("users/fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): Response<String>
}

// WalletApiService.kt
interface WalletApiService {
    @POST("wallets")
    suspend fun createWallet(@Body wallet: WalletDTO): Response<WalletDTO>

    @GET("wallets")
    suspend fun getUserWallets(): Response<List<WalletDTO>>

    @GET("wallets/{walletId}")
    suspend fun getWallet(@Path("walletId") walletId: String): Response<WalletDTO>

    @PUT("wallets/{walletId}")
    suspend fun updateWallet(@Path("walletId") walletId: String, @Body wallet: WalletDTO): Response<WalletDTO>

    @DELETE("wallets/{walletId}")
    suspend fun deleteWallet(@Path("walletId") walletId: String): Response<String>

    @POST("wallets/{walletId}/users/{userIdToAdd}")
    suspend fun addUserToWallet(
        @Path("walletId") walletId: String,
        @Path("userIdToAdd") userIdToAdd: String
    ): Response<String>

    @DELETE("wallets/{walletId}/users/{userIdToRemove}")
    suspend fun removeUserFromWallet(
        @Path("walletId") walletId: String,
        @Path("userIdToRemove") userIdToRemove: String
    ): Response<String>
}

// TransactionApiService.kt
interface TransactionApiService {
    @POST("wallets/{walletId}/transactions")
    suspend fun createTransaction(
        @Path("walletId") walletId: String,
        @Body transaction: TransactionDTO
    ): Response<TransactionDTO>

    @GET("wallets/{walletId}/transactions")
    suspend fun getWalletTransactions(@Path("walletId") walletId: String): Response<List<TransactionDTO>>

    @GET("wallets/{walletId}/transactions/{transactionId}")
    suspend fun getTransaction(
        @Path("walletId") walletId: String,
        @Path("transactionId") transactionId: String
    ): Response<TransactionDTO>

    @PUT("wallets/{walletId}/transactions/{transactionId}")
    suspend fun updateTransaction(
        @Path("walletId") walletId: String,
        @Path("transactionId") transactionId: String,
        @Body transaction: TransactionDTO
    ): Response<TransactionDTO>

    @DELETE("wallets/{walletId}/transactions/{transactionId}")
    suspend fun deleteTransaction(
        @Path("walletId") walletId: String,
        @Path("transactionId") transactionId: String
    ): Response<String>
}

// InvitationApiService.kt
interface InvitationApiService {
    @POST("invitations")
    suspend fun createInvitation(@Body invitation: InvitationDTO): Response<InvitationDTO>

    @GET("invitations/phone/{phoneNumber}")
    suspend fun getInvitationsByPhone(@Path("phoneNumber") phoneNumber: String): Response<List<InvitationDTO>>

    @GET("invitations/status/{status}")
    suspend fun getInvitationsByStatus(@Path("status") status: String): Response<List<InvitationDTO>>

    @POST("invitations/{invitationId}/accept")
    suspend fun acceptInvitation(@Path("invitationId") invitationId: String): Response<InvitationDTO>

    @POST("invitations/{invitationId}/decline")
    suspend fun declineInvitation(@Path("invitationId") invitationId: String): Response<InvitationDTO>
}

// PredictionApiService.kt
interface PredictionApiService {
    @GET("predictions/wallet/{walletId}/next-month")
    suspend fun getNextMonthForecast(@Path("walletId") walletId: String): Response<ForecastResponseDTO>

    @GET("predictions/wallet/{walletId}/end-of-month-projection")
    suspend fun getEndOfMonthProjection(@Path("walletId") walletId: String): Response<MtdProjectionDTO>

    @GET("predictions/user/me/categories")
    suspend fun getCategoryForecasts(): Response<List<CategoryForecastDTO>>

    @GET("predictions/user/me/recurring")
    suspend fun getRecurringTransactions(): Response<List<RecurringTransactionDTO>>

    @GET("predictions/user/me/anomalies")
    suspend fun getAnomalies(): Response<List<AnomalyDTO>>

    @GET("predictions/wallet/{walletId}/history")
    suspend fun getSpendingHistory(
        @Path("walletId") walletId: String,
        @Query("periodType") periodType: String = "MONTHLY",
        @Query("months") months: Int = 12
    ): Response<SpendingHistoryDTO>
}

// FileApiService.kt
interface FileApiService {
    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): Response<UploadResponse>

    @GET("files/{filename}")
    suspend fun downloadFile(@Path("filename") filename: String): Response<ResponseBody>

    @DELETE("files/{filename}")
    suspend fun deleteFile(@Path("filename") filename: String): Response<String>
}
```

---

### Service Instances (Singleton)

```kotlin
// ApiServiceProvider.kt
object ApiServiceProvider {
    val authService: AuthApiService by lazy {
        ApiClient.retrofit.create(AuthApiService::class.java)
    }
    val userService: UserApiService by lazy {
        ApiClient.retrofit.create(UserApiService::class.java)
    }
    val walletService: WalletApiService by lazy {
        ApiClient.retrofit.create(WalletApiService::class.java)
    }
    val transactionService: TransactionApiService by lazy {
        ApiClient.retrofit.create(TransactionApiService::class.java)
    }
    val invitationService: InvitationApiService by lazy {
        ApiClient.retrofit.create(InvitationApiService::class.java)
    }
    val predictionService: PredictionApiService by lazy {
        ApiClient.retrofit.create(PredictionApiService::class.java)
    }
    val fileService: FileApiService by lazy {
        ApiClient.retrofit.create(FileApiService::class.java)
    }
}
```

---

### Authentication Flow

```kotlin
// In your LoginViewModel or Repository
suspend fun login(email: String, password: String): Result<AuthResponse> {
    return try {
        val response = ApiServiceProvider.authService.login(AuthRequest(email, password))
        if (response.isSuccessful) {
            val body = response.body()!!
            // 1. Store the token
            ApiClient.setToken(body.token)
            // 2. Persist token in SharedPreferences or DataStore
            prefs.edit().putString("jwt_token", body.token).apply()
            prefs.edit().putString("refresh_token", body.refreshToken).apply()
            Result.success(body)
        } else {
            Result.failure(Exception(response.errorBody()?.string()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// On app start, restore token
fun restoreSession() {
    val token = prefs.getString("jwt_token", null)
    token?.let { ApiClient.setToken(it) }
}

// On logout
fun logout() {
    ApiClient.clearToken()
    prefs.edit().clear().apply()
}
```

---

### Typical App Startup Sequence

```
1. App opens
   └─> restoreSession() — load saved JWT from preferences

2. If no token → Navigate to Login/Register screen
   └─> POST /api/auth/login
   └─> Store token, set in ApiClient
   └─> POST /api/users/fcm-token  (register device for push notifications)

3. Home screen loads
   └─> GET /api/wallets            (list all user wallets)
   └─> GET /api/invitations/phone/{userPhone}  (check pending invites)

4. User opens a wallet
   └─> GET /api/wallets/{walletId}
   └─> GET /api/wallets/{walletId}/transactions

5. User views analytics
   └─> GET /api/predictions/wallet/{walletId}/history
   └─> GET /api/predictions/wallet/{walletId}/next-month
   └─> GET /api/predictions/wallet/{walletId}/end-of-month-projection
   └─> GET /api/predictions/user/me/anomalies
   └─> GET /api/predictions/user/me/recurring
```

---

### Handling Token Expiry (401 Responses)

Add an OkHttp **Authenticator** to automatically refresh tokens on 401:

```kotlin
val tokenAuthenticator = Authenticator { _, response ->
    if (response.code == 401) {
        val currentToken = prefs.getString("jwt_token", null) ?: return@Authenticator null
        // Synchronous refresh call
        val refreshResponse = runBlocking {
            ApiServiceProvider.authService.refresh("Bearer $currentToken")
        }
        if (refreshResponse.isSuccessful) {
            val newToken = refreshResponse.body()?.token ?: return@Authenticator null
            ApiClient.setToken(newToken)
            prefs.edit().putString("jwt_token", newToken).apply()
            // Retry the original request with the new token
            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        } else {
            // Refresh failed — force logout
            logout()
            null
        }
    } else null
}

// Add to OkHttpClient builder:
.authenticator(tokenAuthenticator)
```

---

### Uploading a File (Receipt Image)

```kotlin
suspend fun uploadReceipt(uri: Uri, context: Context): String? {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val bytes = inputStream.readBytes()
    val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
    val part = MultipartBody.Part.createFormData("file", "receipt.jpg", requestBody)

    val response = ApiServiceProvider.fileService.uploadFile(part)
    return if (response.isSuccessful) response.body()?.fileUrl else null
}
```

---

### Error Handling Utility

```kotlin
suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Result<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Unknown error (${response.code()})"
            Result.failure(Exception(errorMsg))
        }
    } catch (e: IOException) {
        Result.failure(Exception("Network error. Please check your connection."))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// Usage example:
val result = safeApiCall { ApiServiceProvider.walletService.getUserWallets() }
result.onSuccess { wallets -> /* update UI */ }
result.onFailure { error -> /* show error message */ }
```

---

### JWT Token Notes

- Tokens expire after **24 hours** (86,400,000 ms, configurable via `jwt.expiration` in `application.properties`).
- Store the token securely using **EncryptedSharedPreferences** (Android Jetpack Security).
- Always send the token in the `Authorization: Bearer <token>` header for all protected endpoints.
- The user's ID is embedded in the JWT; the backend extracts it automatically — you do **not** need to send the user ID in requests, only the token.

---

*Documentation generated for Xpense Backend v0.0.1 | May 2026*

