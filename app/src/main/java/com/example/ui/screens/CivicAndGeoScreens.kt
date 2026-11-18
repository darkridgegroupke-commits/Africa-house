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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.data.seed.AfricanWorldCatalog
import com.example.domain.FinancialEngines
import com.example.domain.GameClockEngine
import com.example.ui.components.ActionFeedbackBanner
import com.example.ui.components.HoaAdaptivePageContainer
import com.example.ui.components.HoaEmptyStateBanner
import com.example.ui.components.HoaModuleCard
import com.example.ui.components.HoaSectionHeader
import com.example.ui.components.HoaStatusBadge
import com.example.ui.components.KenteFiligreeDivider
import com.example.ui.navigation.HoaEntityRoutes
import com.example.ui.state.ActionFeedback
import com.example.ui.state.CampaignSystemTab
import com.example.ui.state.HoaShellUiState
import com.example.ui.state.SimulationActions
import com.example.ui.state.SimulationSnapshot
import com.example.ui.theme.HoaMonoStyle

@Composable
fun AfricaWorldScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Africa / World Geography",
                subtitle = "Hierarchical countries, states/regions, counties, cities & settlements",
                badgeLabel = "ATLAS READY",
            )
            HoaEmptyStateBanner(
                icon = Icons.Outlined.Public,
                title = "African World Atlas",
                message = "12 seeded sovereign nations, 24 states, and 28 counties connect via immutable /world/{id}, /state/{id}, and /county/{id} routes.",
                phaseNote = "UPDATE 1",
            )
            HoaModuleCard(
                title = "3-Tier Sovereign Hierarchy",
                icon = Icons.Outlined.LocationCity,
                phaseLabel = "Update 1",
                description = "Sign in to load live Firestore countries, treasuries, and settlements.",
                emptyStateTitle = "Canonical IDs",
                emptyStateDetail = "${HoaEntityRoutes.world(101)} • ${HoaEntityRoutes.state(201)} • ${HoaEntityRoutes.county(301)}",
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GovernmentScreen(
    sim: SimulationSnapshot = SimulationSnapshot(),
    feedback: ActionFeedback = ActionFeedback(),
    actions: SimulationActions? = null,
    activeCountryId: Long = 101L,
    activeStateId: Long = 201L,
    activeCountyId: Long = 301L,
    currencyCode: String = "NGN",
    modifier: Modifier = Modifier,
) {
    var selectedLevel by remember { mutableStateOf("NATIONAL") }
    var selectedDept by remember { mutableStateOf("Infrastructure") }
    var spendPurpose by remember { mutableStateOf("County Solar Road & Port Upgrade") }
    var spendAmountUnits by remember { mutableStateOf("5000") }

    val targetGovId = when (selectedLevel) {
        "STATE" -> "gov_state_$activeStateId"
        "COUNTY" -> "gov_county_$activeCountyId"
        else -> "gov_country_$activeCountryId"
    }
    val gov = sim.governments.firstOrNull { it.docId == targetGovId } ?: sim.governments.firstOrNull()
    val treasury = sim.treasuryAccounts.firstOrNull { it.docId == gov?.treasuryAccountId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Government & 14 Statutory Ministries",
                subtitle = "National, State & County administrations with audited treasuries",
                badgeLabel = "UPDATE 9 LIVE",
            )

            ActionFeedbackBanner(feedback = feedback)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("NATIONAL", "STATE", "COUNTY").forEach { tier ->
                    FilterChip(
                        selected = selectedLevel == tier,
                        onClick = { selectedLevel = tier },
                        label = { Text(tier) },
                    )
                }
            }

            if (gov != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${gov.jurisdictionName} (${gov.jurisdictionLevel})", style = MaterialTheme.typography.titleLarge)
                            HoaStatusBadge(label = gov.docId, highlighted = true)
                        }
                        Text("Administration: ${gov.leaderName}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Treasury Balance: ${FinancialEngines.formatCurrency(treasury?.balanceCents ?: 0L, treasury?.currencyCode ?: currencyCode)}",
                            style = HoaMonoStyle,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Tax Policy: Primary ${gov.primaryTaxRatePct}% • Corp ${gov.secondaryTaxRatePct}% • Sales ${gov.salesTaxRatePct}%",
                            style = HoaMonoStyle,
                        )
                        if (actions != null) {
                            OutlinedButton(
                                onClick = { actions.updateGovTaxRates(gov, gov.primaryTaxRatePct, gov.secondaryTaxRatePct, gov.salesTaxRatePct) },
                                enabled = !feedback.isBusy,
                            ) {
                                Text("Confirm & Audit Current Tax Schedule")
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("14 Departments & Audited Public Expenditure", style = MaterialTheme.typography.titleMedium)
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            AfricanWorldCatalog.statutoryDepartments.forEach { dept ->
                                FilterChip(
                                    selected = selectedDept == dept,
                                    onClick = { selectedDept = dept },
                                    label = { Text(dept) },
                                )
                            }
                        }
                        if (actions != null) {
                            OutlinedTextField(
                                value = spendPurpose,
                                onValueChange = { spendPurpose = it },
                                label = { Text("Public Contract / Grant Purpose") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = spendAmountUnits,
                                onValueChange = { spendAmountUnits = it.filter(Char::isDigit) },
                                label = { Text("Disbursement Amount ($currencyCode)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Button(
                                onClick = {
                                    actions.executeGovSpend(
                                        gov = gov,
                                        dept = selectedDept,
                                        purpose = spendPurpose,
                                        amountCents = (spendAmountUnits.toLongOrNull() ?: 1000L) * 100L,
                                    )
                                },
                                enabled = !feedback.isBusy,
                                modifier = Modifier.testTag("gov_disburse_button"),
                            ) {
                                Text("Approve Audited Treasury Expenditure")
                            }
                        }
                    }
                }
            }

            if (sim.governmentExpenditures.isNotEmpty()) {
                KenteFiligreeDivider()
                Text("Recent Public Expenditure Audit Trail", style = MaterialTheme.typography.titleMedium)
                sim.governmentExpenditures.take(6).forEach { exp ->
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                            Text("[${exp.department}] ${exp.purpose}", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${exp.txRef} • ${FinancialEngines.formatCurrency(exp.amountCents, currencyCode)} -> ${exp.recipientName}",
                                style = HoaMonoStyle,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PoliticsScreen(
    uiState: HoaShellUiState,
    onSelectCampaignTab: (CampaignSystemTab) -> Unit,
    sim: SimulationSnapshot = SimulationSnapshot(),
    feedback: ActionFeedback = ActionFeedback(),
    actions: SimulationActions? = null,
    activeCountryId: Long = 101L,
    activeStateId: Long = 201L,
    activeCountyId: Long = 301L,
    currencyCode: String = "NGN",
    modifier: Modifier = Modifier,
) {
    var partyName by remember { mutableStateOf("Pan-African Renewal Front") }
    var partyAbbr by remember { mutableStateOf("PARF") }
    var electionOffice by remember { mutableStateOf("County Governor") }
    var candidatesCsv by remember { mutableStateOf("Amina Okonkwo, Kwame Mensah") }

    val matchingCampaigns = sim.campaigns.filter { it.campaignSystem == uiState.selectedCampaignTab.name }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Politics, Parties & 3 Campaign Systems",
                subtitle = "Strictly segregated State, National & General campaign budgets + secure voting",
                badgeLabel = "UPDATE 10 LIVE",
            )

            ActionFeedbackBanner(feedback = feedback)

            // 3 Segregated Campaign Systems (Update 10)
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
                                text = { Text(tab.title.removeSuffix(" Campaign"), style = MaterialTheme.typography.labelLarge) },
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = uiState.selectedCampaignTab.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Jurisdiction: ${uiState.selectedCampaignTab.scopeLabel} • Strictly segregated budget",
                            style = HoaMonoStyle,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (actions != null) {
                            Button(
                                onClick = {
                                    actions.launchCampaign(
                                        system = uiState.selectedCampaignTab.name,
                                        office = uiState.selectedCampaignTab.scopeLabel,
                                        city = "Capital HQ",
                                        countryId = activeCountryId,
                                        stateId = activeStateId,
                                        currency = currencyCode,
                                        pitch = "Transparent infrastructure, trade, and civic growth.",
                                    )
                                },
                                enabled = !feedback.isBusy,
                                modifier = Modifier.testTag("launch_campaign_button"),
                            ) {
                                Text("Launch ${uiState.selectedCampaignTab.title}")
                            }
                        }
                        matchingCampaigns.forEach { cmp ->
                            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                                Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("${cmp.candidateName} • ${cmp.officeTitle}", style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "Acct: ${cmp.campaignAccountId} • Donations: ${FinancialEngines.formatCurrency(cmp.donationsTotalCents, currencyCode)} • Spent: ${FinancialEngines.formatCurrency(cmp.expensesTotalCents, currencyCode)}",
                                        style = HoaMonoStyle,
                                    )
                                    if (actions != null) {
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedButton(onClick = { actions.donateToCampaign(cmp, 10_000L) }) {
                                                Text("Donate 100.00")
                                            }
                                            OutlinedButton(onClick = { actions.spendCampaignBudget(cmp, activeCountyId, 5_000L, "Town Hall Outreach") }) {
                                                Text("Outreach Event (50.00)")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Political Parties
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Political Parties (${sim.politicalParties.size})", style = MaterialTheme.typography.titleMedium)
                    }
                    if (actions != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = partyName, onValueChange = { partyName = it }, label = { Text("Party Name") }, singleLine = true, modifier = Modifier.weight(2f))
                            OutlinedTextField(value = partyAbbr, onValueChange = { partyAbbr = it }, label = { Text("Abbr") }, singleLine = true, modifier = Modifier.weight(1f))
                        }
                        Button(
                            onClick = { actions.createParty(partyName, partyAbbr, activeCountryId, "Sustainable African industrialization & anti-corruption.") },
                            enabled = !feedback.isBusy,
                            modifier = Modifier.testTag("create_party_button"),
                        ) {
                            Text("Establish Political Party")
                        }
                    }
                    sim.politicalParties.forEach { party ->
                        Text("• ${party.canonicalUrl()} [${party.abbreviation}] ${party.name} — Leader: ${party.leaderName}", style = HoaMonoStyle)
                    }
                }
            }

            // Elections & Secure Ballot Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.HowToVote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Elections & Ballot Security (${sim.elections.size})", style = MaterialTheme.typography.titleMedium)
                    }
                    if (actions != null) {
                        OutlinedTextField(value = electionOffice, onValueChange = { electionOffice = it }, label = { Text("Office Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = candidatesCsv, onValueChange = { candidatesCsv = it }, label = { Text("Candidates (comma-separated)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedButton(
                            onClick = { actions.createElection("COUNTY", activeCountyId, electionOffice, candidatesCsv) },
                            enabled = !feedback.isBusy,
                            modifier = Modifier.testTag("create_election_button"),
                        ) {
                            Text("Open County Election Ballot")
                        }
                    }
                    sim.elections.forEach { el ->
                        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                            Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${el.canonicalUrl()} • ${el.officeTitle}", style = MaterialTheme.typography.titleSmall)
                                    HoaStatusBadge(label = el.electionStatus, highlighted = el.electionStatus == "OPEN")
                                }
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    el.candidates.forEach { cand ->
                                        val count = el.voteCounts[cand] ?: 0L
                                        OutlinedButton(
                                            onClick = { actions?.castVote(el, cand) },
                                            enabled = actions != null && !feedback.isBusy && el.electionStatus == "OPEN",
                                        ) {
                                            Text("Vote $cand ($count)")
                                        }
                                    }
                                    if (actions != null && el.electionStatus == "OPEN") {
                                        OutlinedButton(onClick = { actions.disputeElection(el.electionId, "Audit Recount Requested") }) {
                                            Text("Dispute")
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
fun EconomyScreen(
    sim: SimulationSnapshot = SimulationSnapshot(),
    feedback: ActionFeedback = ActionFeedback(),
    actions: SimulationActions? = null,
    modifier: Modifier = Modifier,
) {
    val activeReferendum = sim.referendums.firstOrNull()
    val tally = activeReferendum?.let {
        FinancialEngines.evaluateReferendum(it.continueVotes, it.partialResetVotes, it.fullResetVotes, it.abstainVotes)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Server Clock, Macro Economy & Era Referendum",
                subtitle = "48h = 1 Yr • Idempotent catch-up • 200-Day / 100-Yr Referendum",
                badgeLabel = "UPDATES 2, 11 & 12",
            )

            ActionFeedbackBanner(feedback = feedback)

            // Server Clock Card (Update 2)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Authoritative Server Game Clock", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(text = sim.formattedCalendarDate, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Tick Index: #${sim.worldClock?.lastProcessedTickIndex ?: 0} • 48h = 1 Yr • 1h = 7.5 Days • 200 Real Days = 100 Yrs",
                        style = HoaMonoStyle,
                    )
                    if (actions != null) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { actions.syncServerClock() },
                                enabled = !feedback.isBusy,
                                modifier = Modifier.testTag("sync_clock_button"),
                            ) {
                                Text("Sync Server Clock & Catch-Up")
                            }
                            OutlinedButton(
                                onClick = { actions.advanceScheduledTick() },
                                enabled = !feedback.isBusy,
                                modifier = Modifier.testTag("advance_tick_button"),
                            ) {
                                Text("Process Scheduled World Tick")
                            }
                        }
                    }
                    sim.tickLogs.take(4).forEach { log ->
                        Text("• Tick #${log.tickIndex} (Day ${log.gameDaysAtTick.toLong()}): ${log.eventsProcessed}", style = HoaMonoStyle)
                    }
                }
            }

            // 200-Day / 100-Year Era Referendum (Update 12)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("200-Day / 100-Year Era Referendum", style = MaterialTheme.typography.titleMedium)
                    }
                    val daysToMilestone = (GameClockEngine.ERA_REFERENDUM_GAME_DAYS - sim.currentGameDay).coerceAtLeast(0.0).toLong()
                    Text(
                        text = "Threshold: >=60% valid votes required for Partial or Full Reset. Legal payment compliance records are always retained separately. Days to 100-Yr Milestone: $daysToMilestone",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (tally != null) {
                        Text(
                            text = "Valid Votes: ${tally.validVotes} (Continue ${tally.continueVotes}, Partial ${tally.partialResetVotes}, Full ${tally.fullResetVotes}, Abstain ${tally.abstainVotes}) • Outcome: ${tally.outcome.name}",
                            style = HoaMonoStyle,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (actions != null) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FinancialEngines.ReferendumChoice.entries.forEach { choice ->
                                OutlinedButton(
                                    onClick = { actions.voteReferendum(sim.worldClock?.eraNumber ?: 1, choice) },
                                    enabled = !feedback.isBusy,
                                    modifier = Modifier.testTag("referendum_vote_${choice.name.lowercase()}"),
                                ) {
                                    Text(choice.name.replace('_', ' '))
                                }
                            }
                        }
                    }
                }
            }

            HoaModuleCard(
                title = "Grouped NPC Economy & Macro Telemetry",
                icon = Icons.Outlined.TrendingUp,
                phaseLabel = "Live",
                description = "NPCs participate in county workforce capacity, marketplace liquidity, and tax generation alongside citizen enterprises.",
                emptyStateTitle = "${sim.companies.size} Citizen Companies • ${sim.tradeHistory.size} Trades Settled • ${sim.governments.size} Jurisdictions",
                emptyStateDetail = "All macroeconomic numbers come from recorded Firestore ledger and trade documents.",
            )
        }
    }
}
