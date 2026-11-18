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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PlayerCharacter
import com.example.data.seed.AfricanWorldCatalog
import com.example.domain.FinancialEngines
import com.example.ui.components.ActionFeedbackBanner
import com.example.ui.components.CitizenAvatarBadge
import com.example.ui.components.HoaAdaptivePageContainer
import com.example.ui.components.HoaEmptyStateBanner
import com.example.ui.components.HoaSectionHeader
import com.example.ui.components.HoaStatusBadge
import com.example.ui.components.KenteFiligreeDivider
import com.example.ui.state.ActionFeedback
import com.example.ui.state.SimulationActions
import com.example.ui.state.SimulationSnapshot
import com.example.ui.theme.HoaMonoStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicPlayerProfileScreen(
    playerId: Long,
    sim: SimulationSnapshot,
    playerLookupResult: Pair<String, PlayerCharacter?>?,
    feedback: ActionFeedback,
    actions: SimulationActions?,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(playerId) {
        if (playerId > 0L) actions?.lookupPlayerById(playerId)
    }

    val ownChar = sim.myCharacters.firstOrNull { it.playerId == playerId }
    val lookupChar = playerLookupResult?.second?.takeIf { it.playerId == playerId }
    val character = ownChar ?: lookupChar
    val isOwner = ownChar != null

    var previewAsPublic by remember(playerId) { mutableStateOf(!isOwner) }
    var editing by remember(playerId) { mutableStateOf(false) }
    var editOccupation by remember(character?.occupation) { mutableStateOf(character?.occupation.orEmpty()) }
    var editBio by remember(character?.biography) { mutableStateOf(character?.biography.orEmpty()) }

    val resolvedTitle = when {
        character == null -> "Player Not Found"
        character.accountStatus == "DELETED" -> "User Deleted"
        character.accountStatus == "SUSPENDED" -> "Account Suspended"
        else -> character.resolvePublicDisplayName()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("public_player_profile_root"),
    ) {
        HoaAdaptivePageContainer {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("player_profile_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    HoaSectionHeader(
                        title = resolvedTitle,
                        subtitle = "/player/$playerId • Zero XP / Zero Levels Citizen Record",
                        badgeLabel = character?.accountStatus ?: "NOT FOUND",
                    )
                }
            }

            ActionFeedbackBanner(feedback = feedback)

            when {
                character == null -> {
                    HoaEmptyStateBanner(
                        icon = Icons.Outlined.Badge,
                        title = "Player Not Found",
                        message = "No citizen record exists for /player/$playerId.",
                        phaseNote = "ID LOOKUP",
                    )
                }

                character.accountStatus == "DELETED" -> {
                    HoaEmptyStateBanner(
                        icon = Icons.Outlined.Lock,
                        title = "User Deleted",
                        message = "Citizen #$playerId has been deleted. Historical records remain labeled 'User Deleted' with personal data redacted.",
                        phaseNote = "DELETED RECORD",
                    )
                }

                character.accountStatus == "SUSPENDED" -> {
                    HoaEmptyStateBanner(
                        icon = Icons.Outlined.Lock,
                        title = "Account Suspended",
                        message = "Citizen #$playerId is currently suspended under civic compliance review.",
                        phaseNote = "SUSPENDED",
                    )
                }

                else -> {
                    val enforcePublicPrivacy = !isOwner || previewAsPublic
                    val nationality = AfricanWorldCatalog.countries.firstOrNull { it.countryId == character.nationalityCountryId }
                    val residenceCountry = AfricanWorldCatalog.countries.firstOrNull { it.countryId == character.currentCountryId }
                    val residenceState = AfricanWorldCatalog.states.firstOrNull { it.stateId == character.currentStateId }
                    val residenceCounty = AfricanWorldCatalog.counties.firstOrNull { it.countyId == character.currentCountyId }
                    val currency = residenceCountry?.currencyCode ?: "NGN"

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("player_profile_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CitizenAvatarBadge(character = character, size = 84.dp)
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(character.resolvePublicDisplayName(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Text("${character.canonicalUrl()} • ${character.characterRole}", style = HoaMonoStyle, color = MaterialTheme.colorScheme.primary)
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        HoaStatusBadge(label = "No XP / No Levels", highlighted = false)
                                        HoaStatusBadge(label = character.accountStatus, highlighted = true)
                                    }
                                }
                            }

                            if (isOwner) {
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = previewAsPublic,
                                        onClick = { previewAsPublic = !previewAsPublic },
                                        modifier = Modifier.testTag("player_profile_privacy_preview_chip"),
                                        label = { Text(if (previewAsPublic) "Viewing: Public Privacy Filter" else "Viewing: Private Owner View") },
                                    )
                                    OutlinedButton(onClick = { editing = !editing }) {
                                        Text(if (editing) "Cancel Edit" else "Edit Bio & Occupation")
                                    }
                                    OutlinedButton(onClick = onOpenSettings) {
                                        Text("Privacy Settings")
                                    }
                                }
                            }

                            KenteFiligreeDivider()

                            if (editing && isOwner) {
                                OutlinedTextField(
                                    value = editOccupation,
                                    onValueChange = { editOccupation = it },
                                    label = { Text("Occupation") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                OutlinedTextField(
                                    value = editBio,
                                    onValueChange = { editBio = it },
                                    label = { Text("Biography") },
                                    minLines = 2,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Button(
                                    onClick = {
                                        actions?.updateFullAvatarAndProfile(
                                            playerId = character.playerId,
                                            avatarStyle = character.avatarStyle,
                                            accentIdx = character.avatarAccentIndex,
                                            skinToneIdx = character.avatarSkinToneIdx,
                                            hairStyle = character.avatarHairStyle,
                                            hairColorIdx = character.avatarHairColorIdx,
                                            faceStyle = character.avatarFaceStyle,
                                            clothingStyle = character.avatarClothingStyle,
                                            accessory = character.avatarAccessory,
                                            occupation = editOccupation.ifBlank { "Citizen" },
                                            bio = editBio,
                                            interests = character.personalInterests,
                                            publicVisible = character.publicVisible,
                                        )
                                        editing = false
                                    },
                                ) {
                                    Text("Save Profile Changes")
                                }
                            } else {
                                Text(
                                    text = "Nationality: " + if (enforcePublicPrivacy && !character.showNationality) {
                                        "Hidden by privacy setting"
                                    } else {
                                        "${nationality?.flagEmoji ?: ""} ${nationality?.name ?: "Pan-African"}"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = "Current Location: " + if (enforcePublicPrivacy && !character.showResidence) {
                                        "Hidden by privacy setting"
                                    } else {
                                        listOfNotNull(
                                            character.settlementName.takeIf { it.isNotBlank() },
                                            residenceCounty?.name,
                                            residenceState?.name,
                                            residenceCountry?.name,
                                        ).joinToString(", ")
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = "Occupation: " + if (enforcePublicPrivacy && !character.showOccupation) {
                                        "Hidden by privacy setting"
                                    } else {
                                        character.occupation
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = "Personal Identity (DOB / Birthplace): " + if (enforcePublicPrivacy && !character.showPersonalIdentity) {
                                        "Private"
                                    } else {
                                        "${character.dateOfBirth} • ${character.placeOfBirth.ifBlank { "Africa" }} • ${character.genderPresentation}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                if (character.biography.isNotBlank()) {
                                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                                        Text(character.biography, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(12.dp))
                                    }
                                }
                                if (character.personalInterests.isNotEmpty()) {
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        character.personalInterests.forEach { tag -> HoaStatusBadge(label = tag, highlighted = false) }
                                    }
                                }
                            }
                        }
                    }

                    // Filtered Civic, Enterprise & Financial Snapshot honoring 8 privacy flags
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Public Records & Affiliations", style = MaterialTheme.typography.titleMedium)
                            val perCents = sim.personalAccount?.balanceCents ?: 0L
                            val capCents = sim.capitalAccount?.balanceCents ?: 0L
                            if (isOwner && !previewAsPublic || character.showPersonalBalance) {
                                Text("Personal Balance: ${FinancialEngines.formatCurrency(perCents, currency)}", style = HoaMonoStyle)
                            } else {
                                Text("Personal Balance: Hidden", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isOwner && !previewAsPublic || character.showCapitalBalance) {
                                Text("Capital Balance: ${FinancialEngines.formatCurrency(capCents, currency)}", style = HoaMonoStyle)
                            } else {
                                Text("Capital Balance: Hidden", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isOwner && !previewAsPublic || character.showNetWorth) {
                                Text("Estimated Liquid Net Worth: ${FinancialEngines.formatCurrency(perCents + capCents, currency)}", style = HoaMonoStyle)
                            } else {
                                Text("Net Worth: Hidden", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = "Employment: " + if (enforcePublicPrivacy && !character.showEmployment) "Hidden" else "${sim.employmentContracts.size} active contracts",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = "Companies: " + if (enforcePublicPrivacy && !character.showCompanies) "Hidden" else "${sim.companies.size} affiliated enterprises",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = "Politics: " + if (enforcePublicPrivacy && !character.showPolitics) "Hidden" else "${sim.politicalParties.size} party records",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = "Public Activity: " + if (enforcePublicPrivacy && !character.showActivity) "Hidden by privacy setting" else "${sim.notifications.take(3).size} recent civic updates visible",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}
