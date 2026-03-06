package com.rokudo.xpense.fragments

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rokudo.xpense.R
import com.rokudo.xpense.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    contacts: List<User>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onContactClick: (User) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invite Contacts") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onRefreshClick) {
                        Text("Refresh")
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
        ) {
            // Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF9C4)
                )
            ) {
                Text(
                    text = "Note: Contacts must have an Xpense account to appear in this list",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                contacts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_person_24),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Text(
                                "No contacts found",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            Text(
                                "Grant contacts permission and refresh",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(contacts) { contact ->
                            ContactItem(
                                user = contact,
                                onClick = { onContactClick(contact) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactItem(
    user: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF9FCFF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = user.pictureUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_baseline_person_24),
                error = painterResource(id = R.drawable.ic_baseline_person_24)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name ?: "Unknown",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = user.phoneNumber ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_round_add_24),
                contentDescription = "Invite",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactsScreenPreview() {
    val mockContacts = listOf(
        User().apply {
            name = "John Doe"
            phoneNumber = "+40123456789"
            pictureUrl = null
        },
        User().apply {
            name = "Jane Smith"
            phoneNumber = "+40987654321"
            pictureUrl = null
        }
    )

    ContactsScreen(
        contacts = mockContacts,
        isLoading = false,
        onBackClick = {},
        onRefreshClick = {},
        onContactClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ContactsScreenEmptyPreview() {
    ContactsScreen(
        contacts = emptyList(),
        isLoading = false,
        onBackClick = {},
        onRefreshClick = {},
        onContactClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ContactsScreenLoadingPreview() {
    ContactsScreen(
        contacts = emptyList(),
        isLoading = true,
        onBackClick = {},
        onRefreshClick = {},
        onContactClick = {}
    )
}

