package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.PlayerCharacter
import com.example.data.seed.AfricanWorldCatalog
import com.example.domain.GameClockEngine
import com.example.domain.OnboardingEngine
import com.example.ui.components.ActionFeedbackBanner
import com.example.ui.components.HoaAdaptivePageContainer
import com.example.ui.components.HoaSectionHeader
import com.example.ui.state.ActionFeedback
import com.example.ui.state.SimulationActions
import com.example.ui.state.SimulationSnapshot
import com.example.ui.theme.HoaMonoStyle

private val genderOptions = listOf("Female", "Male", "Non-binary", "Prefer not to say")
private val interestTags = listOf(
    "Commerce", "Mining", "Agritech", "Governance", "Banking",
    "Politics", "Manufacturing", "Diplomacy", "Logistics", "Energy",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterCreationWizardScreen(
    sim: SimulationSnapshot,
    feedback: ActionFeedback,
    actions: SimulationActions?,
    asAlt: Boolean = false,
    onCompleted: (PlayerCharacter) -> Unit,
    onCancelAlt: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val calendarNow = remember(sim.currentGameDay, sim.worldClock?.eraNumber) {
        GameClockEngine.toGameCalendarDate(sim.worldClock?.eraNumber ?: 1, sim.currentGameDay)
    }
    val currentGameYear = calendarNow.year

    var step by remember { mutableIntStateOf(1) }
    var stepError by remember { mutableStateOf<String?>(null) }

    // Step 1: Identity
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(genderOptions.first()) }
    var birthYear by remember(currentGameYear) { mutableIntStateOf(currentGameYear - 22) }
    var birthMonth by remember { mutableIntStateOf(3) }
    var birthDay by remember { mutableIntStateOf(15) }
    var nationalityCountryId by remember { mutableLongStateOf(101L) }
    var birthCountryId by remember { mutableLongStateOf(101L) }

    // Step 2: Avatar Studio
    var avatarStyle by remember { mutableStateOf("ROYAL_KENTE") }
    var accentIdx by remember { mutableIntStateOf(0) }
    var skinToneIdx by remember { mutableIntStateOf(2) }
    var hairStyle by remember { mutableStateOf("SHORT_FADE") }
    var hairColorIdx by remember { mutableIntStateOf(0) }
    var faceStyle by remember { mutableStateOf("CONFIDENT") }
    var clothingStyle by remember { mutableStateOf("KENTE_ROBE") }
    var accessory by remember { mutableStateOf("NONE") }

    // Step 3: Starting Location (strictly seeded DB catalog)
    var currentCountryId by remember { mutableLongStateOf(101L) }
    var currentStateId by remember { mutableLongStateOf(201L) }
    var currentCountyId by remember { mutableLongStateOf(301L) }
    var settlementName by remember { mutableStateOf("Victoria Island") }

    // Step 4: Biography
    var occupation by remember { mutableStateOf("Emerging Entrepreneur") }
    var biography by remember { mutableStateOf("") }
    var backgroundStory by remember { mutableStateOf("") }
    var selectedInterests by remember { mutableStateOf(setOf("Commerce", "Governance")) }

    val selectedCountry = AfricanWorldCatalog.countries.firstOrNull { it.countryId == currentCountryId }
        ?: AfricanWorldCatalog.countries.first()
    val availableStates = AfricanWorldCatalog.states.filter { it.countryId == selectedCountry.countryId }
    val selectedState = availableStates.firstOrNull { it.stateId == currentStateId } ?: availableStates.first()
    val availableCounties = AfricanWorldCatalog.counties.filter { it.stateId == selectedState.stateId }
    val selectedCounty = availableCounties.firstOrNull { it.countyId == currentCountyId } ?: availableCounties.first()
    val availableSettlements = selectedCounty.settlements.ifEmpty { listOf(selectedCounty.name) }
    val nationalityCountry = AfricanWorldCatalog.countries.firstOrNull { it.countryId == nationalityCountryId }
        ?: selectedCountry
    val birthCountry = AfricanWorldCatalog.countries.firstOrNull { it.countryId == birthCountryId }
        ?: nationalityCountry
    val birthDefaultState = AfricanWorldCatalog.states.firstOrNull { it.countryId == birthCountry.countryId } ?: selectedState
    val birthDefaultCounty = AfricanWorldCatalog.counties.firstOrNull { it.stateId == birthDefaultState.stateId } ?: selectedCounty

    BackHandler(enabled = step > 1 || (asAlt && onCancelAlt != null)) {
        stepError = null
        if (step > 1) step -= 1 else onCancelAlt?.invoke()
    }

    val stepTitle = when (step) {
        1 -> "Step 1 of 6 • Character Identity"
        2 -> "Step 2 of 6 • Avatar Generation"
        3 -> "Step 3 of 6 • Starting Location"
        4 -> "Step 4 of 6 • Character Biography"
        5 -> "Step 5 of 6 • Citizen Dossier Review"
        else -> "Step 6 of 6 • Finalize & Enter World"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("character_wizard_root"),
    ) {
        HoaAdaptivePageContainer {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (step > 1 || (asAlt && onCancelAlt != null)) {
                    IconButton(
                        onClick = {
                            stepError = null
                            if (step > 1) step -= 1 else onCancelAlt?.invoke()
                        },
                        modifier = Modifier.testTag("wizard_back_button"),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous step")
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    HoaSectionHeader(
                        title = if (asAlt) "New ALT Character Wizard" else "Create Your Citizen Character",
                        subtitle = stepTitle,
                        badgeLabel = if (asAlt) "ALT" else "MAIN",
                    )
                }
            }

            LinearProgressIndicator(
                progress = { step / 6f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
            )

            ActionFeedbackBanner(feedback = feedback)

            if (!stepError.isNullOrBlank()) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Text(
                        text = stepError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.fillMaxWidth().padding(12.dp).testTag("wizard_error_banner"),
                    )
                }
            }

            when (step) {
                1 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Identity & Citizenship", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it; stepError = null },
                                label = { Text("First Name *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("wizard_first_name"),
                            )
                            OutlinedTextField(
                                value = middleName,
                                onValueChange = { middleName = it },
                                label = { Text("Middle Name (Optional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("wizard_middle_name"),
                            )
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it; stepError = null },
                                label = { Text("Last Name *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("wizard_last_name"),
                            )

                            Text("Gender Presentation", style = MaterialTheme.typography.labelLarge)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                genderOptions.forEach { g ->
                                    FilterChip(
                                        selected = gender == g,
                                        onClick = { gender = g },
                                        label = { Text(g) },
                                    )
                                }
                            }

                            Text(
                                text = "In-Game Date of Birth (Minimum Age: ${OnboardingEngine.MIN_CITIZEN_AGE_YEARS} In-Game Years)",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            val calcAge = OnboardingEngine.computeCharacterAgeYears(birthYear, currentGameYear)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${OnboardingEngine.formatGameDateOfBirth(birthYear, birthMonth, birthDay)} • Age $calcAge yrs",
                                    style = HoaMonoStyle,
                                    color = if (calcAge >= OnboardingEngine.MIN_CITIZEN_AGE_YEARS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(onClick = { birthYear -= 1; stepError = null }, modifier = Modifier.testTag("wizard_age_older_btn")) { Text("+1 Yr") }
                                    OutlinedButton(onClick = { birthYear += 1; stepError = null }, modifier = Modifier.testTag("wizard_age_younger_btn")) { Text("-1 Yr") }
                                }
                            }

                            Text("Nationality & Country of Birth", style = MaterialTheme.typography.labelLarge)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                AfricanWorldCatalog.countries.forEach { c ->
                                    FilterChip(
                                        selected = nationalityCountryId == c.countryId,
                                        onClick = {
                                            nationalityCountryId = c.countryId
                                            birthCountryId = c.countryId
                                        },
                                        label = { Text("${c.flagEmoji} ${c.name}") },
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> WizardAvatarStepCard(
                    avatarStyle = avatarStyle,
                    accentIdx = accentIdx,
                    skinToneIdx = skinToneIdx,
                    hairStyle = hairStyle,
                    hairColorIdx = hairColorIdx,
                    faceStyle = faceStyle,
                    clothingStyle = clothingStyle,
                    accessory = accessory,
                    onAvatarStyle = { avatarStyle = it },
                    onAccentIdx = { accentIdx = it },
                    onSkinToneIdx = { skinToneIdx = it },
                    onHairStyle = { hairStyle = it },
                    onHairColorIdx = { hairColorIdx = it },
                    onFaceStyle = { faceStyle = it },
                    onClothingStyle = { clothingStyle = it },
                    onAccessory = { accessory = it },
                )

                3 -> WizardLocationStepCard(
                    selectedCountry = selectedCountry,
                    availableStates = availableStates,
                    selectedState = selectedState,
                    availableCounties = availableCounties,
                    selectedCounty = selectedCounty,
                    availableSettlements = availableSettlements,
                    settlementName = settlementName,
                    onSelectCountry = { c ->
                        currentCountryId = c.countryId
                        val st = AfricanWorldCatalog.states.first { it.countryId == c.countryId }
                        currentStateId = st.stateId
                        val ct = AfricanWorldCatalog.counties.first { it.stateId == st.stateId }
                        currentCountyId = ct.countyId
                        settlementName = ct.settlements.firstOrNull() ?: ct.name
                    },
                    onSelectState = { st ->
                        currentStateId = st.stateId
                        val ct = AfricanWorldCatalog.counties.first { it.stateId == st.stateId }
                        currentCountyId = ct.countyId
                        settlementName = ct.settlements.firstOrNull() ?: ct.name
                    },
                    onSelectCounty = { ct ->
                        currentCountyId = ct.countyId
                        settlementName = ct.settlements.firstOrNull() ?: ct.name
                    },
                    onSelectSettlement = { settlementName = it },
                )

                4 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Biography & Ambitions (Optional)", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(
                                value = occupation,
                                onValueChange = { occupation = it },
                                label = { Text("Starting Vocation / Occupation") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("wizard_occupation"),
                            )
                            OutlinedTextField(
                                value = biography,
                                onValueChange = { biography = it },
                                label = { Text("Public Biography") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth().testTag("wizard_biography"),
                            )
                            OutlinedTextField(
                                value = backgroundStory,
                                onValueChange = { backgroundStory = it },
                                label = { Text("Background Story") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth().testTag("wizard_background"),
                            )
                            Text("Personal Interests", style = MaterialTheme.typography.labelLarge)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                interestTags.forEach { tag ->
                                    val picked = tag in selectedInterests
                                    FilterChip(
                                        selected = picked,
                                        onClick = {
                                            selectedInterests = if (picked) selectedInterests - tag else selectedInterests + tag
                                        },
                                        label = { Text(tag) },
                                    )
                                }
                            }
                        }
                    }
                }

                5, 6 -> WizardReviewStepCard(
                    builtName = OnboardingEngine.buildFullName(firstName, middleName, lastName),
                    dobStr = OnboardingEngine.formatGameDateOfBirth(birthYear, birthMonth, birthDay),
                    gender = gender,
                    nationalityCountry = nationalityCountry,
                    selectedCountry = selectedCountry,
                    selectedState = selectedState,
                    selectedCounty = selectedCounty,
                    settlementName = settlementName,
                    occupation = occupation,
                    selectedInterests = selectedInterests,
                    avatarStyle = avatarStyle,
                    accentIdx = accentIdx,
                    skinToneIdx = skinToneIdx,
                    hairStyle = hairStyle,
                    hairColorIdx = hairColorIdx,
                    faceStyle = faceStyle,
                    clothingStyle = clothingStyle,
                    accessory = accessory,
                    asAlt = asAlt,
                    onJumpStep = { step = it },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step -= 1; stepError = null },
                        modifier = Modifier.height(48.dp).testTag("wizard_prev_button"),
                    ) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (step < 5) {
                    Button(
                        onClick = {
                            if (step == 1) {
                                val res = OnboardingEngine.validateCharacterIdentity(
                                    firstName = firstName,
                                    lastName = lastName,
                                    birthGameYear = birthYear,
                                    birthMonth = birthMonth,
                                    birthDay = birthDay,
                                    currentGameYear = currentGameYear,
                                )
                                if (res is OnboardingEngine.ValidationResult.Invalid) {
                                    stepError = res.message
                                    return@Button
                                }
                            }
                            stepError = null
                            step += 1
                        },
                        modifier = Modifier.height(48.dp).testTag("wizard_next_button"),
                    ) {
                        Text("Continue")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Button(
                        onClick = {
                            val dob = OnboardingEngine.formatGameDateOfBirth(birthYear, birthMonth, birthDay)
                            actions?.createWizardCharacter(
                                firstName = firstName.trim(),
                                middleName = middleName.trim(),
                                lastName = lastName.trim(),
                                gender = gender,
                                dateOfBirth = dob,
                                nationalityCountryId = nationalityCountry.countryId,
                                birthCountryId = birthCountry.countryId,
                                birthStateId = birthDefaultState.stateId,
                                birthCountyId = birthDefaultCounty.countyId,
                                placeOfBirth = "${birthDefaultCounty.name}, ${birthCountry.name}",
                                currentCountryId = selectedCountry.countryId,
                                currentStateId = selectedState.stateId,
                                currentCountyId = selectedCounty.countyId,
                                settlementName = settlementName,
                                currencyCode = selectedCountry.currencyCode,
                                occupation = occupation.ifBlank { "Citizen" },
                                biography = biography.ifBlank { "Settled in ${selectedCounty.name}, ${selectedCountry.name}." },
                                backgroundStory = backgroundStory,
                                personalInterests = selectedInterests.toList(),
                                avatarStyle = avatarStyle,
                                avatarAccentIndex = accentIdx,
                                avatarSkinToneIdx = skinToneIdx,
                                avatarHairStyle = hairStyle,
                                avatarHairColorIdx = hairColorIdx,
                                avatarFaceStyle = faceStyle,
                                avatarClothingStyle = clothingStyle,
                                avatarAccessory = accessory,
                                asAlt = asAlt,
                                onCreated = onCompleted,
                            )
                        },
                        enabled = !feedback.isBusy,
                        modifier = Modifier.height(48.dp).testTag("wizard_create_character_button"),
                    ) {
                        if (feedback.isBusy) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Creating Citizen...")
                        } else {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Character")
                        }
                    }
                }
            }
        }
    }
}
