package com.rokudo.xpense.fragments

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.rokudo.xpense.R
import com.rokudo.xpense.components.EmptyState
import com.rokudo.xpense.components.LoadingState
import com.rokudo.xpense.components.XpenseCard
import com.rokudo.xpense.components.XpenseTopAppBar
import com.rokudo.xpense.data.retrofit.models.Institution
import com.rokudo.xpense.ui.theme.XpenseTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectBankScreen(
    banks: List<Institution>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onBankClick: (Institution) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            XpenseTopAppBar(
                title = "Select Bank",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            LoadingState(
                isLoading = isLoading,
                modifier = Modifier.fillMaxSize()
            )

            EmptyState(
                visible = !isLoading && banks.isEmpty(),
                title = "No banks available",
                subtitle = "Please try again later"
            )

            AnimatedVisibility(
                visible = !isLoading && banks.isNotEmpty(),
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(300))
            ) {
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

@Composable
fun BankItem(
    bank: Institution,
    onClick: () -> Unit
) {
    XpenseCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(bank.logo),
                contentDescription = bank.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )

            Text(
                text = bank.name ?: "Unknown Bank",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_round_keyboard_arrow_down_24),
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

    XpenseTheme(dynamicColor = false) {
        SelectBankScreen(
            banks = mockBanks,
            isLoading = false,
            onBackClick = {},
            onBankClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SelectBankScreenLoadingPreview() {
    XpenseTheme(dynamicColor = false) {
        SelectBankScreen(
            banks = emptyList(),
            isLoading = true,
            onBackClick = {},
            onBankClick = {}
        )
    }
}
