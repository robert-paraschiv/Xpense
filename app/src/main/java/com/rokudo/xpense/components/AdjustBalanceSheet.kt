package com.rokudo.xpense.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustBalanceSheet(
    currentBalance: Double,
    currency: String,
    onApply: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }

    val formatted = DecimalFormat("#.##").format(currentBalance)
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = formatted,
                selection = TextRange(formatted.length)
            )
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Auto-focus the text field
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Icon(
                imageVector = Icons.Rounded.AccountBalanceWallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = "Adjust Balance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Set your current wallet balance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ─── Balance Input ───
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    // Only allow digits and a single decimal point
                    val filtered = newValue.text.filter { it.isDigit() || it == '.' }
                    if (filtered.count { it == '.' } <= 1) {
                        textFieldValue = newValue.copy(text = filtered)
                        errorMessage = null
                    }
                },
                label = { Text("Balance") },
                prefix = {
                    Text(
                        text = "$currency ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                ),
                isError = errorMessage != null,
                supportingText = errorMessage?.let { msg ->
                    { Text(msg) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        submitBalance(textFieldValue.text, onApply) { errorMessage = it }
                    }
                ),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Quick Adjust Chips ───
            Text(
                text = "Quick adjust",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(-100.0, -50.0, +50.0, +100.0).forEach { delta ->
                    val label = if (delta > 0) "+${delta.toInt()}" else "${delta.toInt()}"
                    val isPositive = delta > 0
                    AssistChip(
                        onClick = {
                            val current = textFieldValue.text.toDoubleOrNull() ?: currentBalance
                            val newVal = (current + delta).coerceAtLeast(0.0)
                            val newText = DecimalFormat("#.##").format(newVal)
                            textFieldValue = TextFieldValue(
                                text = newText,
                                selection = TextRange(newText.length)
                            )
                            errorMessage = null
                        },
                        label = {
                            Text(
                                text = label,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isPositive)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            else
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            labelColor = if (isPositive)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        ),
                        border = null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ─── Action Buttons ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        submitBalance(textFieldValue.text, onApply) { errorMessage = it }
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Apply", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun submitBalance(
    text: String,
    onApply: (Double) -> Unit,
    onError: (String) -> Unit
) {
    val parsed = text.toDoubleOrNull()
    if (parsed == null) {
        onError("Please enter a valid number")
    } else {
        onApply(parsed)
    }
}

