package com.rokudo.xpense.fragments

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.rokudo.xpense.components.SettingsRow
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
            XpenseTopAppBar(title = "Settings", onBackClick = onBackClick)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // ─── Profile Card ───
            XpenseCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = userProfilePicUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .clickable { onProfilePictureClick() },
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.ic_baseline_person_24),
                        error = painterResource(id = R.drawable.ic_baseline_person_24)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Edit profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Wallets Section ───
            Text(
                text = "WALLETS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            XpenseCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRow(
                        icon = Icons.Filled.AccountBalanceWallet,
                        title = "Manage Wallets",
                        onClick = {}
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SettingsRow(
                        icon = Icons.Filled.People,
                        title = "Shared Wallets",
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Preferences Section ───
            Text(
                text = "PREFERENCES",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            XpenseCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRow(
                        icon = Icons.Filled.Notifications,
                        title = "Notifications",
                        onClick = {}
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SettingsRow(
                        icon = Icons.Filled.Info,
                        title = "About",
                        subtitle = "Version 1.0",
                        onClick = {}
                    )
                }
            }

            // ─── Invitations Section ───
            AnimatedVisibility(
                visible = invitations.isNotEmpty(),
                enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "INVITATIONS (${invitations.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        invitations.forEach { invitation ->
                            InvitationCard(
                                invitation = invitation,
                                onAccept = { onAcceptInvitation(invitation) },
                                onDecline = { onDeclineInvitation(invitation) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── Sign Out ───
            OutlinedButton(
                onClick = onSignOutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                )
            ) {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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
                text = invitation.creator_name ?: "Unknown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Invited you to ${invitation.wallet_title ?: "a wallet"}",
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
                ) { Text("Decline") }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                ) { Text("Accept") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val mockInvitations = listOf(
        Invitation().apply {
            id = "1"; creator_name = "John Doe"; wallet_title = "Family Wallet"
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
