package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.seed.AfricanWorldCatalog
import com.example.ui.components.HoaAdaptivePageContainer
import com.example.ui.components.HoaSectionHeader
import com.example.ui.components.HoaStatusBadge
import com.example.ui.theme.HoaMonoStyle
import java.text.NumberFormat
import java.util.Locale

@Composable
internal fun LandingExploreAfricaSection(
    countrySearch: String,
    onCountrySearchChange: (String) -> Unit,
    onExploreCountry: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HoaSectionHeader(
            title = "Explore Africa",
            subtitle = "Inspect seeded nations, tax policies, resources & /world/{country_id} pages before registering",
            badgeLabel = "SEEDED ATLAS",
        )
        OutlinedTextField(
            value = countrySearch,
            onValueChange = onCountrySearchChange,
            label = { Text("Filter nations by name, capital or currency") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("landing_explore_search"),
        )
        val matchingCountries = AfricanWorldCatalog.countries.filter { c ->
            val q = countrySearch.trim().lowercase()
            q.isEmpty() || c.name.lowercase().contains(q) || c.capitalCity.lowercase().contains(q) || c.currencyCode.lowercase().contains(q)
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cols = when {
                maxWidth >= 820.dp -> 3
                maxWidth >= 560.dp -> 2
                else -> 1
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                matchingCountries.chunked(cols).forEach { chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        chunk.forEach { country ->
                            Card(
                                onClick = { onExploreCountry(country.countryId) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("landing_country_${country.countryId}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("${country.flagEmoji} ${country.name}", style = MaterialTheme.typography.titleSmall)
                                        HoaStatusBadge(label = country.currencyCode, highlighted = true)
                                    }
                                    Text("Capital: ${country.capitalCity}", style = MaterialTheme.typography.bodySmall)
                                    Text(country.canonicalUrl(), style = HoaMonoStyle, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        repeat(cols - chunk.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicCountryPreviewScreen(
    countryId: Long,
    onBack: () -> Unit,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val country = AfricanWorldCatalog.countries.firstOrNull { it.countryId == countryId }
        ?: AfricanWorldCatalog.countries.first()
    val states = AfricanWorldCatalog.states.filter { it.countryId == country.countryId }
    val counties = AfricanWorldCatalog.counties.filter { it.countryId == country.countryId }

    Scaffold(
        modifier = modifier.fillMaxSize().systemBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            HoaAdaptivePageContainer {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("public_world_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to landing page")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${country.flagEmoji} ${country.name}", style = MaterialTheme.typography.headlineSmall)
                        Text("${country.canonicalUrl()} • ${AfricanWorldCatalog.SEED_LABEL}", style = HoaMonoStyle, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("National Profile", style = MaterialTheme.typography.titleMedium)
                        Text("Capital: ${country.capitalCity} • Currency: ${country.currencyName} (${country.currencyCode})")
                        Text("Parliament: ${country.parliamentName} (${country.parliamentSeats} seats)")
                        Text("Baseline Population: ${NumberFormat.getNumberInstance(Locale.US).format(country.population)}")
                        Text("Taxes: Income ${country.incomeTaxRatePct}% • Corporate ${country.corporateTaxRatePct}% • Sales ${country.salesTaxRatePct}%")
                        Text("Resources: ${country.naturalResources.joinToString(", ")}")
                    }
                }
                Text("Regions & Counties (${states.size} States / ${counties.size} Counties)", style = MaterialTheme.typography.titleMedium)
                states.forEach { state ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${state.name} (${state.canonicalUrl()})", style = MaterialTheme.typography.titleSmall)
                            Text("Capital: ${state.capitalCity} • State Tax: ${state.stateTaxRatePct}% • ${state.economicFocus}", style = MaterialTheme.typography.bodySmall)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                counties.filter { it.stateId == state.stateId }.forEach { ct ->
                                    HoaStatusBadge(label = "${ct.name} (${ct.canonicalUrl()})", highlighted = false)
                                }
                            }
                        }
                    }
                }
                Button(
                    onClick = onCreateAccount,
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("public_world_register_cta"),
                ) {
                    Text("Create Account & Settle in ${country.name}")
                }
            }
        }
    }
}
