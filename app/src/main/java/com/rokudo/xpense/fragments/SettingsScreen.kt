package com.rokudo.xpense.fragments

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import coil.compose.AsyncImage
import com.rokudo.xpense.R
import com.rokudo.xpense.components.XpenseCard
import com.rokudo.xpense.components.XpenseTopAppBar
import com.rokudo.xpense.models.Invitation
import com.rokudo.xpense.ui.theme.XpenseTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userName: String,
    userProfilePicUrl: String?,
    invitations: List<Invitation>,
    onBackClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onProfilePictureClick: () -> Unit,
    onAcceptInvitation: (Invitation) -> Unit,
    onDeclineInvitation: (Invitation) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            XpenseTopAppBar(
                title = "Settings",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Profile Section
            XpenseCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = userProfilePicUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable { onProfilePictureClick() },
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.ic_baseline_person_24),
                        error = painterResource(id = R.drawable.ic_baseline_person_24)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Invitations Section
            AnimatedVisibility(
                visible = invitations.isNotEmpty(),
                enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
            ) {
                Column {
                    Text(
                        text = "Pending Invitations",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(invitations) { invitation ->
                            InvitationCard(
                                invitation = invitation,
                                onAccept = { onAcceptInvitation(invitation) },
                                onDecline = { onDeclineInvitation(invitation) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (invitations.isEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Sign Out Button
            Button(
                onClick = onSignOutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InvitationCard(
    invitation: Invitation,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    XpenseCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Invitation from ${invitation.creator_name ?: "Unknown"}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Wallet: ${invitation.wallet_title ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Decline")
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Accept")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val mockInvitations = listOf(
        Invitation().apply {
            id = "1"
            creator_name = "John Doe"
            wallet_title = "Family Wallet"
            status = Invitation.STATUS_SENT
        },
        Invitation().apply {
            id = "2"
            creator_name = "Jane Smith"
            wallet_title = "Shared Expenses"
            status = Invitation.STATUS_SENT
        }
    )

    XpenseTheme(dynamicColor = false) {
        SettingsScreen(
            userName = "Alex Johnson",
            userProfilePicUrl = null,
            invitations = mockInvitations,
            onBackClick = {},
            onSignOutClick = {},
            onProfilePictureClick = {},
            onAcceptInvitation = {},
            onDeclineInvitation = {}
        )
    }
}
