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
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Factory
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.seed.SectorFacilityCatalog
import com.example.domain.FinancialEngines
import com.example.ui.components.ActionFeedbackBanner
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
fun CompaniesScreen(
    sim: SimulationSnapshot = SimulationSnapshot(),
    feedback: ActionFeedback = ActionFeedback(),
    actions: SimulationActions? = null,
    activeCountryId: Long = 101L,
    activeStateId: Long = 201L,
    activeCountyId: Long = 301L,
    currencyCode: String = "NGN",
    modifier: Modifier = Modifier,
) {
    var companyName by remember { mutableStateOf("Kente Agri-Industries Ltd") }
    var selectedSector by remember { mutableStateOf("Farming") }
    var ownershipType by remember { mutableStateOf("PRIVATE") }
    var initialCapUnits by remember { mutableStateOf("350000") }
    var selectedFacilitySpecId by remember { mutableStateOf("small_farm") }

    val selectedCompany = sim.selectedCompany

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Companies, Equity & Facilities",
                subtitle = "1,000,000 shares @ 10 units • CEO >=5% • NPC capacity production",
                badgeLabel = "UPDATES 6 & 7",
            )

            ActionFeedbackBanner(feedback = feedback)

            // Incorporation Card (Update 6)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Register New Company (Fee: 250.00 from Capital)", style = MaterialTheme.typography.titleMedium)
                    }
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Company Legal Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("PRIVATE", "PUBLIC", "GOVERNMENT_OWNED").forEach { type ->
                            FilterChip(
                                selected = ownershipType == type,
                                onClick = { ownershipType = type },
                                label = { Text(type) },
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SectorFacilityCatalog.supportedSectors.take(7).forEach { sec ->
                            FilterChip(
                                selected = selectedSector == sec,
                                onClick = { selectedSector = sec },
                                label = { Text(sec) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = initialCapUnits,
                        onValueChange = { initialCapUnits = it.filter(Char::isDigit) },
                        label = { Text("Initial Paid-In Capital ($currencyCode)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (actions != null) {
                        Button(
                            onClick = {
                                actions.registerCompany(
                                    name = companyName,
                                    sector = selectedSector,
                                    ownershipType = ownershipType,
                                    countryId = activeCountryId,
                                    stateId = activeStateId,
                                    countyId = activeCountyId,
                                    currency = currencyCode,
                                    capitalCents = (initialCapUnits.toLongOrNull() ?: 0L) * 100L,
                                )
                            },
                            enabled = !feedback.isBusy,
                            modifier = Modifier.testTag("register_company_button"),
                        ) {
                            Text("Incorporate Company (1M Shares @ 10.00)")
                        }
                    }
                }
            }

            if (sim.companies.isEmpty()) {
                HoaEmptyStateBanner(
                    icon = Icons.Outlined.Business,
                    title = "0 Companies Registered",
                    message = "Transfer Capital Funds or earn salary/loans first, then incorporate with 1,000,000 shares @ 10.00 units.",
                    phaseNote = "NO FAKE COMPANIES",
                )
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sim.companies.forEach { co ->
                        FilterChip(
                            selected = selectedCompany?.companyId == co.companyId,
                            onClick = { actions?.selectCompany(co.companyId) },
                            label = { Text("#${co.companyId} ${co.name}") },
                        )
                    }
                }

                if (selectedCompany != null) {
                    val corpBalCents = sim.treasuryAccounts // corporate acct may be in myAccounts
                    val corpAcct = sim.myAccounts.firstOrNull { it.docId == selectedCompany.corporateAccountId }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(selectedCompany.name, style = MaterialTheme.typography.titleLarge)
                                HoaStatusBadge(label = selectedCompany.ownershipType, highlighted = true)
                            }
                            Text(
                                text = "${selectedCompany.canonicalUrl()} • ${selectedCompany.registrationCertNumber} • CEO: ${selectedCompany.ceoName}",
                                style = HoaMonoStyle,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Shares: ${selectedCompany.totalShares} @ ${FinancialEngines.formatCurrency(selectedCompany.sharePriceCents, selectedCompany.currencyCode)} = Valuation ${FinancialEngines.formatCurrency(selectedCompany.valuationCents(), selectedCompany.currencyCode)}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "Corporate Treasury: ${FinancialEngines.formatCurrency(corpAcct?.balanceCents ?: 0L, selectedCompany.currencyCode)} • OpEx YTD: ${FinancialEngines.formatCurrency(selectedCompany.operatingCostYtdCents, selectedCompany.currencyCode)}",
                                style = HoaMonoStyle,
                            )
                            if (actions != null) {
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { actions.splitCompanyShares(selectedCompany) },
                                        enabled = !feedback.isBusy,
                                        modifier = Modifier.testTag("split_shares_button"),
                                    ) {
                                        Text("2-for-1 Share Split")
                                    }
                                    OutlinedButton(
                                        onClick = { actions.issueCompanyShares(selectedCompany, 100_000L, selectedCompany.sharePriceCents) },
                                        enabled = !feedback.isBusy,
                                        modifier = Modifier.testTag("issue_shares_button"),
                                    ) {
                                        Text("IPO / Issue +100k Shares")
                                    }
                                }
                            }
                            if (sim.selectedCompanyShareholders.isNotEmpty()) {
                                Text("Shareholders (CEO >=5% verified):", style = MaterialTheme.typography.labelLarge)
                                sim.selectedCompanyShareholders.forEach { sh ->
                                    val pct = FinancialEngines.ownershipPercent(sh.sharesHeld, selectedCompany.totalShares)
                                    Text(
                                        text = "• ${sh.holderName}: ${sh.sharesHeld} shares (${String.format(java.util.Locale.US, "%.2f", pct)}%) • CEO Eligible: ${sh.ceoEligible}",
                                        style = HoaMonoStyle,
                                    )
                                }
                            }
                        }
                    }

                    KenteFiligreeDivider()

                    // Sector Facilities Builder & NPC Workforce Production (Update 7)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Factory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Sector Facilities & NPC Production", style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                text = "Production = Building Capacity × Workforce Efficiency × Input Availability. Unsold units go strictly to Company Inventory (never sales revenue).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SectorFacilityCatalog.facilities.take(6).forEach { spec ->
                                    FilterChip(
                                        selected = selectedFacilitySpecId == spec.specId,
                                        onClick = { selectedFacilitySpecId = spec.specId },
                                        label = { Text("${spec.displayName} (${FinancialEngines.formatCurrency(spec.constructionCostCents, currencyCode)})") },
                                    )
                                }
                            }
                            val chosenSpec = SectorFacilityCatalog.findSpec(selectedFacilitySpecId)
                            if (chosenSpec != null) {
                                Text(
                                    text = "Size: ${chosenSpec.sizeLabel} • Cap: ${chosenSpec.buildingCapacity} • Max NPCs: ${chosenSpec.maxWorkforce} • Input: ${chosenSpec.requiredInputGood} -> Output: ${chosenSpec.outputGood}",
                                    style = HoaMonoStyle,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                if (actions != null) {
                                    Button(
                                        onClick = { actions.buildCompanyFacility(selectedCompany, chosenSpec.specId, chosenSpec.maxWorkforce / 2) },
                                        enabled = !feedback.isBusy,
                                        modifier = Modifier.testTag("build_facility_button"),
                                    ) {
                                        Text("Construct ${chosenSpec.displayName}")
                                    }
                                }
                            }

                            sim.selectedCompanyFacilities.forEach { fac ->
                                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("${fac.facilityName} (${fac.sector})", style = MaterialTheme.typography.titleSmall)
                                            HoaStatusBadge(label = "NPC ${fac.npcEmployees}/${fac.maxWorkforce}", highlighted = true)
                                        }
                                        Text(
                                            text = "Output: ${fac.outputGood} • Cap: ${fac.buildingCapacity} • Last Produced: ${fac.lastProducedUnits} units",
                                            style = HoaMonoStyle,
                                        )
                                        if (actions != null) {
                                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedButton(
                                                    onClick = { actions.adjustFacilityNpc(fac, fac.maxWorkforce) },
                                                    enabled = !feedback.isBusy,
                                                ) {
                                                    Text("Staff Max (${fac.maxWorkforce} NPCs)")
                                                }
                                                Button(
                                                    onClick = { actions.runFacilityProduction(selectedCompany, fac) },
                                                    enabled = !feedback.isBusy,
                                                    modifier = Modifier.testTag("run_production_button"),
                                                ) {
                                                    Text("Run Production Cycle")
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
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarketplaceScreen(
    sim: SimulationSnapshot = SimulationSnapshot(),
    feedback: ActionFeedback = ActionFeedback(),
    actions: SimulationActions? = null,
    activeCountryId: Long = 101L,
    activeCountyId: Long = 301L,
    currencyCode: String = "NGN",
    modifier: Modifier = Modifier,
) {
    var selectedCommodity by remember { mutableStateOf("Maize & Grain") }
    var orderSide by remember { mutableStateOf("SELL") }
    var quantityText by remember { mutableStateOf("10") }
    var unitPriceText by remember { mutableStateOf("120") }
    var useCompanyEntity by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "TSN County Marketplace",
                subtitle = "County-scoped orders, separate Personal/Company inventories & anti-wash protection",
                badgeLabel = "UPDATE 8 LIVE",
            )

            ActionFeedbackBanner(feedback = feedback)

            // Inventories Card (Personal vs Company strictly separated)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Warehouses: Personal vs. Company Inventory", style = MaterialTheme.typography.titleMedium)
                    }
                    if (sim.myInventory.isEmpty()) {
                        Text(
                            text = "0 inventory units held. Produce goods via Company Facilities or purchase from TSN County listings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        sim.myInventory.forEach { item ->
                            Text(
                                text = "• [${item.holderCategory}] ${item.commodityName}: ${item.quantity} units in /county/${item.countyId}",
                                style = HoaMonoStyle,
                            )
                        }
                    }
                }
            }

            // Place Order Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Post Order to /county/$activeCountyId TSN Exchange", style = MaterialTheme.typography.titleMedium)
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SectorFacilityCatalog.commodities.forEach { c ->
                            FilterChip(
                                selected = selectedCommodity == c.name,
                                onClick = {
                                    selectedCommodity = c.name
                                    unitPriceText = (c.basePriceCents / 100L).toString()
                                },
                                label = { Text(c.name) },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SELL", "BUY", "AUCTION").forEach { side ->
                            FilterChip(selected = orderSide == side, onClick = { orderSide = side }, label = { Text(side) })
                        }
                        FilterChip(
                            selected = useCompanyEntity,
                            onClick = { useCompanyEntity = !useCompanyEntity },
                            label = { Text(if (useCompanyEntity) "Entity: Company" else "Entity: Personal") },
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it.filter(Char::isDigit) },
                            label = { Text("Qty") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = unitPriceText,
                            onValueChange = { unitPriceText = it.filter(Char::isDigit) },
                            label = { Text("Price ($currencyCode/unit)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (actions != null) {
                        Button(
                            onClick = {
                                actions.placeMarketOrder(
                                    countyId = activeCountyId,
                                    commodity = selectedCommodity,
                                    side = orderSide,
                                    quantity = quantityText.toLongOrNull() ?: 1L,
                                    priceCents = (unitPriceText.toLongOrNull() ?: 100L) * 100L,
                                    useCompanyEntity = useCompanyEntity,
                                )
                            },
                            enabled = !feedback.isBusy,
                            modifier = Modifier.testTag("post_market_order_button"),
                        ) {
                            Text("Submit Escrowed $orderSide Order")
                        }
                    }
                }
            }

            // Active Order Book
            val openOrders = sim.marketOrders.filter { it.remainingQuantity > 0L }
            if (openOrders.isEmpty()) {
                HoaEmptyStateBanner(
                    icon = Icons.Outlined.Storefront,
                    title = "0 Open Orders in Order Book",
                    message = "All trades require escrowed goods/funds, respect anti-wash rules, and route sales tax to National Treasury.",
                    phaseNote = "ZERO FAKE ORDERS",
                )
            } else {
                openOrders.forEach { order ->
                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${order.orderSide}: ${order.commodityName} (/county/${order.countyId})", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "${order.remainingQuantity}/${order.quantity} units @ ${FinancialEngines.formatCurrency(order.pricePerUnitCents, currencyCode)} • Seller: ${order.entityName}",
                                    style = HoaMonoStyle,
                                )
                            }
                            if (actions != null && order.orderSide != "BUY") {
                                OutlinedButton(
                                    onClick = { actions.fillSellOrder(order, order.remainingQuantity.coerceAtMost(5L), activeCountryId, useCompanyEntity) },
                                    enabled = !feedback.isBusy,
                                ) {
                                    Text("Buy 5")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
