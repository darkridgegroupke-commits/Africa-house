package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Country
import com.example.data.model.County
import com.example.data.model.StateRegion
import com.example.data.model.UserAccount
import com.example.ui.components.HoaAdaptivePageContainer
import com.example.ui.components.HoaEmptyStateBanner
import com.example.ui.components.HoaSectionHeader
import com.example.ui.components.HoaStatusBadge
import com.example.ui.components.KenteFiligreeDivider
import com.example.ui.state.RelocationFeedback
import com.example.ui.state.UiState
import com.example.ui.state.WorldAtlasSnapshot
import com.example.ui.theme.HoaMonoStyle
import com.example.ui.theme.LocalHoaColors
import java.text.NumberFormat
import java.util.Locale

private fun formatPopulation(pop: Long): String =
    NumberFormat.getNumberInstance(Locale.US).format(pop)

@Composable
fun AfricaWorldAtlasScreen(
    worldState: UiState<WorldAtlasSnapshot>,
    searchQuery: String,
    bootstrapError: String?,
    onSearchQueryChange: (String) -> Unit,
    onRetryBootstrap: () -> Unit,
    onOpenCountry: (Long) -> Unit,
    onOpenCountyDirect: ((Long, Long, Long) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Africa / World Atlas",
                subtitle = "Seeded Baseline Geography • 12 Countries, 24 States & 28 Counties",
                badgeLabel = "UPDATE 1 LIVE",
            )

            if (!bootstrapError.isNullOrBlank()) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = bootstrapError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = onRetryBootstrap) {
                            Text("Retry")
                        }
                    }
                }
            }

            when (worldState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Synchronizing African nations from Firestore...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    HoaEmptyStateBanner(
                        icon = Icons.Outlined.Public,
                        title = "Unable to Load World Atlas",
                        message = worldState.message,
                        phaseNote = "FIRESTORE ERROR",
                    )
                }

                is UiState.Success -> {
                    val snapshot = worldState.data
                    CitizenResidenceBanner(
                        account = snapshot.userAccount,
                        countries = snapshot.countries,
                        onOpenCountry = onOpenCountry,
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("world_search_input"),
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                        },
                        label = { Text("Search countries, counties, cities, or currencies") },
                    )

                    val q = searchQuery.trim().lowercase()
                    val filtered = snapshot.countries.filter { c ->
                        q.isEmpty() ||
                            c.name.lowercase().contains(q) ||
                            c.capitalCity.lowercase().contains(q) ||
                            c.currencyCode.lowercase().contains(q) ||
                            c.countryId.toString().contains(q)
                    }

                    val matchingCounties = if (q.isNotEmpty()) {
                        snapshot.allCounties.filter { ct ->
                            ct.name.lowercase().contains(q) ||
                                ct.countyId.toString().contains(q) ||
                                ct.settlements.any { s -> s.lowercase().contains(q) }
                        }
                    } else {
                        emptyList()
                    }

                    if (matchingCounties.isNotEmpty()) {
                        Text(
                            text = "Matching Counties & Settlements (${matchingCounties.size})",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        matchingCounties.take(8).forEach { ct ->
                            Surface(
                                onClick = {
                                    if (onOpenCountyDirect != null) {
                                        onOpenCountyDirect(ct.countryId, ct.stateId, ct.countyId)
                                    } else {
                                        onOpenCountry(ct.countryId)
                                    }
                                },
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ct.name, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            text = "${ct.canonicalUrl()} • Cities: ${ct.settlements.joinToString(", ")}",
                                            style = HoaMonoStyle,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    HoaStatusBadge(label = "/world/${ct.countryId}")
                                }
                            }
                        }
                    }

                    if (filtered.isEmpty() && matchingCounties.isEmpty()) {
                        HoaEmptyStateBanner(
                            icon = Icons.Outlined.Public,
                            title = "No Countries or Counties Match \"$searchQuery\"",
                            message = "Try searching by country name, county, settlement/city, currency code, or immutable ID.",
                            phaseNote = "AFRICA REGISTRY",
                        )
                    } else {
                        filtered.forEach { country ->
                            CountryAtlasCard(
                                country = country,
                                isResident = snapshot.userAccount?.activeCountryId == country.countryId,
                                onClick = { onOpenCountry(country.countryId) },
                            )
                        }
                    }

                    if (snapshot.relocations.isNotEmpty()) {
                        KenteFiligreeDivider()
                        HoaSectionHeader(
                            title = "Internal Relocation Log",
                            subtitle = "Recorded intra-country residence movements",
                            badgeLabel = "${snapshot.relocations.size} LOGGED",
                        )
                        snapshot.relocations.take(5).forEach { reloc ->
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = reloc.note,
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        Text(
                                            text = "/world/${reloc.countryId} -> /state/${reloc.toStateId} -> /county/${reloc.toCountyId}",
                                            style = HoaMonoStyle,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    HoaStatusBadge(label = reloc.relocationType)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CitizenResidenceBanner(
    account: UserAccount?,
    countries: List<Country>,
    onOpenCountry: (Long) -> Unit,
) {
    val hoaColors = LocalHoaColors.current
    val settledCountry = countries.firstOrNull { it.countryId == account?.activeCountryId }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("citizen_residence_banner"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, hoaColors.goldAccent.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = if (settledCountry != null) Icons.Outlined.CheckCircle else Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = hoaColors.goldAccent,
                )
                Column {
                    Text(
                        text = if (settledCountry != null) {
                            "Current Residence: ${settledCountry.flagEmoji} ${settledCountry.name}"
                        } else {
                            "No Sovereign Residence Selected"
                        },
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = if (account != null && account.activeCountyId > 0L) {
                            "/world/${account.activeCountryId} • /state/${account.activeStateId} • /county/${account.activeCountyId}"
                        } else {
                            "Select any country -> state -> county below to settle your residence."
                        },
                        style = HoaMonoStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (settledCountry != null) {
                OutlinedButton(onClick = { onOpenCountry(settledCountry.countryId) }) {
                    Text("View")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CountryAtlasCard(
    country: Country,
    isResident: Boolean,
    onClick: () -> Unit,
) {
    val hoaColors = LocalHoaColors.current
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("country_card_${country.countryId}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (isResident) 1.5.dp else 1.dp,
            color = if (isResident) hoaColors.goldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = country.flagEmoji,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Column {
                        Text(
                            text = country.name,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "${country.canonicalUrl()}  •  Capital: ${country.capitalCity}",
                            style = HoaMonoStyle,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                HoaStatusBadge(
                    label = if (isResident) "HOME NATION" else country.currencyCode,
                    highlighted = isResident,
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                InfoPill("Pop: ${formatPopulation(country.population)}")
                InfoPill("Infra: ${country.infrastructureIndex}/100")
                InfoPill("Income Tax: ${country.incomeTaxRatePct}%")
                InfoPill("VAT: ${country.salesTaxRatePct}%")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Industries: ${country.industries.take(3).joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open ${country.name}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CountryDetailScreen(
    countryId: Long,
    worldState: UiState<WorldAtlasSnapshot>,
    onSelectCountry: (Long) -> Unit,
    onOpenState: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(countryId) {
        onSelectCountry(countryId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_from_country"),
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Back to Africa / World")
            }

            when (worldState) {
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Error -> HoaEmptyStateBanner(
                    icon = Icons.Outlined.Public,
                    title = "Country Unavailable",
                    message = worldState.message,
                    phaseNote = "ERROR",
                )
                is UiState.Success -> {
                    val country = worldState.data.countries.firstOrNull { it.countryId == countryId }
                    if (country == null) {
                        HoaEmptyStateBanner(
                            icon = Icons.Outlined.Public,
                            title = "Country Not Found (/world/$countryId)",
                            message = "This sovereign ID is unregistered.",
                            phaseNote = "IMMUTABLE ID",
                        )
                    } else {
                        HoaSectionHeader(
                            title = "${country.flagEmoji} ${country.name}",
                            subtitle = "${country.canonicalUrl()} • ${country.isoCode} • Capital: ${country.capitalCity}",
                            badgeLabel = country.currencyCode,
                        )

                        DetailSpecCard(
                            title = "National Institutions & Treasury",
                            icon = Icons.Outlined.AccountBalance,
                            rows = listOf(
                                "Parliament" to "${country.parliamentName} (${country.parliamentSeats} seats)",
                                "Currency" to "${country.currencyName} (${country.currencyCode})",
                                "National Treasury" to country.treasuryStatus,
                                "Immigration Permit" to "Renew every ${country.immigrationRenewalYears} in-game years (20 real days)",
                                "Tax Policy" to "Income ${country.incomeTaxRatePct}% • Corp ${country.corporateTaxRatePct}% • Sales ${country.salesTaxRatePct}%",
                                "Population / Infra" to "${formatPopulation(country.population)} • Index ${country.infrastructureIndex}/100",
                            ),
                            tagsTitle = "Natural Resources & Key Industries",
                            tags = country.naturalResources + country.industries,
                        )

                        KenteFiligreeDivider()

                        HoaSectionHeader(
                            title = "States & Regions (${worldState.data.statesForSelectedCountry.size})",
                            subtitle = "Select a state to inspect its governor, treasury, and counties",
                            badgeLabel = "/state/{id}",
                        )

                        worldState.data.statesForSelectedCountry.forEach { state ->
                            Card(
                                onClick = { onOpenState(state.stateId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("state_card_${state.stateId}"),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = state.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "${state.canonicalUrl()} • Capital: ${state.capitalCity}",
                                            style = HoaMonoStyle,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = state.economicFocus,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Open ${state.name}",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StateDetailScreen(
    stateId: Long,
    worldState: UiState<WorldAtlasSnapshot>,
    onSelectState: (Long) -> Unit,
    onOpenCounty: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(stateId) {
        onSelectState(stateId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_from_state"),
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Back to Country")
            }

            when (worldState) {
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Error -> HoaEmptyStateBanner(
                    icon = Icons.Outlined.Map,
                    title = "State Unavailable",
                    message = worldState.message,
                    phaseNote = "ERROR",
                )
                is UiState.Success -> {
                    val state = worldState.data.statesForSelectedCountry.firstOrNull { it.stateId == stateId }
                    if (state == null) {
                        HoaEmptyStateBanner(
                            icon = Icons.Outlined.Map,
                            title = "State Not Found (/state/$stateId)",
                            message = "Select a country first to load its constituent states.",
                            phaseNote = "IMMUTABLE ID",
                        )
                    } else {
                        HoaSectionHeader(
                            title = state.name,
                            subtitle = "${state.canonicalUrl()} • Country /world/${state.countryId}",
                            badgeLabel = "STATE TAX ${state.stateTaxRatePct}%",
                        )

                        DetailSpecCard(
                            title = "State Government & Economy",
                            icon = Icons.Outlined.LocationCity,
                            rows = listOf(
                                "State Capital" to state.capitalCity,
                                "Executive Office" to state.governorTitle,
                                "State Treasury" to state.treasuryStatus,
                                "Economic Focus" to state.economicFocus,
                                "Population / Infra" to "${formatPopulation(state.population)} • Index ${state.infrastructureIndex}/100",
                            ),
                            tagsTitle = "State Departments",
                            tags = state.departments,
                        )

                        KenteFiligreeDivider()

                        HoaSectionHeader(
                            title = "Counties (${worldState.data.countiesForSelectedState.size})",
                            subtitle = "Each county hosts its own local administration and TSN marketplace",
                            badgeLabel = "/county/{id}",
                        )

                        worldState.data.countiesForSelectedState.forEach { county ->
                            Card(
                                onClick = { onOpenCounty(county.countyId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("county_card_${county.countyId}"),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = county.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "${county.canonicalUrl()} • Settlements: ${county.settlements.joinToString(", ")}",
                                            style = HoaMonoStyle,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = county.economicSummary,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Open ${county.name}",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountyDetailScreen(
    countyId: Long,
    worldState: UiState<WorldAtlasSnapshot>,
    relocationFeedback: RelocationFeedback,
    onSettleOrRelocate: (countryId: Long, stateId: Long, countyId: Long, countyName: String, stateName: String) -> Unit,
    onBack: () -> Unit,
    onRequestCrossBorderImmigration: ((Long, Long, Long) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_from_county"),
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Back to State")
            }

            when (worldState) {
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Error -> HoaEmptyStateBanner(
                    icon = Icons.Outlined.Storefront,
                    title = "County Unavailable",
                    message = worldState.message,
                    phaseNote = "ERROR",
                )
                is UiState.Success -> {
                    val county = worldState.data.countiesForSelectedState.firstOrNull { it.countyId == countyId }
                        ?: worldState.data.allCounties.firstOrNull { it.countyId == countyId }
                    val parentState = worldState.data.statesForSelectedCountry.firstOrNull { it.stateId == county?.stateId }
                    val account = worldState.data.userAccount

                    if (county == null) {
                        HoaEmptyStateBanner(
                            icon = Icons.Outlined.Storefront,
                            title = "County Not Found (/county/$countyId)",
                            message = "Return to the parent state to load county details.",
                            phaseNote = "IMMUTABLE ID",
                        )
                    } else {
                        val isCurrentCounty = account?.activeCountyId == county.countyId
                        val isSameCountry = account?.activeCountryId == 0L || account?.activeCountryId == county.countryId

                        HoaSectionHeader(
                            title = county.name,
                            subtitle = "${county.canonicalUrl()} • /state/${county.stateId} • /world/${county.countryId}",
                            badgeLabel = if (isCurrentCounty) "SETTLED HERE" else "LOCAL",
                        )

                        DetailSpecCard(
                            title = "County Administration & Local Services",
                            icon = Icons.Outlined.Storefront,
                            rows = listOf(
                                "County Population" to formatPopulation(county.population),
                                "Infrastructure Index" to "${county.infrastructureIndex}/100",
                                "Registered Businesses" to "${county.registeredBusinesses} (County Registry)",
                                "TSN Marketplace" to county.marketplaceStatus,
                                "Economic Profile" to county.economicSummary,
                            ),
                            tagsTitle = "Cities, Settlements & Local Services",
                            tags = county.settlements + county.localServices,
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = BorderStroke(1.dp, LocalHoaColors.current.goldAccent.copy(alpha = 0.55f)),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = when {
                                        isCurrentCounty -> "Current Citizen Residence"
                                        account?.activeCountryId == 0L -> "Establish Initial Residence"
                                        isSameCountry -> "Internal County Relocation (No Visa Required)"
                                        else -> "International Immigration Required"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = when {
                                        isCurrentCounty -> "You are currently settled in ${county.name}."
                                        account?.activeCountryId == 0L -> "Settle here as your initial African county residence in Firestore."
                                        isSameCountry -> "Moving between states or counties within the same country is handled as an internal relocation."
                                        else -> "You are settled in /world/${account?.activeCountryId}. Moving across countries requires Exit Clearance and a 10-year Entry/Residence Permit."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                if (!relocationFeedback.message.isNullOrBlank()) {
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = if (relocationFeedback.isError) {
                                            MaterialTheme.colorScheme.errorContainer
                                        } else {
                                            MaterialTheme.colorScheme.primaryContainer
                                        },
                                    ) {
                                        Text(
                                            text = relocationFeedback.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (relocationFeedback.isError) {
                                                MaterialTheme.colorScheme.onErrorContainer
                                            } else {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (!isSameCountry && onRequestCrossBorderImmigration != null) {
                                            onRequestCrossBorderImmigration(
                                                county.countryId,
                                                county.stateId,
                                                county.countyId,
                                            )
                                        } else {
                                            onSettleOrRelocate(
                                                county.countryId,
                                                county.stateId,
                                                county.countyId,
                                                county.name,
                                                parentState?.name ?: "State ${county.stateId}",
                                            )
                                        }
                                    },
                                    enabled = !relocationFeedback.isBusy && !isCurrentCounty,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("settle_relocate_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                    ),
                                ) {
                                    if (relocationFeedback.isBusy) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Updating Residence...")
                                    } else {
                                        Text(
                                            when {
                                                isCurrentCounty -> "Settled in ${county.name}"
                                                account?.activeCountryId == 0L -> "Settle in ${county.name}"
                                                isSameCountry -> "Relocate Internally to ${county.name}"
                                                else -> "Apply Cross-Border Permit (10 Yrs)"
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailSpecCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    rows: List<Pair<String, String>>,
    tagsTitle: String,
    tags: List<String>,
) {
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
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }
            rows.forEach { (label, value) ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tagsTitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    tags.forEach { tag -> InfoPill(tag) }
                }
            }
        }
    }
}

@Composable
private fun InfoPill(text: String) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Text(
            text = text,
            style = HoaMonoStyle,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
