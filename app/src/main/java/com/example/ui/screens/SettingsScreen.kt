package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import com.example.data.model.PrivacySecuritySettings
import com.example.data.model.UserAccount
import com.example.ui.components.ActionFeedbackBanner
import com.example.ui.components.AvatarOptionsCatalog
import com.example.ui.components.CitizenAvatarBadge
import com.example.ui.components.HoaAdaptivePageContainer
import com.example.ui.components.HoaModuleCard
import com.example.ui.components.HoaSectionHeader
import com.example.ui.components.HoaStatusBadge
import com.example.ui.state.ActionFeedback
import com.example.ui.state.HoaShellUiState
import com.example.ui.state.PublicVisibilitySettings
import com.example.ui.state.SimulationActions
import com.example.ui.state.SimulationSnapshot
import com.example.ui.theme.HoaMonoStyle
import com.example.ui.theme.ThemePreference

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: HoaShellUiState,
    onSelectTheme: (ThemePreference) -> Unit,
    onToggleNationality: (Boolean) -> Unit,
    onToggleResidence: (Boolean) -> Unit,
    onToggleOccupation: (Boolean) -> Unit,
    onToggleActivity: (Boolean) -> Unit,
    onUpdateVisibilitySettings: ((PublicVisibilitySettings) -> PublicVisibilitySettings) -> Unit = {},
    sim: SimulationSnapshot = SimulationSnapshot(),
    feedback: ActionFeedback = ActionFeedback(),
    actions: SimulationActions? = null,
    userAccount: UserAccount? = null,
    userDisplayName: String? = null,
    userEmail: String? = null,
    onOpenCharacterWizard: ((asAlt: Boolean) -> Unit)? = null,
    onOpenPlayerProfile: ((Long) -> Unit)? = null,
    onSignOut: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val activeChar = sim.activeCharacter
    val v = uiState.visibilitySettings

    var editOccupation by remember(activeChar?.playerId) { mutableStateOf(activeChar?.occupation ?: "Citizen") }
    var editBio by remember(activeChar?.playerId) { mutableStateOf(activeChar?.biography.orEmpty()) }
    var avatarStyle by remember(activeChar?.playerId) { mutableStateOf(activeChar?.avatarStyle ?: "ROYAL_KENTE") }
    var accentIdx by remember(activeChar?.playerId) { mutableIntStateOf(activeChar?.avatarAccentIndex ?: 0) }
    var skinToneIdx by remember(activeChar?.playerId) { mutableIntStateOf(activeChar?.avatarSkinToneIdx ?: 2) }
    var hairStyle by remember(activeChar?.playerId) { mutableStateOf(activeChar?.avatarHairStyle ?: "SHORT_FADE") }
    var hairColorIdx by remember(activeChar?.playerId) { mutableIntStateOf(activeChar?.avatarHairColorIdx ?: 0) }
    var faceStyle by remember(activeChar?.playerId) { mutableStateOf(activeChar?.avatarFaceStyle ?: "CONFIDENT") }
    var clothingStyle by remember(activeChar?.playerId) { mutableStateOf(activeChar?.avatarClothingStyle ?: "KENTE_ROBE") }
    var accessory by remember(activeChar?.playerId) { mutableStateOf(activeChar?.avatarAccessory ?: "NONE") }

    val buildPrivacyPayload = {
        PrivacySecuritySettings(
            twoFactorEnabled = sim.privacySecurity?.twoFactorEnabled ?: true,
            emailVerified = userAccount?.emailVerified ?: (sim.privacySecurity?.emailVerified ?: true),
            showNationality = v.showNationality,
            showResidence = v.showCurrentResidence,
            showOccupation = v.showOccupation,
            showPublicActivity = v.showPublicActivity,
            showNetWorth = v.showNetWorth,
            showPersonalBalance = v.showPersonalBalance,
            showCapitalBalance = v.showCapitalBalance,
            showEmployment = v.showEmployment,
            showCompanies = v.showCompanies,
            showPolitics = v.showPolitics,
            showPersonalIdentity = v.showPersonalIdentity,
            notifySalaries = v.notifySalaries,
            notifyTaxes = v.notifyTaxes,
            notifyElections = v.notifyElections,
            notifyCompanies = v.notifyCompanies,
            notifyMarketplace = v.notifyMarketplace,
            allowFriendRequests = v.allowFriendRequests,
            allowDirectMessages = v.allowDirectMessages,
            publicProfileSearchable = v.publicProfileSearchable,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen_root"),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Player Settings & Security",
                subtitle = "Account, character identity, avatar studio, privacy, security, alerts, social & ALT slots",
                badgeLabel = "8 SECTIONS",
            )

            ActionFeedbackBanner(feedback = feedback)

            // 1. Account & Login
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_section_account"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Account & Login", style = MaterialTheme.typography.titleMedium)
                    Text("Username: @${userAccount?.username?.ifBlank { "unclaimed" } ?: "citizen"}", style = HoaMonoStyle, color = MaterialTheme.colorScheme.primary)
                    Text("Display Name: ${userDisplayName ?: userAccount?.displayName ?: "Authenticated Citizen"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Email: ${userEmail?.ifBlank { userAccount?.email } ?: userAccount?.email ?: "Verified via Google"}", style = MaterialTheme.typography.bodySmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HoaStatusBadge(label = if (userAccount?.emailVerified != false) "EMAIL VERIFIED" else "PENDING", highlighted = true)
                        if (activeChar != null && onOpenPlayerProfile != null) {
                            OutlinedButton(onClick = { onOpenPlayerProfile(activeChar.playerId) }, modifier = Modifier.testTag("settings_open_public_profile")) {
                                Text("View ${activeChar.canonicalUrl()}")
                            }
                        }
                        if (onSignOut != null) {
                            OutlinedButton(onClick = onSignOut, modifier = Modifier.testTag("sign_out_button")) { Text("Logout") }
                        }
                    }
                }
            }

            // 2. Character Identity & Display Theme
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_section_identity"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("2. Character Identity & Display Theme", style = MaterialTheme.typography.titleMedium)
                    }
                    if (activeChar != null) {
                        Text("${activeChar.fullName} • ${activeChar.canonicalUrl()} • DOB: ${activeChar.dateOfBirth}", style = HoaMonoStyle)
                        OutlinedTextField(value = editOccupation, onValueChange = { editOccupation = it }, label = { Text("Occupation") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editBio, onValueChange = { editBio = it }, label = { Text("Biography") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

            // 3. Avatar & Appearance Studio
            if (activeChar != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("settings_section_avatar"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("3. Avatar & Appearance Studio", style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CitizenAvatarBadge(
                                avatarStyle = avatarStyle,
                                accentIndex = accentIdx,
                                size = 72.dp,
                                skinToneIdx = skinToneIdx,
                                hairStyle = hairStyle,
                                hairColorIdx = hairColorIdx,
                                faceStyle = faceStyle,
                                clothingStyle = clothingStyle,
                                accessory = accessory,
                            )
                            Column {
                                Text(AvatarOptionsCatalog.clothingLabels[clothingStyle] ?: clothingStyle, style = MaterialTheme.typography.titleSmall)
                                Text("${AvatarOptionsCatalog.hairStyleLabels[hairStyle]} • ${AvatarOptionsCatalog.skinToneNames[skinToneIdx]}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AvatarOptionsCatalog.skinToneNames.forEachIndexed { idx, name ->
                                FilterChip(selected = skinToneIdx == idx, onClick = { skinToneIdx = idx }, label = { Text(name) })
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AvatarOptionsCatalog.hairStyles.forEach { st ->
                                FilterChip(selected = hairStyle == st, onClick = { hairStyle = st }, label = { Text(AvatarOptionsCatalog.hairStyleLabels[st] ?: st) })
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AvatarOptionsCatalog.clothingStyles.forEach { cl ->
                                FilterChip(selected = clothingStyle == cl, onClick = { clothingStyle = cl }, label = { Text(AvatarOptionsCatalog.clothingLabels[cl] ?: cl) })
                            }
                            AvatarOptionsCatalog.accessories.forEach { acc ->
                                FilterChip(selected = accessory == acc, onClick = { accessory = acc }, label = { Text(AvatarOptionsCatalog.accessoryLabels[acc] ?: acc) })
                            }
                        }
                        Button(
                            onClick = {
                                actions?.updateFullAvatarAndProfile(
                                    playerId = activeChar.playerId,
                                    avatarStyle = avatarStyle,
                                    accentIdx = accentIdx,
                                    skinToneIdx = skinToneIdx,
                                    hairStyle = hairStyle,
                                    hairColorIdx = hairColorIdx,
                                    faceStyle = faceStyle,
                                    clothingStyle = clothingStyle,
                                    accessory = accessory,
                                    occupation = editOccupation.ifBlank { "Citizen" },
                                    bio = editBio,
                                    interests = activeChar.personalInterests,
                                    publicVisible = activeChar.publicVisible,
                                )
                            },
                            enabled = !feedback.isBusy,
                            modifier = Modifier.testTag("settings_save_avatar_button"),
                        ) {
                            Text("Save Character Identity & Avatar")
                        }
                    }
                }
            }

            // 4. Granular Privacy Controls (8 Requested Toggles + Legacy Matrix)
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_section_privacy"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("4. Public Profile Privacy Controls", style = MaterialTheme.typography.titleMedium)
                    SettingToggleRow("Show Nationality", v.showNationality, "toggle_visibility_nationality", onToggleNationality)
                    SettingToggleRow("Show Current Residence", v.showCurrentResidence, "toggle_visibility_residence", onToggleResidence)
                    SettingToggleRow("Show Occupation", v.showOccupation, "toggle_visibility_occupation", onToggleOccupation)
                    SettingToggleRow("Show Public Activity Feed", v.showPublicActivity, "toggle_visibility_activity", onToggleActivity)
                    SettingToggleRow("Show Net Worth", v.showNetWorth, "toggle_visibility_networth") { en -> onUpdateVisibilitySettings { it.copy(showNetWorth = en) } }
                    SettingToggleRow("Show Personal Balance", v.showPersonalBalance, "toggle_visibility_personal_bal") { en -> onUpdateVisibilitySettings { it.copy(showPersonalBalance = en) } }
                    SettingToggleRow("Show Capital Balance", v.showCapitalBalance, "toggle_visibility_capital_bal") { en -> onUpdateVisibilitySettings { it.copy(showCapitalBalance = en) } }
                    SettingToggleRow("Show Employment", v.showEmployment, "toggle_visibility_employment") { en -> onUpdateVisibilitySettings { it.copy(showEmployment = en) } }
                    SettingToggleRow("Show Companies", v.showCompanies, "toggle_visibility_companies") { en -> onUpdateVisibilitySettings { it.copy(showCompanies = en) } }
                    SettingToggleRow("Show Politics", v.showPolitics, "toggle_visibility_politics") { en -> onUpdateVisibilitySettings { it.copy(showPolitics = en) } }
                    SettingToggleRow("Show Personal Identity (DOB / Birthplace)", v.showPersonalIdentity, "toggle_visibility_identity") { en -> onUpdateVisibilitySettings { it.copy(showPersonalIdentity = en) } }
                }
            }

            // 5. Security
            HoaModuleCard(
                modifier = Modifier.testTag("settings_section_security"),
                title = "5. Security & Verification",
                icon = Icons.Outlined.Security,
                phaseLabel = "2FA ACTIVE",
                description = "Google Credential Manager login with 2FA protection, session isolation, rate-limiting & ${sim.auditLogs.size} immutable audit logs.",
                emptyStateTitle = "Password Auth Disclosure",
                emptyStateDetail = "Password changes require Firebase Email/Password authentication; this app enforces verified Google Sign-In.",
                specTags = listOf("2FA Active", "Email Verified", "NAT-Safe Guard"),
            )

            // 6. Notifications & 7. Social Settings
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_section_notifications_social"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("6. Notification Preferences", style = MaterialTheme.typography.titleMedium)
                    SettingToggleRow("Salary & Payroll Alerts", v.notifySalaries, "toggle_notify_salaries") { en -> onUpdateVisibilitySettings { it.copy(notifySalaries = en) } }
                    SettingToggleRow("Tax Deduction Notices", v.notifyTaxes, "toggle_notify_taxes") { en -> onUpdateVisibilitySettings { it.copy(notifyTaxes = en) } }
                    SettingToggleRow("Election & Campaign Updates", v.notifyElections, "toggle_notify_elections") { en -> onUpdateVisibilitySettings { it.copy(notifyElections = en) } }
                    SettingToggleRow("Company & Dividend Alerts", v.notifyCompanies, "toggle_notify_companies") { en -> onUpdateVisibilitySettings { it.copy(notifyCompanies = en) } }
                    SettingToggleRow("Marketplace Order Fills", v.notifyMarketplace, "toggle_notify_market") { en -> onUpdateVisibilitySettings { it.copy(notifyMarketplace = en) } }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("7. Social & Directory Settings", style = MaterialTheme.typography.titleMedium)
                    SettingToggleRow("Allow Citizen Friend Requests", v.allowFriendRequests, "toggle_social_friends") { en -> onUpdateVisibilitySettings { it.copy(allowFriendRequests = en) } }
                    SettingToggleRow("Allow Direct Messages", v.allowDirectMessages, "toggle_social_dm") { en -> onUpdateVisibilitySettings { it.copy(allowDirectMessages = en) } }
                    SettingToggleRow("Searchable in Public Citizen Directory", v.publicProfileSearchable, "toggle_social_searchable") { en -> onUpdateVisibilitySettings { it.copy(publicProfileSearchable = en) } }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { actions?.saveSecuritySettings(buildPrivacyPayload()) },
                        enabled = !feedback.isBusy,
                        modifier = Modifier.fillMaxWidth().testTag("settings_save_cloud_button"),
                    ) {
                        Text("Save Privacy, Notification & Social Settings")
                    }
                }
            }

            // 8. ALT Character Management
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_section_alt"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("8. Main & ALT Character Management", style = MaterialTheme.typography.titleMedium)
                    Text("Manage your Main character and linked ALT characters under one verified account.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        sim.myCharacters.forEach { ch ->
                            FilterChip(
                                selected = ch.playerId == activeChar?.playerId,
                                onClick = { actions?.switchCharacter(ch.playerId) },
                                label = { Text("#${ch.playerId} ${ch.fullName} (${ch.characterRole})") },
                            )
                        }
                    }
                    if (onOpenCharacterWizard != null) {
                        OutlinedButton(
                            onClick = { onOpenCharacterWizard(sim.myCharacters.any { it.characterRole == "MAIN" && it.accountStatus != "DELETED" }) },
                            modifier = Modifier.testTag("settings_create_alt_wizard_button"),
                        ) {
                            Text(if (sim.myCharacters.any { it.characterRole == "MAIN" && it.accountStatus != "DELETED" }) "Create ALT Character (6-Step Wizard)" else "Create MAIN Character")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SettingToggleRow(
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
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.testTag(testTag))
    }
}
