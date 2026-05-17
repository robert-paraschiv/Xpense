package com.rokudo.xpense.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.rokudo.xpense.data.repositories.TransactionRepo
import com.rokudo.xpense.models.ExpenseCategory
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.utils.CategoriesUtil
import com.rokudo.xpense.utils.DatabaseUtils
import com.rokudo.xpense.utils.TransactionUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

// --- Contract ---

data class AddTransactionState(
    val amount: String = "",
    val title: String = "",
    val date: Date = Date(),
    val type: String = Transaction.EXPENSE_TYPE,
    val selectedCategory: ExpenseCategory? = null,
    val isCashTransaction: Boolean = false,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val amountError: String? = null,
    val showCategoryError: Boolean = false,
    val walletId: String = "",
    val currency: String = "$",
    val originalTransaction: Transaction? = null
)

sealed class AddTransactionEvent {
    data class Init(val walletId: String, val currency: String, val transaction: Transaction?, val editMode: Boolean) : AddTransactionEvent()
    data class OnAmountChange(val amount: String) : AddTransactionEvent()
    data class OnTitleChange(val title: String) : AddTransactionEvent()
    data class OnDateChange(val date: Date) : AddTransactionEvent()
    data class OnTypeChange(val type: String) : AddTransactionEvent()
    data class OnCategoryChange(val category: ExpenseCategory?) : AddTransactionEvent()
    data class OnCashTransactionChange(val isCash: Boolean) : AddTransactionEvent()
    object OnSaveClick : AddTransactionEvent()
    object OnBackClick : AddTransactionEvent()
    object OnDeleteClick : AddTransactionEvent()
    object OnCategoryClick : AddTransactionEvent()
    object OnDeleteConfirmed : AddTransactionEvent()
}

sealed class AddTransactionEffect {
    data class ShowToast(val message: String) : AddTransactionEffect()
    object NavigateBack : AddTransactionEffect()
    object ShowCategoryDialog : AddTransactionEffect()
    object ShowDeleteConfirmation : AddTransactionEffect()
}

// --- ViewModel ---

class AddTransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = TransactionRepo.instance

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AddTransactionEffect>()
    val effect: SharedFlow<AddTransactionEffect> = _effect.asSharedFlow()

    private var isInitialized = false

    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.Init -> {
                if (isInitialized) return
                isInitialized = true

                val category = if (event.transaction != null) {
                    when {
                        event.transaction.type == Transaction.INCOME_TYPE -> ExpenseCategory("Income")
                        event.transaction.type == Transaction.TRANSFER_TYPE -> ExpenseCategory("Transfer")
                        event.transaction.category != null -> {
                             val catName = event.transaction.category
                             CategoriesUtil.expenseCategoryList.find { it.name == catName } ?: CategoriesUtil.expenseCategoryList.firstOrNull()
                        }
                        else -> CategoriesUtil.expenseCategoryList.firstOrNull()
                    }
                } else {
                     CategoriesUtil.expenseCategoryList.firstOrNull()
                }

                _state.update {
                    it.copy(
                        walletId = event.walletId,
                        currency = event.currency,
                        originalTransaction = event.transaction,
                        isEditMode = event.editMode,
                        amount = event.transaction?.amount?.toString() ?: "",
                        title = event.transaction?.title ?: "",
                        date = event.transaction?.date ?: Date(),
                        type = event.transaction?.type ?: Transaction.EXPENSE_TYPE,
                        isCashTransaction = event.transaction?.isCashTransaction ?: false,
                        selectedCategory = category
                    )
                }
            }
            is AddTransactionEvent.OnAmountChange -> {
                 _state.update { it.copy(amount = event.amount, amountError = null) }
            }
            is AddTransactionEvent.OnTitleChange -> {
                _state.update { it.copy(title = event.title) }
            }
            is AddTransactionEvent.OnDateChange -> {
                _state.update { it.copy(date = event.date) }
            }
            is AddTransactionEvent.OnTypeChange -> {
                _state.update {
                    it.copy(
                        type = event.type,
                        selectedCategory = when(event.type) {
                            Transaction.INCOME_TYPE -> ExpenseCategory("Income")
                            Transaction.TRANSFER_TYPE -> ExpenseCategory("Transfer")
                            else -> if (it.selectedCategory?.name == "Income" || it.selectedCategory?.name == "Transfer") CategoriesUtil.expenseCategoryList.firstOrNull() else it.selectedCategory
                        }
                    )
                }
            }
            is AddTransactionEvent.OnCategoryChange -> {
                _state.update { it.copy(selectedCategory = event.category, showCategoryError = false) }
            }
            is AddTransactionEvent.OnCashTransactionChange -> {
                _state.update { it.copy(isCashTransaction = event.isCash) }
            }
             is AddTransactionEvent.OnCategoryClick -> {
                viewModelScope.launch { _effect.emit(AddTransactionEffect.ShowCategoryDialog) }
            }
            is AddTransactionEvent.OnBackClick -> {
                viewModelScope.launch { _effect.emit(AddTransactionEffect.NavigateBack) }
            }
            is AddTransactionEvent.OnDeleteClick -> {
                viewModelScope.launch { _effect.emit(AddTransactionEffect.ShowDeleteConfirmation) }
            }
            is AddTransactionEvent.OnDeleteConfirmed -> {
                deleteTransaction()
            }
            is AddTransactionEvent.OnSaveClick -> {
                saveTransaction()
            }
        }
    }

    private fun saveTransaction() {
        val currentState = _state.value
        val amount = currentState.amount
        val type = currentState.type
        val selectedCategory = currentState.selectedCategory

        var hasError = false
        if (type == Transaction.EXPENSE_TYPE && selectedCategory == null) {
            _state.update { it.copy(showCategoryError = true) }
            hasError = true
        }

        if (amount.trim().isEmpty()) {
            _state.update { it.copy(amountError = "Please input your amount") }
            hasError = true
        } else {
            try {
                val amountValue = amount.toDouble()
                if (amountValue <= 0) {
                     _state.update { it.copy(amountError = "Amount must be greater than zero") }
                    hasError = true
                }
            } catch (_: NumberFormatException) {
                 _state.update { it.copy(amountError = "Invalid amount format") }
                hasError = true
            }
        }

        if (hasError) return

        _state.update { it.copy(isLoading = true) }

        val transaction = Transaction().apply {
            walletId = currentState.walletId
            this.amount = currentState.amount.toDouble()
            currency = currentState.currency
            this.date = currentState.date
            picUrl = DatabaseUtils.currentUser?.pictureUrl
            user_id = DatabaseUtils.currentUser?.uid ?: ""
            userName = DatabaseUtils.currentUser?.name ?: ""

            this.category = when(type) {
                 Transaction.INCOME_TYPE -> "Income"
                 Transaction.TRANSFER_TYPE -> "Transfer"
                 else -> selectedCategory?.name ?: ""
            }

            this.title = currentState.title
            this.isCashTransaction = currentState.isCashTransaction
            this.type = type
        }

        val original = currentState.originalTransaction

        if (original == null) {
             val documentReference = DatabaseUtils.getTransactionsRef(currentState.walletId).document()
             transaction.id = documentReference.id

             val liveData = repo.addTransaction(transaction)
             var observer: Observer<String>? = null
             observer = Observer { t ->
                 observer?.let { liveData.removeObserver(it as Observer<in String?>) }
                 _state.update { it.copy(isLoading = false) }
                 if (t == "Success") {
                     viewModelScope.launch { _effect.emit(AddTransactionEffect.NavigateBack) }
                 } else {
                      viewModelScope.launch { _effect.emit(AddTransactionEffect.ShowToast("Error adding transaction")) }
                 }
             }
             liveData.observeForever(observer as Observer<in String?>)

        } else {
            if (TransactionUtils.isTransactionDifferent(original, transaction)) {
                transaction.id = if (original.id == null || original.id == "NOTPROVIDED") {
                    DatabaseUtils.getTransactionsRef(currentState.walletId).document().id
                } else {
                    original.id
                }

                val liveData = repo.updateTransaction(transaction)
                var observer: Observer<String>? = null
                observer = Observer { t ->
                    observer?.let { liveData.removeObserver(it as Observer<in String?>) }
                    _state.update { it.copy(isLoading = false) }
                    if (t == "Success") {
                        viewModelScope.launch { _effect.emit(AddTransactionEffect.NavigateBack) }
                    } else {
                        viewModelScope.launch { _effect.emit(AddTransactionEffect.ShowToast("Error updating")) }
                    }
                }
                liveData.observeForever(observer as Observer<in String?>)
            } else {
                _state.update { it.copy(isLoading = false) }
                viewModelScope.launch { _effect.emit(AddTransactionEffect.NavigateBack) }
            }
        }
    }

    private fun deleteTransaction() {
        val currentState = _state.value
        val original = currentState.originalTransaction ?: return

        _state.update { it.copy(isLoading = true) }
        val liveData = repo.deleteTransaction(original.id ?: "", original.walletId ?: "")
        var observer: Observer<Boolean>? = null
        observer = Observer { t ->
            observer?.let { liveData.removeObserver(it) }
            _state.update { it.copy(isLoading = false) }
            if (t == true) {
                 viewModelScope.launch {
                     _effect.emit(AddTransactionEffect.ShowToast("Deleted Successfully"))
                     _effect.emit(AddTransactionEffect.NavigateBack)
                 }
            } else {
                 viewModelScope.launch { _effect.emit(AddTransactionEffect.ShowToast("Error deleting")) }
            }
        }
        liveData.observeForever(observer)
    }
}


