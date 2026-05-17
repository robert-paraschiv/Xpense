package com.rokudo.xpense.fragments

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rokudo.xpense.R
import com.rokudo.xpense.components.EmptyState
import com.rokudo.xpense.components.LoadingState
import com.rokudo.xpense.components.XpenseCard
import com.rokudo.xpense.components.XpenseTopAppBar
import com.rokudo.xpense.models.User
import com.rokudo.xpense.ui.theme.XpenseTheme

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            XpenseTopAppBar(
                title = "Invite Contacts",
                onBackClick = onBackClick,
                actions = {
                    TextButton(onClick = onRefreshClick) {
                        Text("Refresh", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Info Card
            XpenseCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Note: Contacts must have an Xpense account to appear in this list",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LoadingState(
                    isLoading = isLoading,
                    modifier = Modifier.fillMaxSize()
                )

                EmptyState(
                    visible = !isLoading && contacts.isEmpty(),
                    title = "No contacts found",
                    subtitle = "Grant contacts permission and refresh",
                    iconResId = R.drawable.ic_baseline_person_24
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = !isLoading && contacts.isNotEmpty(),
                    enter = fadeIn(tween(400)),
                    exit = fadeOut(tween(300))
                ) {
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
    XpenseCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
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
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user.phoneNumber ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

    XpenseTheme(dynamicColor = false) {
        ContactsScreen(
            contacts = mockContacts,
            isLoading = false,
            onBackClick = {},
            onRefreshClick = {},
            onContactClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ContactsScreenEmptyPreview() {
    XpenseTheme(dynamicColor = false) {
        ContactsScreen(
            contacts = emptyList(),
            isLoading = false,
            onBackClick = {},
            onRefreshClick = {},
            onContactClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ContactsScreenLoadingPreview() {
    XpenseTheme(dynamicColor = false) {
        ContactsScreen(
            contacts = emptyList(),
            isLoading = true,
            onBackClick = {},
            onRefreshClick = {},
            onContactClick = {}
        )
    }
}
