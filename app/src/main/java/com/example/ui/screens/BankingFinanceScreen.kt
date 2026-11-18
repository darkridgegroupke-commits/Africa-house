package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.WorkOutline
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
import com.example.domain.FinancialEngines
import com.example.ui.components.ActionFeedbackBanner
import com.example.ui.components.HoaAdaptivePageContainer
import com.example.ui.components.HoaSectionHeader
import com.example.ui.components.HoaStatusBadge
import com.example.ui.components.KenteFiligreeDivider
import com.example.ui.state.ActionFeedback
import com.example.ui.state.BankAccountCategory
import com.example.ui.state.HoaShellUiState
import com.example.ui.state.SimulationActions
import com.example.ui.state.SimulationSnapshot
import com.example.ui.theme.HoaMonoStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BankingScreen(
    uiState: HoaShellUiState,
    onSelectAccountCategory: (BankAccountCategory) -> Unit,
    sim: SimulationSnapshot = SimulationSnapshot(),
    feedback: ActionFeedback = ActionFeedback(),
    actions: SimulationActions? = null,
    activeCountryId: Long = 101L,
    activeStateId: Long = 201L,
    activeCountyId: Long = 301L,
    modifier: Modifier = Modifier,
) {
    var transferAmountUnits by remember { mutableStateOf("500") }
    var transferNote by remember { mutableStateOf("Capital Allocation") }
    var targetIsCapital by remember { mutableStateOf(true) }
    var loanAmountUnits by remember { mutableStateOf("2000") }
    var propertyTitle by remember { mutableStateOf("Victoria County Apartment") }

    val selectedAcct = when (uiState.selectedBankCategory) {
        BankAccountCategory.PERSONAL -> sim.personalAccount
        BankAccountCategory.CAPITAL -> sim.capitalAccount
        BankAccountCategory.CAMPAIGN -> sim.campaignAccount
    }
    val currency = selectedAcct?.currencyCode ?: "NGN"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HoaAdaptivePageContainer {
            HoaSectionHeader(
                title = "Banking, Salaries & Central Ledger",
                subtitle = "Double-entry accounting • Tax routing • Loans as liabilities",
                badgeLabel = "UPDATES 4, 5 & 11",
            )

            ActionFeedbackBanner(feedback = feedback)

            // Segregated Accounts Card (Update 4)
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
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(text = uiState.selectedBankCategory.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (selectedAcct != null) {
                                "${selectedAcct.docId} • Balance: ${FinancialEngines.formatCurrency(selectedAcct.balanceCents, currency)}"
                            } else {
                                "${uiState.selectedBankCategory.code} • Balance: 0.00 $currency"
                            },
                            style = HoaMonoStyle,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = uiState.selectedBankCategory.ruleSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        if (actions != null && sim.personalAccount != null && sim.capitalAccount != null) {
                            OutlinedTextField(
                                value = transferAmountUnits,
                                onValueChange = { transferAmountUnits = it.filter(Char::isDigit) },
                                label = { Text("Transfer Amount ($currency)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = transferNote,
                                onValueChange = { transferNote = it },
                                label = { Text("Ledger Reference") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                FilterChip(
                                    selected = targetIsCapital,
                                    onClick = { targetIsCapital = true },
                                    label = { Text("Personal -> Capital (0% Tax)") },
                                )
                                FilterChip(
                                    selected = !targetIsCapital,
                                    onClick = { targetIsCapital = false },
                                    label = { Text("Personal -> Campaign") },
                                )
                                Button(
                                    onClick = {
                                        val cents = (transferAmountUnits.toLongOrNull() ?: 0L) * 100L
                                        val sender = selectedAcct?.docId ?: sim.personalAccount!!.docId
                                        val receiver = if (targetIsCapital) sim.capitalAccount!!.docId else (sim.campaignAccount?.docId ?: sim.capitalAccount!!.docId)
                                        actions.transferFunds(sender, receiver, cents, transferNote)
                                    },
                                    enabled = !feedback.isBusy,
                                    modifier = Modifier.testTag("execute_transfer_button"),
                                ) {
                                    Text("Execute Double-Entry Transfer")
                                }
                            }
                        }
                    }
                }
            }

            // Employment & Hourly Salary Engine (Update 5)
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.WorkOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Employment Contracts & Hourly Salary", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = "Salaries deduct National, State, and County taxes directly into each treasury and track unpaid employer wage liabilities cleanly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (actions != null && sim.employmentContracts.isEmpty()) {
                        Button(
                            onClick = {
                                actions.signCivicEmployment(
                                    countryId = activeCountryId,
                                    stateId = activeStateId,
                                    countyId = activeCountyId,
                                    jobTitle = "County Trade & Treasury Analyst",
                                    hourlyWageCents = 25_000L, // 250.00 / hr
                                )
                            },
                            enabled = !feedback.isBusy,
                            modifier = Modifier.testTag("sign_employment_button"),
                        ) {
                            Text("Sign County Civil Service Contract (250.00/hr)")
                        }
                    }
                    sim.employmentContracts.forEach { contract ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(contract.jobTitle, style = MaterialTheme.typography.titleSmall)
                                    HoaStatusBadge(label = contract.contractStatus, highlighted = true)
                                }
                                Text(
                                    text = "${contract.employerName} • Rate: ${FinancialEngines.formatCurrency(contract.hourlyWageCents, currency)}/hr",
                                    style = HoaMonoStyle,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = "YTD Paid: ${FinancialEngines.formatCurrency(contract.totalPaidCents, currency)} • Taxes: ${FinancialEngines.formatCurrency(contract.totalTaxDeductedCents, currency)} • Unpaid Liability: ${FinancialEngines.formatCurrency(contract.unpaidLiabilityCents, currency)}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                if (actions != null) {
                                    Button(
                                        onClick = { actions.collectSalaryCycle(contract, 8) },
                                        enabled = !feedback.isBusy,
                                        modifier = Modifier.testTag("collect_salary_button"),
                                    ) {
                                        Text("Settle 8h Shift Salary + Tax Deductions")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Loans (Liabilities), Treasury Bonds & Property Assets (Update 11)
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Loans (Liabilities), Bonds & Property Deeds", style = MaterialTheme.typography.titleMedium)
                    }
                    if (actions != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = loanAmountUnits,
                                onValueChange = { loanAmountUnits = it.filter(Char::isDigit) },
                                label = { Text("Amount ($currency)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            Button(
                                onClick = {
                                    actions.takePersonalLoan(
                                        countyId = activeCountyId,
                                        principalCents = (loanAmountUnits.toLongOrNull() ?: 1000L) * 100L,
                                        collateral = "Income & County Residence",
                                    )
                                },
                                enabled = !feedback.isBusy,
                                modifier = Modifier.testTag("take_loan_button"),
                            ) {
                                Text("Originate Loan")
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { actions.purchaseTreasuryBond(activeCountryId, 50_000L) },
                                enabled = !feedback.isBusy,
                            ) {
                                Text("Buy 500.00 Treasury Bond (7.25%)")
                            }
                            OutlinedButton(
                                onClick = { actions.buyProperty(activeCountyId, "RESIDENTIAL", propertyTitle, 100_000L, 4_500L) },
                                enabled = !feedback.isBusy,
                            ) {
                                Icon(Icons.Outlined.HomeWork, contentDescription = null)
                                Spacer(Modifier.padding(2.dp))
                                Text("Acquire Property Deed (1,000.00)")
                            }
                        }
                    }

                    sim.loans.forEach { loan ->
                        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Loan Liability (${loan.annualInterestPct}% APR)", style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "Remaining: ${FinancialEngines.formatCurrency(loan.principalRemainingCents, currency)} • Status: ${loan.loanStatus}",
                                        style = HoaMonoStyle,
                                    )
                                }
                                if (actions != null && loan.loanStatus == "ACTIVE") {
                                    OutlinedButton(onClick = { actions.repayLoan(loan, loan.principalRemainingCents) }) {
                                        Text("Repay Full")
                                    }
                                }
                            }
                        }
                    }

                    if (sim.bonds.isNotEmpty() || sim.properties.isNotEmpty()) {
                        Text(
                            text = "Bonds Held: ${sim.bonds.size} • Property Deeds: ${sim.properties.size}",
                            style = HoaMonoStyle,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            KenteFiligreeDivider()

            // Double-Entry Ledger Statements
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Double-Entry Ledger Statements (${sim.ledgerTransactions.size})", style = MaterialTheme.typography.titleMedium)
                    }
                    if (sim.ledgerTransactions.isEmpty()) {
                        Text(
                            text = "No ledger entries yet. Every salary, transfer, tax deduction, loan, and purchase records an immutable debit/credit pair.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        sim.ledgerTransactions.take(12).forEach { tx ->
                            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                                Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${tx.txRef} • ${tx.txType}", style = HoaMonoStyle, color = MaterialTheme.colorScheme.primary)
                                        Text(FinancialEngines.formatCurrency(tx.amountCents, currency), style = MaterialTheme.typography.labelMedium)
                                    }
                                    Text("${tx.senderAccountId} -> ${tx.receiverAccountId}", style = MaterialTheme.typography.bodySmall)
                                    Text(tx.reference, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
