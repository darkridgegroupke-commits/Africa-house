package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.FinancialEngines
import com.example.ui.components.HoaAdaptivePageContainer
import com.example.ui.components.HoaModuleCard
import com.example.ui.components.HoaSectionHeader
import com.example.ui.components.HoaStatusBadge
import com.example.ui.components.KenteFiligreeDivider
import com.example.ui.navigation.HoaDestination
import com.example.ui.state.HoaShellUiState
import com.example.ui.state.PhaseProgress
import com.example.ui.state.SimulationSnapshot
import com.example.ui.state.UiState
import com.example.ui.state.WorldAtlasSnapshot
import com.example.ui.theme.HoaMonoStyle
import com.example.ui.theme.LocalHoaColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeDashboardScreen(
    uiState: HoaShellUiState,
    onNavigate: (HoaDestination) -> Unit,
    worldState: UiState<WorldAtlasSnapshot>? = null,
    sim: SimulationSnapshot = SimulationSnapshot(),
    onOpenPublicProfile: ((Long) -> Unit)? = null,
    onOpenCharacterWizard: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val liveAtlas = (worldState as? UiState.Success)?.data
    val settledCountry = liveAtlas?.countries?.firstOrNull {
        it.countryId == liveAtlas.userAccount?.activeCountryId
    }
    val currency = settledCountry?.currencyCode ?: "NGN"
    val activeChar = sim.activeCharacter
    val hasActiveCampaignFunds = sim.campaigns.isNotEmpty() || (sim.campaignAccount?.balanceCents ?: 0L) > 0L

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        HoaAdaptivePageContainer {
            HeroWelcomeBanner(onExploreWorld = { onNavigate(HoaDestination.AFRICA_WORLD) })

            ServerClockContractStrip(
                clockLabel = sim.formattedCalendarDate,
                tickIndex = sim.worldClock?.lastProcessedTickIndex ?: 0L,
                onOpenEconomy = { onNavigate(HoaDestination.ECONOMY) },
            )

            KenteFiligreeDivider()

            HoaSectionHeader(
                title = "Citizen Command Dashboard",
                subtitle = if (settledCountry != null) {
                    "Settled in ${settledCountry.flagEmoji} ${settledCountry.name} (/county/${liveAtlas.userAccount?.activeCountyId})"
                } else {
                    "Open Africa / World to choose your starting nation and county"
                },
                badgeLabel = activeChar?.let { "#${it.playerId} LIVE" } ?: "UPDATES 1–12 LIVE",
            )

            if (activeChar != null) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (onOpenPublicProfile != null) {
                        OutlinedButton(
                            onClick = { onOpenPublicProfile(activeChar.playerId) },
                            modifier = Modifier.testTag("dashboard_view_profile_button"),
                        ) {
                            Text("Public Profile (${activeChar.canonicalUrl()})")
                        }
                    }
                    OutlinedButton(
                        onClick = { onNavigate(HoaDestination.SETTINGS) },
                        modifier = Modifier.testTag("dashboard_settings_button"),
                    ) {
                        Text("Settings (/settings)")
                    }
                    if (onOpenCharacterWizard != null) {
                        OutlinedButton(
                            onClick = { onOpenCharacterWizard(true) },
                            modifier = Modifier.testTag("dashboard_create_alt_button"),
                        ) {
                            Text("New ALT Character")
                        }
                    }
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val twoColumns = maxWidth >= 680.dp
                val perBal = FinancialEngines.formatCurrency(sim.personalAccount?.balanceCents ?: 0L, currency)
                val capBal = FinancialEngines.formatCurrency(sim.capitalAccount?.balanceCents ?: 0L, currency)
                val cmpBal = FinancialEngines.formatCurrency(sim.campaignAccount?.balanceCents ?: 0L, currency)

                val widgets = listOf<@Composable (Modifier) -> Unit>(
                    { cardModifier ->
                        HoaModuleCard(
                            modifier = cardModifier.testTag("widget_profile_summary"),
                            title = "Citizen Overview & Immigration",
                            icon = Icons.Outlined.Badge,
                            phaseLabel = if (activeChar != null) "#${activeChar.playerId}" else "Register",
                            description = "Identity, residence, ${sim.immigrationRecords.size} immigration permits & zero-XP reputation.",
                            emptyStateTitle = activeChar?.let { "${it.fullName} (${it.characterRole})" } ?: "No citizen profile registered yet",
                            emptyStateDetail = if (settledCountry != null) {
                                "${settledCountry.flagEmoji} ${settledCountry.name} • /county/${liveAtlas.userAccount?.activeCountyId} • ${sim.notifications.size} Alerts"
                            } else {
                                "Open Character Creation or Profile to register Citizen ID #12453+."
                            },
                            specTags = listOf("Zero XP", "${sim.myCharacters.size} Chars", "${sim.immigrationRecords.size} Permits"),
                            onClick = {
                                if (activeChar != null && onOpenPublicProfile != null) {
                                    onOpenPublicProfile(activeChar.playerId)
                                } else {
                                    onNavigate(HoaDestination.PROFILE)
                                }
                            },
                        )
                    },
                    { cardModifier ->
                        HoaModuleCard(
                            modifier = cardModifier.testTag("widget_account_balances"),
                            title = "Personal, Capital & Ledger",
                            icon = Icons.Outlined.AccountBalanceWallet,
                            phaseLabel = "${sim.ledgerTransactions.size} Tx",
                            description = if (hasActiveCampaignFunds) {
                                "Double-entry Personal, Capital, and segregated Campaign funds with zero fake balances."
                            } else {
                                "Double-entry Personal and Capital accounts backed by atomic transactions."
                            },
                            emptyStateTitle = "Personal: $perBal",
                            emptyStateDetail = if (hasActiveCampaignFunds) {
                                "Capital: $capBal • Campaign: $cmpBal"
                            } else {
                                "Capital: $capBal • ${sim.bonds.size} Bonds • ${sim.properties.size} Deeds"
                            },
                            specTags = if (hasActiveCampaignFunds) {
                                listOf("Atomic Tx", "Campaign Active", "Audit Logged")
                            } else {
                                listOf("Atomic Tx", "Zero Double-Tax", "Audit Logged")
                            },
                            onClick = { onNavigate(HoaDestination.BANKING) },
                        )
                    },
                    { cardModifier ->
                        HoaModuleCard(
                            modifier = cardModifier.testTag("widget_income_employment"),
                            title = "Income & Employment",
                            icon = Icons.Outlined.WorkOutline,
                            phaseLabel = "${sim.employmentContracts.size} Active",
                            description = "Hourly wage accumulation, unpaid wage liabilities, and automated National/State/County tax routing.",
                            emptyStateTitle = sim.employmentContracts.firstOrNull()?.jobTitle ?: "No active employment contract",
                            emptyStateDetail = sim.employmentContracts.firstOrNull()?.let {
                                "Paid: ${FinancialEngines.formatCurrency(it.totalPaidCents, currency)} • Taxes: ${FinancialEngines.formatCurrency(it.totalTaxDeductedCents, currency)}"
                            } ?: "Sign a civil service or company employment contract in Banking.",
                            specTags = listOf("Hourly Wage", "Liability Ledger", "3-Tier Tax"),
                            onClick = { onNavigate(HoaDestination.BANKING) },
                        )
                    },
                    { cardModifier ->
                        HoaModuleCard(
                            modifier = cardModifier.testTag("widget_company_performance"),
                            title = "Companies & Investments",
                            icon = Icons.Outlined.Business,
                            phaseLabel = "${sim.companies.size} Reg",
                            description = "Enterprises launching with 1,000,000 shares @ 10 units plus Treasury/Corp bonds & property.",
                            emptyStateTitle = sim.selectedCompany?.name ?: "0 companies registered",
                            emptyStateDetail = sim.selectedCompany?.let {
                                "${it.registrationCertNumber} • Val: ${FinancialEngines.formatCurrency(it.valuationCents(), it.currencyCode)}"
                            } ?: "Incorporate a company, subscribe bonds (${sim.bonds.size}), or buy real estate (${sim.properties.size}).",
                            specTags = listOf("1M Shares", "${sim.bonds.size} Bonds", "${sim.properties.size} Deeds"),
                            onClick = { onNavigate(HoaDestination.COMPANIES) },
                        )
                    },
                    { cardModifier ->
                        HoaModuleCard(
                            modifier = cardModifier.testTag("widget_market_updates"),
                            title = "TSN Marketplace & Alerts",
                            icon = Icons.Outlined.Storefront,
                            phaseLabel = "${sim.marketOrders.count { it.remainingQuantity > 0 }} Open",
                            description = "County-specific order books, warehouses, and recent activity feed (${sim.auditLogs.size} events).",
                            emptyStateTitle = "${sim.tradeHistory.size} Trades Settled • ${sim.myInventory.sumOf { it.quantity }} Inventory Units",
                            emptyStateDetail = sim.notifications.firstOrNull()?.title ?: "Dynamic supply & demand pricing across every African county.",
                            specTags = listOf("County Orders", "Escrow", "${sim.notifications.size} Alerts"),
                            onClick = { onNavigate(HoaDestination.MARKETPLACE) },
                        )
                    },
                    { cardModifier ->
                        HoaModuleCard(
                            modifier = cardModifier.testTag("widget_gov_politics"),
                            title = "Government & Political Updates",
                            icon = Icons.Outlined.HowToVote,
                            phaseLabel = "${sim.elections.size} Ballots",
                            description = "14 statutory departments, audited treasuries, parties, elections & 3 segregated campaign systems.",
                            emptyStateTitle = "${sim.politicalParties.size} Parties • ${sim.campaigns.size} Campaigns",
                            emptyStateDetail = "State, National, and General campaigns strictly prohibit mixing funds.",
                            specTags = listOf("14 Depts", "3 Campaigns", "Single Ballot"),
                            onClick = { onNavigate(HoaDestination.POLITICS) },
                        )
                    },
                )

                if (twoColumns) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        widgets.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                rowItems.forEach { item -> item(Modifier.weight(1f)) }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        widgets.forEach { item -> item(Modifier.fillMaxWidth()) }
                    }
                }
            }

            HoaSectionHeader(
                title = "House of Africa • Full 12-Update Architecture",
                subtitle = "All 12 economic, governance, political, and security modules connected to Firestore",
                badgeLabel = "COMPLETE",
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("world_events_roadmap_card"),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    uiState.roadmapPhases.forEach { phase ->
                        val isActive = phase.status == PhaseProgress.ACTIVE || phase.status == PhaseProgress.COMPLETED
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(
                                        imageVector = if (isActive) Icons.Outlined.CheckCircle else Icons.Outlined.Public,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Column {
                                        Text(phase.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                        Text(phase.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                HoaStatusBadge(label = "LIVE", highlighted = true)
                            }
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { onNavigate(HoaDestination.AFRICA_WORLD) },
                            modifier = Modifier.testTag("cta_prepare_phase2"),
                        ) {
                            Text("Open Africa / World Atlas")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        OutlinedButton(
                            onClick = { onNavigate(HoaDestination.GOVERNMENT) },
                            modifier = Modifier.testTag("cta_inspect_government"),
                        ) {
                            Icon(Icons.Outlined.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("14 Gov Departments")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroWelcomeBanner(onExploreWorld: () -> Unit) {
    val hoaColors = LocalHoaColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_banner_card"),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.5.dp, hoaColors.goldAccent.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(210.dp)) {
            Image(
                painter = painterResource(id = R.drawable.img_africa_hero_banner_1795036583841),
                contentDescription = stringResource(id = R.string.cd_hero_banner),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(
                            hoaColors.forestHeader.copy(alpha = 0.80f),
                            hoaColors.forestHeader.copy(alpha = 0.58f),
                            hoaColors.forestHeader.copy(alpha = 0.90f),
                        ),
                    ),
                ),
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_hoa_emblem_1795036582659),
                        contentDescription = stringResource(id = R.string.cd_app_emblem),
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = hoaColors.goldAccent.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, hoaColors.goldAccent),
                    ) {
                        Text(
                            text = "PERSISTENT PAN-AFRICAN SIM",
                            style = HoaMonoStyle,
                            color = hoaColors.onForestHeader,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("House of Africa", style = MaterialTheme.typography.displaySmall, color = hoaColors.onForestHeader)
                    Text(
                        "Nations, companies, double-entry treasuries, campaigns & dynamic county markets.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = hoaColors.onForestHeader.copy(alpha = 0.88f),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        onClick = onExploreWorld,
                        modifier = Modifier.testTag("hero_world_button"),
                        shape = RoundedCornerShape(50),
                        color = hoaColors.goldAccent,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("Explore Africa / World", style = MaterialTheme.typography.labelLarge, color = hoaColors.onGoldContainer)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = hoaColors.onGoldContainer, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerClockContractStrip(
    clockLabel: String,
    tickIndex: Long,
    onOpenEconomy: () -> Unit,
) {
    val hoaColors = LocalHoaColors.current
    Surface(
        onClick = onOpenEconomy,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("server_clock_banner"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, hoaColors.goldAccent.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = hoaColors.goldAccent, modifier = Modifier.size(22.dp))
                Column {
                    Text("Server Clock: $clockLabel", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text("Tick #$tickIndex • 48h = 1 Yr • 1h = 7.5 Days • 200 Days = Referendum", style = HoaMonoStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HoaStatusBadge(label = "SERVER LIVE", highlighted = true)
        }
    }
}
