package com.rokudo.xpense.data.retrofit.models

data class Account(
    var resourceId: String = "",
    var iban: String = "",
    var currency: String = "",
    var ownerName: String = "",
    var name: String = "",
    var product: String = "",
    var cashAccountType: String = ""
)

data class AccountDetails(
    var account: Account? = null,
    var account_id: String = ""
)

data class Balance(
    var balanceAmount: Map<String, String>? = null,
    var balanceType: String = "",
    var referenceDay: String = "",
    var lastChangeDateTime: String = ""
)

data class Balances(
    var balances: Array<Balance>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Balances) return false
        return balances.contentEquals(other.balances)
    }
    override fun hashCode(): Int = balances?.contentHashCode() ?: 0
}

data class BankTransaction(
    var transactionId: String = "",
    var endToEndId: String = "",
    var bookingDate: String = "",
    var valueDate: String = "",
    var transactionAmount: TransactionAmount? = null,
    var remittanceInformationUnstructured: String = "",
    var proprietaryBankTransactionCode: String = "",
    var internalTransactionId: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BankTransaction) return false
        return transactionId == other.transactionId && internalTransactionId == other.internalTransactionId
    }
    override fun hashCode(): Int = 31 * transactionId.hashCode() + internalTransactionId.hashCode()
}

data class DeleteResponse(
    var summary: String = "",
    var detail: String = "",
    var status_code: Int? = null
)

data class EndUserAgreement(
    var id: String = "",
    var created: String = "",
    var max_historical_days: Int? = null,
    var access_valid_for_days: Int? = null,
    var access_scope: Array<String>? = null,
    var accepted: String = "",
    var institution_id: String = ""
)

data class EUAResponse(
    var count: Int? = null,
    var next: String? = null,
    var previous: String? = null,
    var results: List<EndUserAgreement>? = null
)

data class Institution(
    var id: String = "",
    var name: String = "",
    var bic: String = "",
    var transaction_total_days: Int? = null,
    var countries: Array<String>? = null,
    var logo: String = "",
    var payments: Boolean? = null
)

data class Requisition(
    var id: String = "",
    var created: String = "",
    var redirect: String = "",
    var status: String = "",
    var institution_id: String = "",
    var agreement: String = "",
    var reference: String = "",
    var accounts: Array<String> = emptyArray(),
    var user_language: String = "",
    var link: String = "",
    var ssn: String = "",
    var account_selection: Boolean? = null,
    var redirect_immediate: Boolean? = null
)

data class RequisitionsResult(
    var count: Int? = null,
    var next: String? = null,
    var previous: String? = null,
    var results: Array<Requisition>? = null
)

data class Token(
    var access: String? = null,
    var access_expires: Int? = null,
    var refresh: String? = null,
    var refresh_expires: Int? = null
)

data class TransactionAmount(
    var currency: String = "",
    var amount: Float? = null
)

data class Transactions(
    var booked: Array<BankTransaction>? = null,
    var pending: Array<BankTransaction>? = null
)

data class TransactionsResponse(
    var transactions: Transactions? = null
)

