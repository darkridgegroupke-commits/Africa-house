package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Country
import com.example.data.model.County
import com.example.data.model.StateRegion
import com.example.data.seed.AfricanWorldCatalog
import com.example.domain.FinancialEngines
import com.example.ui.components.AvatarOptionsCatalog
import com.example.ui.components.CitizenAvatarBadge
import com.example.ui.theme.HoaMonoStyle
import com.example.ui.theme.LocalHoaColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WizardAvatarStepCard(
    avatarStyle: String,
    accentIdx: Int,
    skinToneIdx: Int,
    hairStyle: String,
    hairColorIdx: Int,
    faceStyle: String,
    clothingStyle: String,
    accessory: String,
    onAvatarStyle: (String) -> Unit,
    onAccentIdx: (Int) -> Unit,
    onSkinToneIdx: (Int) -> Unit,
    onHairStyle: (String) -> Unit,
    onHairColorIdx: (Int) -> Unit,
    onFaceStyle: (String) -> Unit,
    onClothingStyle: (String) -> Unit,
    onAccessory: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CitizenAvatarBadge(
                    avatarStyle = avatarStyle,
                    accentIndex = accentIdx,
                    size = 96.dp,
                    skinToneIdx = skinToneIdx,
                    hairStyle = hairStyle,
                    hairColorIdx = hairColorIdx,
                    faceStyle = faceStyle,
                    clothingStyle = clothingStyle,
                    accessory = accessory,
                    modifier = Modifier.testTag("wizard_avatar_preview"),
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Portrait Studio", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${AvatarOptionsCatalog.skinToneNames[skinToneIdx]} • ${AvatarOptionsCatalog.hairStyleLabels[hairStyle]}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "${AvatarOptionsCatalog.clothingLabels[clothingStyle]} • ${AvatarOptionsCatalog.accessoryLabels[accessory]}",
                        style = HoaMonoStyle,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Text("Skin Tone", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AvatarOptionsCatalog.skinToneNames.forEachIndexed { idx, label ->
                    FilterChip(selected = skinToneIdx == idx, onClick = { onSkinToneIdx(idx) }, label = { Text(label) })
                }
            }

            Text("Hairstyle", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AvatarOptionsCatalog.hairStyles.forEach { st ->
                    FilterChip(selected = hairStyle == st, onClick = { onHairStyle(st) }, label = { Text(AvatarOptionsCatalog.hairStyleLabels[st] ?: st) })
                }
            }

            Text("Hair Color & Expression", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AvatarOptionsCatalog.hairColorNames.forEachIndexed { idx, label ->
                    FilterChip(selected = hairColorIdx == idx, onClick = { onHairColorIdx(idx) }, label = { Text(label) })
                }
                AvatarOptionsCatalog.faceStyles.forEach { face ->
                    FilterChip(selected = faceStyle == face, onClick = { onFaceStyle(face) }, label = { Text(face) })
                }
            }

            Text("Clothing Style & Accessories", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AvatarOptionsCatalog.clothingStyles.forEach { cloth ->
                    FilterChip(selected = clothingStyle == cloth, onClick = { onClothingStyle(cloth) }, label = { Text(AvatarOptionsCatalog.clothingLabels[cloth] ?: cloth) })
                }
                AvatarOptionsCatalog.accessories.forEach { acc ->
                    FilterChip(selected = accessory == acc, onClick = { onAccessory(acc) }, label = { Text(AvatarOptionsCatalog.accessoryLabels[acc] ?: acc) })
                }
            }

            Text("Crest Emblem & Accent Ring", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AvatarOptionsCatalog.emblemStyles.forEach { emb ->
                    FilterChip(selected = avatarStyle == emb, onClick = { onAvatarStyle(emb) }, label = { Text(emb) })
                }
                listOf(0, 1, 2, 3).forEach { idx ->
                    FilterChip(selected = accentIdx == idx, onClick = { onAccentIdx(idx) }, label = { Text("Ring ${idx + 1}") })
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WizardLocationStepCard(
    selectedCountry: Country,
    availableStates: List<StateRegion>,
    selectedState: StateRegion,
    availableCounties: List<County>,
    selectedCounty: County,
    availableSettlements: List<String>,
    settlementName: String,
    onSelectCountry: (Country) -> Unit,
    onSelectState: (StateRegion) -> Unit,
    onSelectCounty: (County) -> Unit,
    onSelectSettlement: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Choose Starting Residence", style = MaterialTheme.typography.titleMedium)
            Text("1. Country", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AfricanWorldCatalog.countries.forEach { c ->
                    FilterChip(
                        selected = selectedCountry.countryId == c.countryId,
                        onClick = { onSelectCountry(c) },
                        modifier = Modifier.testTag("wizard_country_${c.countryId}"),
                        label = { Text("${c.flagEmoji} ${c.name}") },
                    )
                }
            }
            Text("2. State / Region", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                availableStates.forEach { st ->
                    FilterChip(selected = selectedState.stateId == st.stateId, onClick = { onSelectState(st) }, label = { Text(st.name) })
                }
            }
            Text("3. County & Settlement", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                availableCounties.forEach { ct ->
                    FilterChip(selected = selectedCounty.countyId == ct.countyId, onClick = { onSelectCounty(ct) }, label = { Text(ct.name) })
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                availableSettlements.forEach { sName ->
                    FilterChip(selected = settlementName == sName, onClick = { onSelectSettlement(sName) }, label = { Text(sName) })
                }
            }
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Local Economic Summary", style = MaterialTheme.typography.titleSmall)
                    Text("${selectedCountry.flagEmoji} ${selectedCountry.name} (${selectedCountry.currencyCode}) • Capital: ${selectedCountry.capitalCity}", style = MaterialTheme.typography.bodySmall)
                    Text("Taxes: Income ${selectedCountry.incomeTaxRatePct}% • Corporate ${selectedCountry.corporateTaxRatePct}% • Sales ${selectedCountry.salesTaxRatePct}% • State ${selectedState.stateTaxRatePct}%", style = HoaMonoStyle)
                    Text(selectedCounty.economicSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WizardReviewStepCard(
    builtName: String,
    dobStr: String,
    gender: String,
    nationalityCountry: Country,
    selectedCountry: Country,
    selectedState: StateRegion,
    selectedCounty: County,
    settlementName: String,
    occupation: String,
    selectedInterests: Set<String>,
    avatarStyle: String,
    accentIdx: Int,
    skinToneIdx: Int,
    hairStyle: String,
    hairColorIdx: Int,
    faceStyle: String,
    clothingStyle: String,
    accessory: String,
    asAlt: Boolean,
    onJumpStep: (Int) -> Unit,
) {
    val hoaColors = LocalHoaColors.current
    Card(
        modifier = Modifier.fillMaxWidth().testTag("wizard_review_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, hoaColors.goldAccent.copy(alpha = 0.65f)),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                CitizenAvatarBadge(
                    avatarStyle = avatarStyle,
                    accentIndex = accentIdx,
                    size = 84.dp,
                    skinToneIdx = skinToneIdx,
                    hairStyle = hairStyle,
                    hairColorIdx = hairColorIdx,
                    faceStyle = faceStyle,
                    clothingStyle = clothingStyle,
                    accessory = accessory,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(builtName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${nationalityCountry.flagEmoji} ${nationalityCountry.name} Citizen • $gender", style = MaterialTheme.typography.bodySmall)
                    Text("Permanent ID (#12453+) assigned on creation • DOB private by default ($dobStr)", style = HoaMonoStyle, color = MaterialTheme.colorScheme.primary)
                }
            }
            Text("Settlement: $settlementName, ${selectedCounty.name}, ${selectedState.name}, ${selectedCountry.name}", style = MaterialTheme.typography.bodyMedium)
            Text("Vocation: ${occupation.ifBlank { "Citizen" }}", style = MaterialTheme.typography.bodySmall)
            if (selectedInterests.isNotEmpty()) {
                Text("Interests: ${selectedInterests.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
            }
            if (!asAlt) {
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
                    Text(
                        text = "Starter Citizen Grant (MAIN account once per user): Personal ${FinancialEngines.formatCurrency(FinancialEngines.INITIAL_PERSONAL_BALANCE_CENTS, selectedCountry.currencyCode)} + Capital ${FinancialEngines.formatCurrency(FinancialEngines.INITIAL_CAPITAL_BALANCE_CENTS, selectedCountry.currencyCode)}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(1 to "Identity", 2 to "Avatar", 3 to "Location", 4 to "Bio").forEach { (editStep, label) ->
                    OutlinedButton(onClick = { onJumpStep(editStep) }) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit $label")
                    }
                }
            }
        }
    }
}
