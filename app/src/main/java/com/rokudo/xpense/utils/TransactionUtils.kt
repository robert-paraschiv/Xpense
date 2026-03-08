package com.rokudo.xpense.utils

import com.rokudo.xpense.models.Transaction
import java.text.SimpleDateFormat
import java.util.*

object TransactionUtils {
    private val simpleDateFormat = SimpleDateFormat("E, MMM d", Locale.getDefault())
    private val checkDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    fun getTransactionDateString(transaction: Transaction): String {
        return if (isTransactionDoneToday(transaction)) "Today"
        else simpleDateFormat.format(transaction.date!!)
    }

    private fun isTransactionDoneToday(transaction: Transaction): Boolean {
        return checkDateFormat.format(Date()) == checkDateFormat.format(transaction.date!!)
    }

    fun isTransactionDifferent(oldTransaction: Transaction, newTransaction: Transaction): Boolean {
        val fmt = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        if (oldTransaction.title.isNullOrEmpty()) return true
        if (oldTransaction.title != newTransaction.title) return true
        if (oldTransaction.amount != newTransaction.amount) return true
        if (oldTransaction.type != newTransaction.type) return true
        if (oldTransaction.category != newTransaction.category) return true
        if (fmt.format(oldTransaction.date!!) != fmt.format(newTransaction.date!!)) return true
        return false
    }

    fun isBankTransactionDifferent(oldTransaction: Transaction, newTransaction: Transaction): Boolean {
        val fmt = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        if (oldTransaction.title.isNullOrEmpty()) return true
        if (oldTransaction.title != newTransaction.title) return true
        if (oldTransaction.amount != newTransaction.amount) return true
        if (fmt.format(oldTransaction.date!!) != fmt.format(newTransaction.date!!)) return true
        return false
    }
}

