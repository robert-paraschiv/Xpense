# Firestore Invalid Document Reference Error - Fix Summary

> **Note**: This fix also addresses navigation issues to `AddTransactionFragment`. See [NAVIGATION_ERROR_FIX.md](./NAVIGATION_ERROR_FIX.md) for details.

## Problem
The app was crashing with the following error:
```
java.lang.IllegalArgumentException: Invalid document reference. 
Document references must have an even number of segments, but Wallets has 1
```

**Root Cause**: 
- In `HomeFragment.kt`, when the wallet was null or the wallet ID was empty/null, the code was passing an empty string `""` to database utility methods using the pattern `wallet?.id ?: ""`.
- This empty string was then used to create a Firestore document reference like `walletsRef.document("")`, which is invalid because Firestore document paths must have an even number of segments (collection/document/collection/document).

## Solution Applied

### 1. HomeFragment.kt - Conditional Data Loading
**File**: `app/src/main/java/com/rokudo/xpense/fragments/HomeFragment.kt`

**Changes**:
- Modified the code to only load transactions, statistics, and other wallet-dependent data when a valid wallet ID exists
- Wrapped LiveData creations in `remember` blocks with null/empty checks
- Returns empty `MutableLiveData` when wallet ID is null or empty instead of making database calls

**Before**:
```kotlin
val barChartTransactions by transactionViewModel
    .loadTransactionsDateInterval(wallet?.id ?: "", startDate, endDate)
    .observeAsState(emptyList())
```

**After**:
```kotlin
val barChartTransactionsLiveData = remember(wallet?.id) {
    if (wallet?.id != null && wallet.id.isNotEmpty()) {
        transactionViewModel.loadTransactionsDateInterval(wallet.id, startDate, endDate)
    } else {
        androidx.lifecycle.MutableLiveData(emptyList())
    }
}
val barChartTransactions by barChartTransactionsLiveData.observeAsState(emptyList())
```

Similar changes were applied to:
- `loadTransactionsDateInterval()` call
- `listenForStatisticsDoc()` call  
- `loadLatestTransaction()` call

### 2. DatabaseUtils.java - Input Validation
**File**: `app/src/main/java/com/rokudo/xpense/utils/DatabaseUtils.java`

**Changes**:
Added null and empty string validation to all methods that accept `walletId` parameters:

- `getTransactionsRef(String walletId)`
- `getYearReference(String walletId, String year)`
- `getMonthsReference(String walletId, String year)`

**Example**:
```java
public static CollectionReference getTransactionsRef(String walletId) {
    if (walletId == null || walletId.isEmpty()) {
        throw new IllegalArgumentException("Wallet ID cannot be null or empty");
    }
    return walletsRef.document(walletId).collection("Transactions");
}
```

### 3. BankAccFragment.kt - Navigation Validation
**File**: `app/src/main/java/com/rokudo/xpense/fragments/BankAccFragment.kt`

**Changes**:
Added validation before navigating to `AddTransactionFragment` to ensure wallet ID and currency are not null or empty:

```kotlin
val walletId = bAccount.walletIds?.firstOrNull()
val currency = bAccount.linked_acc_currency

if (!walletId.isNullOrEmpty() && !currency.isNullOrEmpty()) {
    val action = BankAccFragmentDirections
        .actionBAccountDetailsFragmentToAddTransactionFragment(
            walletId,
            currency,
            transaction,
            false
        )
    findNavController().navigate(action)
} else {
    Toast.makeText(requireContext(), "Please link this account to a wallet first", Toast.LENGTH_SHORT).show()
}
```

## Impact
- ✅ Prevents Firestore crashes when wallet ID is null or empty
- ✅ Prevents navigation crashes due to missing required arguments
- ✅ Provides clear error messages if invalid wallet IDs are passed
- ✅ Improves app stability on HomeFragment when no wallet is selected
- ✅ Improves user experience with helpful toast messages
- ✅ No breaking changes to existing functionality

## Testing Recommendations
1. **Test without a wallet selected**: Launch the app without selecting a wallet and verify the home screen loads without crashing
2. **Test wallet selection**: Verify that selecting a wallet properly loads transactions and statistics
3. **Test wallet switching**: Switch between wallets and ensure data loads correctly
4. **Test first-time user**: Verify the experience for a user with no wallets created

## Date Fixed
March 6, 2026

