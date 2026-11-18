package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Factory
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Storefront
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
import com.example.ui.navigation.HoaEntityRoutes
import com.example.ui.state.BankAccountCategory
import com.example.ui.state.HoaShellUiState
import com.example.ui.theme.HoaMonoStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompaniesScreen(
    modifier: Modifier = Modifier,
) {
    val sectors = listOf(
        "Agriculture", "Mining", "Manufacturing", "Energy", "Construction",
        "Transport", "Retail", "Telecoms", "Banking & Finance", "Healthcare",
        "Education", "Tourism", "Technology", "Real Estate", "Food Processing",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Companies & Production",
                subtitle = "Private, Public & Government-owned enterprises, shares, and sector facilities",
                badgeLabel = "PHASE 6 & 7",
            )

            HoaEmptyStateBanner(
                icon = Icons.Outlined.Business,
                title = "0 Companies Registered",
                message = "New enterprises launch with 1,000,000 shares at 10 currency units (10,000,000 implied valuation). CEOs must hold >=5% equity.",
                phaseNote = "PLANNED • PHASE 6 & 7",
            )

            HoaModuleCard(
                title = "Production & Facility Economics",
                icon = Icons.Outlined.Factory,
                phaseLabel = "Phase 7",
                description = "Production = Building Capacity × Workforce Efficiency × Input Availability. Ordinary workers are NPCs capped strictly by facility capacity.",
                emptyStateTitle = "No active facilities constructed",
                emptyStateDetail = "Unsold output remains in company inventory and is never booked as sales revenue until sold.",
                specTags = listOf(HoaEntityRoutes.company(501), "2-for-1 Splits", "P&L / Balance Sheet"),
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
                        text = "Supported Industry Sectors",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        sectors.forEach { sector ->
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                            ) {
                                Text(
                                    text = sector,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
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
fun MarketplaceScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "TSN Marketplace",
                subtitle = "County-specific supply & demand order books, auctions, and logistics",
                badgeLabel = "PHASE 8",
            )

            HoaEmptyStateBanner(
                icon = Icons.Outlined.Storefront,
                title = "County Order Book Awaiting Phase 8",
                message = "Both citizens and NPCs trade goods across county markets. Prices respond dynamically to inventory, shortages, surpluses, and transport costs.",
                phaseNote = "PLANNED • PHASE 8",
            )

            HoaModuleCard(
                title = "Warehouses, Contracts & Transport",
                icon = Icons.Outlined.LocalShipping,
                phaseLabel = "Phase 8",
                description = "Manage separate personal vs. company inventories, route freight across counties, and execute verified buy/sell orders and auctions.",
                emptyStateTitle = "0 active listings or contracts",
                emptyStateDetail = "Anti-manipulation guards block circular trades, fake orders, market flooding, and real-money cash-out channels.",
                specTags = listOf("County Scoped", "No Wash Trades", "No Cash-Out"),
            )
        }
    }
}

@Composable
fun BankingScreen(
    uiState: HoaShellUiState,
    onSelectAccountCategory: (BankAccountCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Banking & Central Ledger",
                subtitle = "Double-entry accounting with zero unrecorded money creation",
                badgeLabel = "PHASE 5",
            )

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TabRow(selectedTabIndex = uiState.selectedBankCategory.ordinal) {
                        BankAccountCategory.entries.forEach { category ->
                            Tab(
                                selected = uiState.selectedBankCategory == category,
                                onClick = { onSelectAccountCategory(category) },
                                modifier = Modifier.testTag("bank_tab_${category.name.lowercase()}"),
                                text = {
                                    Text(
                                        text = category.title.removeSuffix(" Funds"),
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
                            text = uiState.selectedBankCategory.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${uiState.selectedBankCategory.code}  •  Balance: -- (Ledger Inactive)",
                            style = HoaMonoStyle,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = uiState.selectedBankCategory.ruleSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            HoaModuleCard(
                title = "Double-Entry Transaction Ledger",
                icon = Icons.Outlined.ReceiptLong,
                phaseLabel = "Phase 5",
                description = "Every debit has an atomic matching credit, unique transaction ID, tax routing to National/State/County treasuries, and full audit trail.",
                emptyStateTitle = "0 ledger entries recorded",
                emptyStateDetail = "Transfers between a player's own accounts never trigger double taxation.",
                specTags = listOf("Atomic DB Tx", "Zero Negative Drift", "Audit Logged"),
            )

            HoaModuleCard(
                title = "Loans, Bonds & Investments",
                icon = Icons.Outlined.Savings,
                phaseLabel = "Phase 11",
                description = "Personal/company loans, corporate & government bonds with clear maturity schedules, and collateral tracking. Loan proceeds are recorded as liabilities, never income.",
                emptyStateTitle = "No active debt or bond instruments",
                emptyStateDetail = "Repayment schedules and default rules activate in Phase 11.",
            )

            HoaModuleCard(
                title = "Financial Security Controls",
                icon = Icons.Outlined.Lock,
                phaseLabel = "Phase 5 / 12",
                description = "Guards against race conditions, double spending, duplicate salary payouts, and unapproved balance edits.",
                emptyStateTitle = "Ledger safeguards specified",
                emptyStateDetail = "Administrators cannot edit balances without an audited transaction.",
            )
        }
    }
}
