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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.HoaAdaptivePageContainer
import com.example.ui.components.HoaModuleCard
import com.example.ui.components.HoaSectionHeader
import com.example.ui.components.HoaStatusBadge
import com.example.ui.components.KenteFiligreeDivider
import com.example.ui.navigation.HoaDestination
import com.example.ui.state.HoaShellUiState
import com.example.ui.state.PhaseProgress
import com.example.ui.theme.HoaMonoStyle
import com.example.ui.theme.LocalHoaColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeDashboardScreen(
    uiState: HoaShellUiState,
    onNavigate: (HoaDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        HoaAdaptivePageContainer {
            HeroWelcomeBanner(onExploreWorld = { onNavigate(HoaDestination.AFRICA_WORLD) })

            ServerClockContractStrip(onOpenEconomy = { onNavigate(HoaDestination.ECONOMY) })

            KenteFiligreeDivider()

            HoaSectionHeader(
                title = "Citizen Command Dashboard",
                subtitle = "Phase 1 UI shell — realistic empty states prior to backend persistence",
                badgeLabel = "PHASE 1 SHELL",
            )

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val twoColumns = maxWidth >= 680.dp
                val widgets = listOf<@Composable (Modifier) -> Unit>(
                    { cardModifier ->
                        HoaModuleCard(
                            modifier = cardModifier.testTag("widget_profile_summary"),
                            title = "Player Profile Summary",
                            icon = Icons.Outlined.Badge,
                            phaseLabel = "Phase 4",
                            description = "Citizen identity, nationality, current country/state/county, occupation, and 16-section dossier.",
                            emptyStateTitle = "No citizen character created",
                            emptyStateDetail = "Identity generation, avatar customization, and ALT characters unlock in Phase 4.",
                            specTags = listOf("16 Tabs", "No Levels/XP", "ALT Support"),
                            onClick = { onNavigate(HoaDestination.PROFILE) },
                        )
                    },
                    { cardModifier ->
                        HoaModuleCard(
                            modifier = cardModifier.testTag("widget_account_balances"),
                            title = "Central Ledger & Accounts",
                            icon = Icons.Outlined.AccountBalanceWallet,
                            phaseLabel = "Phase 5",
                            description = "Double-entry Personal Funds, Capital Funds, and ring-fenced Campaign Funds.",
                            emptyStateTitle = "Balances unprovisioned (--)",
                            emptyStateDetail = "No simulated balances or fake transactions. Real ledger accounts activate in Phase 5.",
                            specTags = listOf("Personal: --", "Capital: --", "Campaign: --"),
                            onClick = { onNavigate(HoaDestination.BANKING) },
                        )
                    },
                    { cardModifier ->
                        HoaModuleCard(
                            modifier = cardModifier.testTag("widget_income_employment"),
                            title = "Income & Employment",
                            icon = Icons.Outlined.WorkOutline,
                            phaseLabel = "Phase 5 / 7",
                            description = "Hourly wage tracking, paid vs. outstanding salary liabilities, bonuses, and tax deductions.",
                            emptyStateTitle = "No active employment record",
                            emptyStateDetail = "Salaries accrue against the server clock and track unpaid liabilities cleanly.",
                            specTags = listOf("Hourly Rate", "Liability Ledger", "Tax Deducted"),
                            onClick = { onNavigate(HoaDestination.PROFILE) },
                        )
                    },
                    { cardModifier ->
                        HoaModuleCard(
                            modifier = cardModifier.testTag("widget_company_performance"),
                            title = "Company Performance",
                            icon = Icons.Outlined.Business,
                            phaseLabel = "Phase 6 / 7",
                            description = "Private, Public & State-Owned companies starting at 1,000,000 shares @ 10 units.",
                            emptyStateTitle = "0 companies registered",
                            emptyStateDetail = "Production requires facilities, NPC workforce capacity, and real inputs—buildings never mint money alone.",
                            specTags = listOf("1M Shares", "2-for-1 Splits", "NPC Workforce"),
                            onClick = { onNavigate(HoaDestination.COMPANIES) },
                        )
                    },
                    { cardModifier ->
                        HoaModuleCard(
                            modifier = cardModifier.testTag("widget_market_updates"),
                            title = "TSN Marketplace Updates",
                            icon = Icons.Outlined.Storefront,
                            phaseLabel = "Phase 8",
                            description = "County-specific order books, warehouses, transport logistics, and dynamic supply/demand pricing.",
                            emptyStateTitle = "County exchange awaiting Phase 8",
                            emptyStateDetail = "Anti-wash trade safeguards and separate company/personal inventories.",
                            specTags = listOf("County Orders", "Dynamic Price", "Logistics"),
                            onClick = { onNavigate(HoaDestination.MARKETPLACE) },
                        )
                    },
                    { cardModifier ->
                        HoaModuleCard(
                            modifier = cardModifier.testTag("widget_gov_politics"),
                            title = "Government & Political Updates",
                            icon = Icons.Outlined.HowToVote,
                            phaseLabel = "Phase 9 / 10",
                            description = "National, State & County treasuries, 14 departments, parties, elections, and 3 separate campaigns.",
                            emptyStateTitle = "No active elections or offices",
                            emptyStateDetail = "State, National, and General campaigns keep strictly segregated budgets.",
                            specTags = listOf("14 Depts", "3 Campaigns", "Term Rules"),
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
                                rowItems.forEach { item ->
                                    item(Modifier.weight(1f))
                                }
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
                title = "World Events & Architecture Blueprint",
                subtitle = "12-phase rollout from UI shell to full persistent multiplayer simulation",
                badgeLabel = "NEXT: PHASE 2",
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
                        val isCurrent = phase.status == PhaseProgress.ACTIVE
                        val isNext = phase.status == PhaseProgress.PREPARED_NEXT
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = when {
                                isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                isNext -> LocalHoaColors.current.goldContainer.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.surfaceContainerLow
                            },
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
                                        imageVector = if (isCurrent) Icons.Outlined.CheckCircle else Icons.Outlined.Public,
                                        contentDescription = null,
                                        tint = if (isCurrent) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Column {
                                        Text(
                                            text = phase.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = phase.summary,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                HoaStatusBadge(
                                    label = when (phase.status) {
                                        PhaseProgress.ACTIVE -> "ACTIVE"
                                        PhaseProgress.PREPARED_NEXT -> "NEXT"
                                        PhaseProgress.PLANNED -> "PLANNED"
                                    },
                                    highlighted = isCurrent || isNext,
                                )
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
                            Text("Preview Phase 2: Africa / World")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        OutlinedButton(
                            onClick = { onNavigate(HoaDestination.GOVERNMENT) },
                            modifier = Modifier.testTag("cta_inspect_government"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountBalance,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
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
private fun HeroWelcomeBanner(
    onExploreWorld: () -> Unit,
) {
    val hoaColors = LocalHoaColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_banner_card"),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.5.dp, hoaColors.goldAccent.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_africa_hero_banner_1795036583841),
                contentDescription = stringResource(id = R.string.cd_hero_banner),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xCC071F14),
                                Color(0x990D3B26),
                                Color(0xE6071710),
                            ),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_hoa_emblem_1795036582659),
                            contentDescription = stringResource(id = R.string.cd_app_emblem),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0x40D4AF37),
                            border = BorderStroke(1.dp, Color(0xFFD4AF37)),
                        ) {
                            Text(
                                text = "PERSISTENT PAN-AFRICAN SIM",
                                style = HoaMonoStyle,
                                color = Color(0xFFF7F5EE),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "House of Africa",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color(0xFFF7F5EE),
                    )
                    Text(
                        text = "Nations, companies, double-entry treasuries, campaigns & dynamic county markets.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE4E0D2),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        onClick = onExploreWorld,
                        modifier = Modifier.testTag("hero_world_button"),
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFD4AF37),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "Inspect World Blueprint",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF122119),
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF122119),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerClockContractStrip(
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
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = hoaColors.goldAccent,
                    modifier = Modifier.size(22.dp),
                )
                Column {
                    Text(
                        text = "Server Game Clock: Offline (Phase 3)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "48h = 1 Yr  •  1h = 7.5 Days  •  200 Days = 100-Yr Referendum",
                        style = HoaMonoStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HoaStatusBadge(label = "SYNC PENDING")
        }
    }
}
