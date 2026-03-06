package com.rokudo.xpense.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rokudo.xpense.R
import com.rokudo.xpense.models.ExpenseCategory
import com.rokudo.xpense.models.Transaction
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transaction: Transaction?,
    isEditMode: Boolean,
    selectedCategory: ExpenseCategory?,
    onBackClick: () -> Unit,
    onSaveClick: (amount: String, title: String, date: Date, category: ExpenseCategory?, type: String, isCash: Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onCategoryChange: (ExpenseCategory?) -> Unit
) {
    var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var title by remember { mutableStateOf(transaction?.title ?: "") }
    var selectedDate by remember { mutableStateOf(transaction?.date ?: Date()) }
    var selectedType by remember { mutableStateOf(transaction?.type ?: Transaction.EXPENSE_TYPE) }
    var isCashTransaction by remember { mutableStateOf(transaction?.cashTransaction ?: false) }

    var amountError by remember { mutableStateOf<String?>(null) }
    var showCategoryError by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Transaction" else "Add Transaction"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF9FCFF)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Type Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == Transaction.EXPENSE_TYPE,
                    onClick = {
                        selectedType = Transaction.EXPENSE_TYPE
                        onCategoryChange(null)
                    },
                    label = { Text("Expense") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == Transaction.INCOME_TYPE,
                    onClick = {
                        selectedType = Transaction.INCOME_TYPE
                        onCategoryChange(ExpenseCategory("Income"))
                    },
                    label = { Text("Income") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == Transaction.TRANSFER_TYPE,
                    onClick = {
                        selectedType = Transaction.TRANSFER_TYPE
                        onCategoryChange(ExpenseCategory("Transfer"))
                    },
                    label = { Text("Transfer") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Category Selection (only for Expense)
            if (selectedType == Transaction.EXPENSE_TYPE) {
                Card(
                    onClick = onCategoryClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF9FCFF)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedCategory != null) {
                            Icon(
                                painter = painterResource(id = selectedCategory.resourceId),
                                contentDescription = selectedCategory.name,
                                tint = Color(selectedCategory.color),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(selectedCategory.name, fontWeight = FontWeight.Medium)
                        } else {
                            Text("Select Category", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (showCategoryError && selectedCategory == null) {
                    Text(
                        "Please select a category",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }

            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    amountError = null
                },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                isError = amountError != null,
                supportingText = amountError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Date Picker (simplified)
            OutlinedTextField(
                value = dateFormat.format(selectedDate),
                onValueChange = {},
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_date_range_24),
                        contentDescription = "Select Date"
                    )
                }
            )

            // Cash Transaction Switch (only for Expense)
            if (selectedType == Transaction.EXPENSE_TYPE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cash Transaction")
                    Switch(
                        checked = isCashTransaction,
                        onCheckedChange = { isCashTransaction = it }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    // Validation
                    var hasError = false

                    if (selectedType == Transaction.EXPENSE_TYPE && selectedCategory == null) {
                        showCategoryError = true
                        hasError = true
                    }

                    if (amount.trim().isEmpty()) {
                        amountError = "Please input your amount"
                        hasError = true
                    } else {
                        try {
                            val amountValue = amount.toDouble()
                            if (amountValue <= 0) {
                                amountError = "Amount must be greater than zero"
                                hasError = true
                            }
                        } catch (_: NumberFormatException) {
                            amountError = "Invalid amount format"
                            hasError = true
                        }
                    }

                    if (!hasError) {
                        onSaveClick(
                            amount,
                            title,
                            selectedDate,
                            when(selectedType) {
                                Transaction.INCOME_TYPE -> ExpenseCategory("Income")
                                Transaction.TRANSFER_TYPE -> ExpenseCategory("Transfer")
                                else -> selectedCategory
                            },
                            selectedType,
                            isCashTransaction
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (isEditMode) "Update Transaction" else "Save Transaction")
            }

            // Delete Button (only in edit mode)
            if (isEditMode && transaction != null) {
                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Transaction")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    AddTransactionScreen(
        transaction = null,
        isEditMode = false,
        selectedCategory = ExpenseCategory("Food"),
        onBackClick = {},
        onSaveClick = { _, _, _, _, _, _ -> },
        onDeleteClick = {},
        onCategoryClick = {},
        onCategoryChange = {}
    )
}

@Preview(showBackground = true)
@Composable
fun EditTransactionScreenPreview() {
    val mockTransaction = Transaction().apply {
        amount = 150.0
        title = "Grocery Shopping"
        type = Transaction.EXPENSE_TYPE
        category = "Shopping"
        date = Date()
    }

    AddTransactionScreen(
        transaction = mockTransaction,
        isEditMode = true,
        selectedCategory = ExpenseCategory("Shopping"),
        onBackClick = {},
        onSaveClick = { _, _, _, _, _, _ -> },
        onDeleteClick = {},
        onCategoryClick = {},
        onCategoryChange = {}
    )
}

