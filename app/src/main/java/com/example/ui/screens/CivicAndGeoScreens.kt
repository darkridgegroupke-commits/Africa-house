package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.HoaAdaptivePageContainer
import com.example.ui.components.HoaEmptyStateBanner
import com.example.ui.components.HoaModuleCard
import com.example.ui.components.HoaSectionHeader
import com.example.ui.components.KenteFiligreeDivider
import com.example.ui.navigation.HoaEntityRoutes
import com.example.ui.state.CampaignSystemTab
import com.example.ui.state.HoaShellUiState
import com.example.ui.theme.HoaMonoStyle

@Composable
fun AfricaWorldScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Africa / World Geography",
                subtitle = "Hierarchical countries, states/regions, counties, cities & settlements",
                badgeLabel = "PHASE 2 NEXT",
            )

            HoaEmptyStateBanner(
                icon = Icons.Outlined.Public,
                title = "African World Atlas Ready for Phase 2",
                message = "No fabricated country populations or dummy GDP numbers are shown. Phase 2 seeds the sovereign country, state, and county registry.",
                phaseNote = "NEXT UP • PHASE 2",
            )

            KenteFiligreeDivider()

            HoaModuleCard(
                title = "3-Tier Sovereign Hierarchy",
                icon = Icons.Outlined.LocationCity,
                phaseLabel = "Phase 2",
                description = "Every Country hosts States/Regions, which host Counties, cities, and settlements—each with its own government, treasury, population, and infrastructure.",
                emptyStateTitle = "0 countries loaded",
                emptyStateDetail = "Clean immutable IDs: ${HoaEntityRoutes.world(101)}, ${HoaEntityRoutes.state(201)}, ${HoaEntityRoutes.county(301)} (never recycled).",
                specTags = listOf("/world/101", "/state/201", "/county/301", "Unique IDs"),
            )

            HoaModuleCard(
                title = "Immigration & Internal Relocation",
                icon = Icons.Outlined.Groups,
                phaseLabel = "Phase 4",
                description = "Cross-border travel requires exit clearance and destination entry/residence permits valid for 10 in-game years (20 real-life days). Intra-country moves use internal relocation.",
                emptyStateTitle = "No active border crossings",
                emptyStateDetail = "Immigration applications, renewal dates, and permit approvals activate alongside citizen identity.",
                specTags = listOf("10-Yr Renewal", "Exit Clearance", "Entry Permit"),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GovernmentScreen(
    modifier: Modifier = Modifier,
) {
    val departments = listOf(
        "Finance", "Immigration", "Interior", "Defence", "Agriculture",
        "Mining", "Energy", "Transport", "Health", "Education",
        "Infrastructure", "Trade", "Justice", "Environment",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Government & Ministries",
                subtitle = "National, State & County administrations with audited treasuries",
                badgeLabel = "PHASE 9",
            )

            HoaEmptyStateBanner(
                icon = Icons.Outlined.AccountBalance,
                title = "No Active Administrations Seated",
                message = "Public budgets never allow unlimited money creation. Every expenditure records source, approval, recipient, and purpose.",
                phaseNote = "PLANNED • PHASE 9",
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
                    Text(
                        text = "14 Statutory Government Departments",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Officials operate within role-based legal permissions across National, State, and County jurisdictions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        departments.forEach { dept ->
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            ) {
                                Text(
                                    text = dept,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PoliticsScreen(
    uiState: HoaShellUiState,
    onSelectCampaignTab: (CampaignSystemTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Politics, Parties & Campaigns",
                subtitle = "Three strictly separated campaign systems and multi-tier elections",
                badgeLabel = "PHASE 10",
            )

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TabRow(selectedTabIndex = uiState.selectedCampaignTab.ordinal) {
                        CampaignSystemTab.entries.forEach { tab ->
                            Tab(
                                selected = uiState.selectedCampaignTab == tab,
                                onClick = { onSelectCampaignTab(tab) },
                                modifier = Modifier.testTag("campaign_tab_${tab.name.lowercase()}"),
                                text = {
                                    Text(
                                        text = tab.title.removeSuffix(" Campaign"),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                },
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = uiState.selectedCampaignTab.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Jurisdiction: ${uiState.selectedCampaignTab.scopeLabel}",
                            style = HoaMonoStyle,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = when (uiState.selectedCampaignTab) {
                                CampaignSystemTab.STATE ->
                                    "Dedicated state HQ, regional manifesto, state team, traceable donations, advertising, outreach, and state expenditure reports."
                                CampaignSystemTab.NATIONAL ->
                                    "National HQ, national manifesto, travel expenses, national advertising, donor limits, and federal expenditure audits."
                                CampaignSystemTab.GENERAL ->
                                    "General election headquarters, public events, campaign materials, voter outreach, and election authority reporting."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            HoaModuleCard(
                title = "Political Parties & Elections",
                icon = Icons.Outlined.HowToVote,
                phaseLabel = "Phase 10",
                description = "Establish parties with logos, constitutions, manifestos, and financial reports. Run in County, State, and National elections with ballot security and term rules.",
                emptyStateTitle = "0 registered parties or active ballots",
                emptyStateDetail = "Campaign funds remain strictly segregated from personal, company, government, and other campaigns' funds.",
                specTags = listOf(HoaEntityRoutes.party(601), HoaEntityRoutes.election(801), "Zero Fund Mixing"),
            )

            HoaModuleCard(
                title = "Campaign Compliance Guardrails",
                icon = Icons.Outlined.Campaign,
                phaseLabel = "Phase 10",
                description = "All donations and spending limits are enforced by election authorities. Political office salaries are taxed under standard income rules.",
                emptyStateTitle = "Awaiting election authority rules",
                emptyStateDetail = "No simulated campaign donations or placeholder vote counts in Phase 1.",
            )
        }
    }
}

@Composable
fun EconomyScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Economy, Clock & Era Referendum",
                subtitle = "Deterministic server time and 100-year continental governance milestones",
                badgeLabel = "PHASE 3 & 12",
            )

            HoaModuleCard(
                title = "Server-Controlled Game Clock",
                icon = Icons.Outlined.Schedule,
                phaseLabel = "Phase 3",
                description = "All salaries, production, taxes, elections, loans, dividends, and immigration permits follow one authoritative server clock—never the device clock.",
                emptyStateTitle = "Server clock integration pending Phase 3",
                emptyStateDetail = "48 real hours = 1 in-game year • 24h = 6 months • 12h = 3 months • 1h = 7.5 in-game days.",
                specTags = listOf("48h = 1 Yr", "1h = 7.5 Days", "Idempotent Catch-Up"),
            )

            HoaModuleCard(
                title = "200-Day / 100-Year Era Referendum",
                icon = Icons.Outlined.Gavel,
                phaseLabel = "Phase 12",
                description = "At the 200-real-day (100 in-game year) milestone, citizens vote to Continue Era, execute a Selective Partial Reset, or execute a Full Reset (>=60% valid votes required).",
                emptyStateTitle = "No referendum active",
                emptyStateDetail = "Selective resets preserve non-selected systems; legally required audit records are always retained separately.",
                specTags = listOf("200 Real Days", "60% Threshold", "Partial / Full"),
            )

            HoaModuleCard(
                title = "Macroeconomic & NPC Activity Index",
                icon = Icons.Outlined.TrendingUp,
                phaseLabel = "Phase 11",
                description = "Grouped NPC simulation drives employment, local purchasing, logistics, and market liquidity without per-second lag.",
                emptyStateTitle = "No simulated macroeconomic charts",
                emptyStateDetail = "Real national, state, and county GDP/inflation metrics compute once Phase 3–8 engines run.",
            )
        }
    }
}
