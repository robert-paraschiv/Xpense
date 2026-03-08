package com.rokudo.xpense.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rokudo.xpense.R
import com.rokudo.xpense.data.viewmodels.AddTransactionEvent
import com.rokudo.xpense.data.viewmodels.AddTransactionState
import com.rokudo.xpense.models.ExpenseCategory
import com.rokudo.xpense.models.Transaction
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    state: AddTransactionState,
    onEvent: (AddTransactionEvent) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEBF1F8))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Custom Toolbar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFEBF1F8)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onEvent(AddTransactionEvent.OnBackClick) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_round_arrow_back_ios_24),
                            contentDescription = "Back",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                    Text(
                        text = if (state.isEditMode) "Edit Transaction" else "New Transaction",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.isEditMode) {
                        IconButton(onClick = { onEvent(AddTransactionEvent.OnDeleteClick) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_round_delete_24),
                                contentDescription = "Delete",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Spacer(modifier = Modifier.height(0.dp))

                // Transaction Type Chips - Centered
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = state.type == Transaction.INCOME_TYPE,
                        onClick = { onEvent(AddTransactionEvent.OnTypeChange(Transaction.INCOME_TYPE)) },
                        label = {
                            Text(
                                "Income",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        },
                        modifier = Modifier.height(60.dp)
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    FilterChip(
                        selected = state.type == Transaction.EXPENSE_TYPE,
                        onClick = { onEvent(AddTransactionEvent.OnTypeChange(Transaction.EXPENSE_TYPE)) },
                        label = {
                            Text(
                                "Expense",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        },
                        modifier = Modifier.height(60.dp)
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    FilterChip(
                        selected = state.type == Transaction.TRANSFER_TYPE,
                        onClick = { onEvent(AddTransactionEvent.OnTypeChange(Transaction.TRANSFER_TYPE)) },
                        label = {
                            Text(
                                "Transfer",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        },
                        modifier = Modifier.height(60.dp)
                    )
                }

                // Title Input
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { onEvent(AddTransactionEvent.OnTitleChange(it)) },
                    label = { Text("Transaction Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9FCFF),
                        unfocusedContainerColor = Color(0xFFF9FCFF),
                        disabledContainerColor = Color(0xFFF9FCFF),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    shape = MaterialTheme.shapes.medium.copy(
                        topStart = androidx.compose.foundation.shape.CornerSize(18.dp),
                        topEnd = androidx.compose.foundation.shape.CornerSize(18.dp),
                        bottomStart = androidx.compose.foundation.shape.CornerSize(18.dp),
                        bottomEnd = androidx.compose.foundation.shape.CornerSize(18.dp)
                    )
                )

                // Amount Input
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = { onEvent(AddTransactionEvent.OnAmountChange(it)) },
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.amountError != null,
                    supportingText = state.amountError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 22.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9FCFF),
                        unfocusedContainerColor = Color(0xFFF9FCFF),
                        disabledContainerColor = Color(0xFFF9FCFF),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    shape = MaterialTheme.shapes.medium.copy(
                        topStart = androidx.compose.foundation.shape.CornerSize(18.dp),
                        topEnd = androidx.compose.foundation.shape.CornerSize(18.dp),
                        bottomStart = androidx.compose.foundation.shape.CornerSize(18.dp),
                        bottomEnd = androidx.compose.foundation.shape.CornerSize(18.dp)
                    )
                )

                // Category error message
                if (state.showCategoryError && state.selectedCategory == null && state.type == Transaction.EXPENSE_TYPE) {
                    Text(
                        "Please select a category first",
                        color = Color(0xFFCC0000),
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // Category Selection Card (only for Expense)
                if (state.type == Transaction.EXPENSE_TYPE) {
                    Card(
                        onClick = { onEvent(AddTransactionEvent.OnCategoryClick) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF9FCFF)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (state.selectedCategory != null) {
                                    Icon(
                                        painter = painterResource(id = state.selectedCategory.resourceId),
                                        contentDescription = state.selectedCategory.name,
                                        tint = Color(state.selectedCategory.color),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        state.selectedCategory.name,
                                        fontWeight = FontWeight.Normal
                                    )
                                } else {
                                    Text("Select Category")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tap to change",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Cash Transaction Switch (only for Expense)
                if (state.type == Transaction.EXPENSE_TYPE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cash transaction")
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = state.isCashTransaction,
                            onCheckedChange = { onEvent(AddTransactionEvent.OnCashTransactionChange(it)) }
                        )
                    }
                }

                // Date Picker Placeholder
                OutlinedTextField(
                    value = dateFormat.format(state.date),
                    onValueChange = {},
                    label = { Text("Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_round_date_range_24),
                            contentDescription = "Select Date"
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    )
                )

            }

            // Save Button
            Button(
                onClick = { onEvent(AddTransactionEvent.OnSaveClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                shape = MaterialTheme.shapes.medium.copy(
                    topStart = androidx.compose.foundation.shape.CornerSize(10.dp),
                    topEnd = androidx.compose.foundation.shape.CornerSize(10.dp),
                    bottomStart = androidx.compose.foundation.shape.CornerSize(10.dp),
                    bottomEnd = androidx.compose.foundation.shape.CornerSize(10.dp)
                )
            ) {
                Text(if (state.isEditMode) "Update Transaction" else "Add Transaction")
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    AddTransactionScreen(
        state = AddTransactionState(),
        onEvent = {}
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
        state = AddTransactionState(
            originalTransaction = mockTransaction,
            isEditMode = true,
            title = "Grocery Shopping",
            amount = "150.0",
            selectedCategory = ExpenseCategory("Shopping")
        ),
        onEvent = {}
    )
}

