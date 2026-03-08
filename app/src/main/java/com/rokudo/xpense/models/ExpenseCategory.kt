package com.rokudo.xpense.models

data class ExpenseCategory(
    var name: String = "",
    var resourceId: Int? = null,
    var amount: Double = 0.0,
    var transactionList: List<Transaction>? = null,
    var color: Int? = null
) {
    constructor(name: String, resourceId: Int, color: Int) : this(
        name = name,
        resourceId = resourceId,
        amount = 0.0,
        transactionList = null,
        color = color
    )

    constructor(name: String, transactionList: List<Transaction>, resourceId: Int, amount: Double) : this(
        name = name,
        resourceId = resourceId,
        amount = amount,
        transactionList = transactionList,
        color = null
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExpenseCategory) return false
        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()
}

