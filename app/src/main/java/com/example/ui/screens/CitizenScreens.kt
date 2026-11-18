package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.HoaAdaptivePageContainer
import com.example.ui.components.HoaEmptyStateBanner
import com.example.ui.components.HoaModuleCard
import com.example.ui.components.HoaSectionHeader
import com.example.ui.navigation.HoaEntityRoutes
import com.example.ui.state.HoaShellUiState
import com.example.ui.state.ProfileSectionTab
import com.example.ui.theme.HoaMonoStyle
import com.example.ui.theme.ThemePreference

@Composable
fun ProfileScreen(
    uiState: HoaShellUiState,
    onSelectSection: (ProfileSectionTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Citizen Profile",
                subtitle = "16-section citizen dossier, ALT character policy, and zero-level design",
                badgeLabel = "PHASE 4",
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProfileSectionTab.entries.forEach { tab ->
                    FilterChip(
                        selected = uiState.selectedProfileSection == tab,
                        onClick = { onSelectSection(tab) },
                        modifier = Modifier.testTag("profile_tab_${tab.name.lowercase()}"),
                        label = { Text("${tab.index}. ${tab.label}") },
                    )
                }
            }

            HoaModuleCard(
                title = "${uiState.selectedProfileSection.index}. ${uiState.selectedProfileSection.label}",
                icon = Icons.Outlined.Badge,
                phaseLabel = "Phase 4",
                description = "Route pattern: ${HoaEntityRoutes.player(12453)}. One account holds one main character and optional ALT characters (never separate login accounts).",
                emptyStateTitle = "No citizen profile active yet",
                emptyStateDetail = "Deleted accounts render 'User Deleted' in historical ledgers; suspended accounts remain distinct; unknown IDs show 'Player Not Found'.",
                specTags = listOf("No XP/Levels", "Privacy Controls", "ALT Policy"),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationsScreen(
    uiState: HoaShellUiState,
    onSelectFilter: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = listOf(
        "All", "Salaries", "Taxes", "Companies", "Shares", "Dividends",
        "Campaigns", "Elections", "Immigration", "Loans", "Marketplace", "Government",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Notifications & Audit History",
                subtitle = "Real-time event feed for financial, civic, and world actions",
                badgeLabel = "0 UNREAD",
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = uiState.selectedNotificationFilter == category,
                        onClick = { onSelectFilter(category) },
                        modifier = Modifier.testTag("notif_filter_${category.lowercase()}"),
                        label = { Text(category) },
                    )
                }
            }

            HoaEmptyStateBanner(
                icon = Icons.Outlined.NotificationsNone,
                title = "No Notifications (${uiState.selectedNotificationFilter})",
                message = "Salary payouts, tax receipts, share trades, immigration renewals, and election alerts will stream here once game engines launch.",
                phaseNote = "REAL EMPTY STATE • PHASE 1",
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: HoaShellUiState,
    onSelectTheme: (ThemePreference) -> Unit,
    onToggleNationality: (Boolean) -> Unit,
    onToggleResidence: (Boolean) -> Unit,
    onToggleOccupation: (Boolean) -> Unit,
    onToggleActivity: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Settings & Security",
                subtitle = "Appearance, public profile privacy defaults, and anti-exploit readiness",
                badgeLabel = "ACTIVE",
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Appearance (Dark Green, Gold & Off-White)",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ThemePreference.entries.forEach { pref ->
                            FilterChip(
                                selected = uiState.themePreference == pref,
                                onClick = { onSelectTheme(pref) },
                                modifier = Modifier.testTag("theme_chip_${pref.name.lowercase()}"),
                                label = {
                                    Text(
                                        when (pref) {
                                            ThemePreference.SYSTEM -> "System Default"
                                            ThemePreference.LIGHT -> "Parchment Light"
                                            ThemePreference.DARK -> "Obsidian Dark"
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Public Profile Visibility Controls",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Sensitive account fields are never stored in public profiles. Configure default public visibility:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PrivacyToggleRow(
                        label = "Show Nationality",
                        checked = uiState.visibilitySettings.showNationality,
                        testTag = "toggle_visibility_nationality",
                        onCheckedChange = onToggleNationality,
                    )
                    PrivacyToggleRow(
                        label = "Show Current Country / State / County",
                        checked = uiState.visibilitySettings.showCurrentResidence,
                        testTag = "toggle_visibility_residence",
                        onCheckedChange = onToggleResidence,
                    )
                    PrivacyToggleRow(
                        label = "Show Occupation",
                        checked = uiState.visibilitySettings.showOccupation,
                        testTag = "toggle_visibility_occupation",
                        onCheckedChange = onToggleOccupation,
                    )
                    PrivacyToggleRow(
                        label = "Show Public Activity Feed",
                        checked = uiState.visibilitySettings.showPublicActivity,
                        testTag = "toggle_visibility_activity",
                        onCheckedChange = onToggleActivity,
                    )
                }
            }

            HoaModuleCard(
                title = "Authentication & Anti-Exploit Architecture",
                icon = Icons.Outlined.Security,
                phaseLabel = "Phase 12",
                description = "Password hashing, email verification, 2FA support, rate limiting, audit logs, and carrier-NAT-safe IP abuse signals (never blanket IP bans).",
                emptyStateTitle = "Auth & 2FA provisioning planned for Phase 4 / 12",
                emptyStateDetail = "Build: House of Africa • Phase 1 UI Shell",
                specTags = listOf("2FA Ready", "NAT-Safe Signals", "Zero Sensitive Leaks"),
            ) {
                Text(
                    text = "Architecture Version: Phase 1 Shell • Package: com.aistudio.houseofafrica.krmpxv",
                    style = HoaMonoStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PrivacyToggleRow(
    label: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
        )
    }
}
