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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.PlayerCharacter
import com.example.domain.FinancialEngines
import com.example.ui.components.ActionFeedbackBanner
import com.example.ui.components.CitizenAvatarBadge
import com.example.ui.components.HoaAdaptivePageContainer
import com.example.ui.components.HoaEmptyStateBanner
import com.example.ui.components.HoaModuleCard
import com.example.ui.components.HoaSectionHeader
import com.example.ui.components.HoaStatusBadge
import com.example.ui.state.ActionFeedback
import com.example.ui.state.HoaShellUiState
import com.example.ui.state.ProfileSectionTab
import com.example.ui.state.SimulationActions
import com.example.ui.state.SimulationSnapshot
import com.example.ui.theme.HoaMonoStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    uiState: HoaShellUiState,
    onSelectSection: (ProfileSectionTab) -> Unit,
    sim: SimulationSnapshot = SimulationSnapshot(),
    feedback: ActionFeedback = ActionFeedback(),
    playerLookupResult: Pair<String, PlayerCharacter?>? = null,
    actions: SimulationActions? = null,
    activeCountryId: Long = 101L,
    activeStateId: Long = 201L,
    activeCountyId: Long = 301L,
    onOpenPublicProfile: ((Long) -> Unit)? = null,
    onOpenCharacterWizard: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var fullName by remember { mutableStateOf("Amina Okonkwo") }
    var occupation by remember { mutableStateOf("Trade Economist") }
    var bio by remember { mutableStateOf("Building regional supply chains across Africa.") }
    var avatarStyle by remember { mutableStateOf("ROYAL_KENTE") }
    var accentIdx by remember { mutableIntStateOf(0) }
    var createAsAlt by remember { mutableStateOf(false) }
    var publicPreviewMode by remember { mutableStateOf(false) }
    var lookupIdText by remember { mutableStateOf("12453") }

    val activeChar = sim.activeCharacter

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Citizen Profile & Immigration",
                subtitle = "Main + ALT characters • Zero XP/Levels • 16-section citizen dossier",
                badgeLabel = activeChar?.let { "#${it.playerId} ${it.characterRole}" } ?: "UPDATE 3 LIVE",
            )

            ActionFeedbackBanner(feedback = feedback)

            // Active Citizen Banner + Public/Private Toggle
            if (activeChar != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            CitizenAvatarBadge(character = activeChar)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(activeChar.resolvePublicDisplayName(), style = MaterialTheme.typography.titleLarge)
                                Text(
                                    text = "${activeChar.canonicalUrl()} • ${activeChar.characterRole} • ${activeChar.accountStatus}",
                                    style = HoaMonoStyle,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = if (publicPreviewMode && !activeChar.showResidence) "Residence Hidden (Privacy)" else "/world/${activeChar.currentCountryId} • /state/${activeChar.currentStateId} • /county/${activeChar.currentCountyId}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            FilterChip(
                                selected = publicPreviewMode,
                                onClick = { publicPreviewMode = !publicPreviewMode },
                                label = { Text(if (publicPreviewMode) "Public View" else "Private View") },
                            )
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (onOpenPublicProfile != null) {
                                OutlinedButton(
                                    onClick = { onOpenPublicProfile(activeChar.playerId) },
                                    modifier = Modifier.testTag("profile_open_public_route_button"),
                                ) {
                                    Text("Open ${activeChar.canonicalUrl()}")
                                }
                            }
                            if (onOpenCharacterWizard != null) {
                                OutlinedButton(
                                    onClick = { onOpenCharacterWizard(true) },
                                    modifier = Modifier.testTag("profile_open_wizard_alt_button"),
                                ) {
                                    Text("Launch 6-Step ALT Wizard")
                                }
                            }
                        }
                        if (sim.myCharacters.size > 1) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                sim.myCharacters.forEach { ch ->
                                    FilterChip(
                                        selected = ch.playerId == activeChar.playerId,
                                        onClick = { actions?.switchCharacter(ch.playerId) },
                                        label = { Text("#${ch.playerId} ${ch.fullName} (${ch.characterRole})") },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 16 Section Tabs
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

            // Section-specific Live View
            when (uiState.selectedProfileSection) {
                ProfileSectionTab.IDENTITY, ProfileSectionTab.OVERVIEW -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CitizenAvatarBadge(avatarStyle = avatarStyle, accentIndex = accentIdx, size = 54.dp)
                                Column {
                                    Text("Citizen Registration & Avatar Studio", style = MaterialTheme.typography.titleMedium)
                                    Text("1 Main Character + optional ALT characters (zero XP/levels)", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("ROYAL_KENTE", "EMERALD_CROWN", "SAVANNA_SUN", "CONTINENT_STAR").forEach { st ->
                                    FilterChip(selected = avatarStyle == st, onClick = { avatarStyle = st }, label = { Text(st) })
                                }
                                listOf(0, 1, 2, 3).forEach { idx ->
                                    FilterChip(selected = accentIdx == idx, onClick = { accentIdx = idx }, label = { Text("Accent ${idx + 1}") })
                                }
                            }
                            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = occupation, onValueChange = { occupation = it }, label = { Text("Occupation") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Biography") }, modifier = Modifier.fillMaxWidth())
                            if (actions != null) {
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(selected = createAsAlt, onClick = { createAsAlt = !createAsAlt }, label = { Text(if (createAsAlt) "Role: ALT Character" else "Role: MAIN Character") })
                                    Button(
                                        onClick = {
                                            actions.createCharacter(
                                                fullName = fullName,
                                                gender = "Citizen",
                                                countryId = activeCountryId.coerceAtLeast(101L),
                                                stateId = activeStateId.coerceAtLeast(201L),
                                                countyId = activeCountyId.coerceAtLeast(301L),
                                                occupation = occupation,
                                                biography = bio,
                                                avatarStyle = avatarStyle,
                                                avatarAccentIndex = accentIdx,
                                                asAlt = createAsAlt,
                                            )
                                        },
                                        enabled = !feedback.isBusy,
                                        modifier = Modifier.testTag("create_citizen_button"),
                                    ) {
                                        Text("Register Citizen (#12453+)")
                                    }
                                    if (activeChar != null) {
                                        OutlinedButton(onClick = { actions.softDeleteCharacter(activeChar.playerId) }) {
                                            Text("Soft-Delete ('User Deleted')")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Directory / Deleted / Suspended / Not Found Lookup
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Permanent Player ID Lookup (/player/{id})", style = MaterialTheme.typography.titleSmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(value = lookupIdText, onValueChange = { lookupIdText = it.filter(Char::isDigit) }, label = { Text("Player ID") }, singleLine = true, modifier = Modifier.weight(1f))
                                OutlinedButton(onClick = { actions?.lookupPlayerById(lookupIdText.toLongOrNull() ?: 0L) }) {
                                    Text("Lookup")
                                }
                                if (onOpenPublicProfile != null) {
                                    OutlinedButton(onClick = { onOpenPublicProfile(lookupIdText.toLongOrNull() ?: 0L) }) {
                                        Text("Open Page")
                                    }
                                }
                            }
                            if (playerLookupResult != null) {
                                Text(
                                    text = "Resolved Label: ${playerLookupResult.first} (Status: ${playerLookupResult.second?.accountStatus ?: "UNKNOWN"})",
                                    style = HoaMonoStyle,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }

                ProfileSectionTab.IMMIGRATION -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("International Immigration (10-Yr / 20 Real-Day Permits)", style = MaterialTheme.typography.titleMedium)
                            Text("Cross-border relocation requires Exit Clearance and an Entry/Residence Permit.", style = MaterialTheme.typography.bodySmall)
                            if (actions != null && activeChar != null) {
                                val nextCountry = if (activeChar.currentCountryId == 101L) Triple(102L, 203L, 304L) else Triple(101L, 201L, 301L)
                                Button(
                                    onClick = { actions.applyCrossBorderImmigration(nextCountry.first, nextCountry.second, nextCountry.third) },
                                    enabled = !feedback.isBusy,
                                    modifier = Modifier.testTag("immigration_apply_button"),
                                ) {
                                    Text("Apply Exit Clearance & Entry Permit -> /world/${nextCountry.first}")
                                }
                            }
                            sim.immigrationRecords.forEach { rec ->
                                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${rec.entryPermitCode} (${rec.permitStatus})", style = MaterialTheme.typography.titleSmall)
                                            Text("/world/${rec.fromCountryId} -> /world/${rec.toCountryId} • Expires Day ${rec.expiresAtGameDay.toLong()}", style = HoaMonoStyle)
                                        }
                                        if (actions != null) {
                                            OutlinedButton(onClick = { actions.renewImmigrationPermit(rec) }) { Text("Renew 10 Yrs") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    val sectionSummary = when (uiState.selectedProfileSection) {
                        ProfileSectionTab.BANK_ACCOUNTS -> "Personal: ${FinancialEngines.formatCurrency(sim.personalAccount?.balanceCents ?: 0L)} • Capital: ${FinancialEngines.formatCurrency(sim.capitalAccount?.balanceCents ?: 0L)}"
                        ProfileSectionTab.INCOME -> "${sim.ledgerTransactions.size} ledger entries • Employment taxes routed automatically"
                        ProfileSectionTab.EMPLOYMENT -> "${sim.employmentContracts.size} active employment contracts"
                        ProfileSectionTab.COMPANIES -> "${sim.companies.size} registered companies"
                        ProfileSectionTab.INVESTMENTS -> "${sim.bonds.size} treasury/corporate bonds"
                        ProfileSectionTab.PROPERTIES -> "${sim.properties.size} property deeds owned"
                        ProfileSectionTab.CERTIFICATES -> "${sim.companies.size} incorporation certificates • ${sim.immigrationRecords.size} permits"
                        ProfileSectionTab.POLITICS -> "${sim.politicalParties.size} registered parties"
                        ProfileSectionTab.CAMPAIGNS -> "${sim.campaigns.size} active campaigns across State/National/General"
                        ProfileSectionTab.ELECTIONS -> "${sim.elections.size} elections tracked"
                        ProfileSectionTab.ACHIEVEMENTS -> "Zero XP / Zero Levels — Reputation measured through civic offices, enterprises & history."
                        ProfileSectionTab.HISTORY -> "${sim.auditLogs.size} immutable audit actions logged"
                        else -> "Manage appearance, privacy & 2FA in Settings."
                    }
                    HoaModuleCard(
                        title = "${uiState.selectedProfileSection.index}. ${uiState.selectedProfileSection.label}",
                        icon = Icons.Outlined.Badge,
                        phaseLabel = "Live",
                        description = sectionSummary,
                        emptyStateTitle = activeChar?.resolvePublicDisplayName() ?: "No active citizen registered yet",
                        emptyStateDetail = "Zero XP/Level architecture • Every record backed by Firestore.",
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationsScreen(
    uiState: HoaShellUiState,
    onSelectFilter: (String) -> Unit,
    sim: SimulationSnapshot = SimulationSnapshot(),
    modifier: Modifier = Modifier,
) {
    val categories = listOf(
        "All", "Salaries", "Taxes", "Companies", "Shares", "Dividends",
        "Campaigns", "Elections", "Immigration", "Loans", "Marketplace", "Government", "System",
    )
    val filtered = sim.notifications.filter {
        uiState.selectedNotificationFilter == "All" || it.category.equals(uiState.selectedNotificationFilter, ignoreCase = true)
    }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Notifications & Audit History",
                subtitle = "Real-time event feed for financial, civic, and world actions",
                badgeLabel = "${sim.notifications.size} EVENTS",
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { category ->
                    FilterChip(
                        selected = uiState.selectedNotificationFilter == category,
                        onClick = { onSelectFilter(category) },
                        modifier = Modifier.testTag("notif_filter_${category.lowercase()}"),
                        label = { Text(category) },
                    )
                }
            }

            if (filtered.isEmpty()) {
                HoaEmptyStateBanner(
                    icon = Icons.Outlined.NotificationsNone,
                    title = "No Notifications (${uiState.selectedNotificationFilter})",
                    message = "Salary payouts, immigration renewals, company registrations, and referendum votes stream here.",
                    phaseNote = "LIVE STREAM",
                )
            } else {
                filtered.forEach { n ->
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(n.title, style = MaterialTheme.typography.titleSmall)
                                HoaStatusBadge(label = n.category, highlighted = !n.readStatus)
                            }
                            Text(n.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
