package com.rokudo.xpense.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rokudo.xpense.R
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.models.WalletUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWalletScreen(
    wallet: Wallet?,
    currencies: List<String>,
    onBackClick: () -> Unit,
    onSaveClick: (title: String, amount: String, currency: String) -> Unit,
    onDeleteClick: () -> Unit,
    onInviteClick: () -> Unit
) {
    var title by remember { mutableStateOf(wallet?.title ?: "") }
    var amount by remember { mutableStateOf(wallet?.amount?.toString() ?: "") }
    var selectedCurrency by remember { mutableStateOf(wallet?.currency ?: "") }
    var expandedCurrency by remember { mutableStateOf(false) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var currencyError by remember { mutableStateOf<String?>(null) }

    val isEditMode = wallet != null
    val otherUser = wallet?.walletUsers?.firstOrNull { it.userId != com.rokudo.xpense.utils.DatabaseUtils.getCurrentUser().uid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Wallet" else "Add Wallet") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .background(Color(0xFFEBF1F8))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Wallet Title
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null
                },
                label = { Text("Wallet Title") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it) } },
                singleLine = true
            )

            // Wallet Amount
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

            // Currency Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCurrency,
                onExpandedChange = { expandedCurrency = it }
            ) {
                OutlinedTextField(
                    value = selectedCurrency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Currency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCurrency) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    isError = currencyError != null,
                    supportingText = currencyError?.let { { Text(it) } }
                )
                ExposedDropdownMenu(
                    expanded = expandedCurrency,
                    onDismissRequest = { expandedCurrency = false }
                ) {
                    currencies.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency) },
                            onClick = {
                                selectedCurrency = currency
                                expandedCurrency = false
                                currencyError = null
                            }
                        )
                    }
                }
            }

            // Invited Person Card (only in edit mode)
            if (isEditMode) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onInviteClick() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = otherUser?.userPic,
                            contentDescription = "User Picture",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.ic_baseline_person_24),
                            error = painterResource(id = R.drawable.ic_baseline_person_24)
                        )

                        Text(
                            text = otherUser?.userName ?: "Tap to invite a collaborator",
                            fontSize = 14.sp,
                            fontWeight = if (otherUser != null) FontWeight.Normal else FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    // Validation
                    var hasError = false

                    if (title.trim().isEmpty()) {
                        titleError = "Wallet title cannot be empty"
                        hasError = true
                    }

                    if (amount.trim().isEmpty()) {
                        amountError = "Wallet amount cannot be empty"
                        hasError = true
                    }

                    if (selectedCurrency.trim().isEmpty()) {
                        currencyError = "Please select a currency"
                        hasError = true
                    }

                    if (!hasError) {
                        onSaveClick(title, amount, selectedCurrency)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (isEditMode) "Update Wallet" else "Create Wallet")
            }

            // Delete Button (only in edit mode)
            if (isEditMode) {
                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Wallet")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditWalletScreenPreview() {
    val mockWallet = Wallet().apply {
        id = "1"
        title = "Personal Wallet"
        amount = 1500.0
        currency = "$"
    }

    EditWalletScreen(
        wallet = mockWallet,
        currencies = listOf("$", "€", "£", "₹"),
        onBackClick = {},
        onSaveClick = { _, _, _ -> },
        onDeleteClick = {},
        onInviteClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun AddWalletScreenPreview() {
    EditWalletScreen(
        wallet = null,
        currencies = listOf("$", "€", "£", "₹"),
        onBackClick = {},
        onSaveClick = { _, _, _ -> },
        onDeleteClick = {},
        onInviteClick = {}
    )
}

