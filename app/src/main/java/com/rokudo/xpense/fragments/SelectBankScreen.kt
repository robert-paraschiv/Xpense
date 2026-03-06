package com.rokudo.xpense.fragments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.rokudo.xpense.data.retrofit.models.Institution

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectBankScreen(
    banks: List<Institution>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onBankClick: (Institution) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Bank") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEBF1F8))
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                banks.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "No banks available",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Text(
                            "Please try again later",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(banks) { bank ->
                            BankItem(
                                bank = bank,
                                onClick = { onBankClick(bank) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BankItem(
    bank: Institution,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF9FCFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bank Logo
            Image(
                painter = rememberAsyncImagePainter(bank.logo),
                contentDescription = bank.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )

            // Bank Name
            Text(
                text = bank.name ?: "Unknown Bank",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            // Arrow Icon
            Icon(
                painter = rememberAsyncImagePainter(android.R.drawable.ic_menu_more),
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectBankScreenPreview() {
    val mockBanks = listOf(
        Institution().apply {
            id = "1"
            name = "Sample Bank"
            logo = null
        },
        Institution().apply {
            id = "2"
            name = "Another Bank"
            logo = null
        }
    )

    SelectBankScreen(
        banks = mockBanks,
        isLoading = false,
        onBackClick = {},
        onBankClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SelectBankScreenLoadingPreview() {
    SelectBankScreen(
        banks = emptyList(),
        isLoading = true,
        onBackClick = {},
        onBankClick = {}
    )
}

