package com.rokudo.xpense.fragments

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rokudo.xpense.R
import com.rokudo.xpense.components.LoadingState
import com.rokudo.xpense.components.XpenseCard
import com.rokudo.xpense.data.viewmodels.AddTransactionEvent
import com.rokudo.xpense.data.viewmodels.AddTransactionState
import com.rokudo.xpense.models.ExpenseCategory
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.ui.theme.XpenseTheme
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Custom Toolbar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
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
                            modifier = Modifier.size(25.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = if (state.isEditMode) "Edit Transaction" else "New Transaction",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedVisibility(
                        visible = state.isEditMode,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(onClick = { onEvent(AddTransactionEvent.OnDeleteClick) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_round_delete_24),
                                contentDescription = "Delete",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.error
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

                // Transaction Type Chips
                TransactionTypeSelector(
                    selectedType = state.type,
                    onTypeChange = { onEvent(AddTransactionEvent.OnTypeChange(it)) }
                )

                // Title Input
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { onEvent(AddTransactionEvent.OnTitleChange(it)) },
                    label = { Text("Transaction Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    shape = MaterialTheme.shapes.medium
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
                    textStyle = TextStyle(fontSize = 22.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                // Category error message
                AnimatedVisibility(
                    visible = state.showCategoryError && state.selectedCategory == null && state.type == Transaction.EXPENSE_TYPE,
                    enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                    exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                ) {
                    Text(
                        "Please select a category first",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // Category Selection Card (only for Expense)
                AnimatedVisibility(
                    visible = state.type == Transaction.EXPENSE_TYPE,
                    enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                    exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                ) {
                    CategorySelectionCard(
                        selectedCategory = state.selectedCategory,
                        onClick = { onEvent(AddTransactionEvent.OnCategoryClick) }
                    )
                }

                // Cash Transaction Switch (only for Expense)
                AnimatedVisibility(
                    visible = state.type == Transaction.EXPENSE_TYPE,
                    enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                    exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Cash transaction",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = state.isCashTransaction,
                            onCheckedChange = { onEvent(AddTransactionEvent.OnCashTransactionChange(it)) }
                        )
                    }
                }

                // Date Picker
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
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        disabledContainerColor = MaterialTheme.colorScheme.background,
                    )
                )
            }

            // Save Button
            Button(
                onClick = { onEvent(AddTransactionEvent.OnSaveClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    if (state.isEditMode) "Update Transaction" else "Add Transaction",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Loading overlay
        LoadingState(
            isLoading = state.isLoading,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun TransactionTypeSelector(
    selectedType: String,
    onTypeChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        listOf(
            Transaction.INCOME_TYPE to "Income",
            Transaction.EXPENSE_TYPE to "Expense",
            Transaction.TRANSFER_TYPE to "Transfer"
        ).forEachIndexed { index, (type, label) ->
            if (index > 0) Spacer(modifier = Modifier.width(12.dp))
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeChange(type) },
                label = {
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                },
                modifier = Modifier.height(52.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun CategorySelectionCard(
    selectedCategory: ExpenseCategory?,
    onClick: () -> Unit
) {
    XpenseCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedCategory != null) {
                    Icon(
                        painter = painterResource(id = selectedCategory.resourceId),
                        contentDescription = selectedCategory.name,
                        tint = androidx.compose.ui.graphics.Color(selectedCategory.color),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        selectedCategory.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        "Select Category",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tap to change",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    XpenseTheme(dynamicColor = false) {
        AddTransactionScreen(
            state = AddTransactionState(),
            onEvent = {}
        )
    }
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

    XpenseTheme(dynamicColor = false) {
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
}
