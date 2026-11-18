package com.example.domain

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * Pure deterministic financial, corporate, production, TSN market, and referendum engines.
 * Monetary amounts are always stored in integer cents (100 cents = 1.00 currency unit)
 * to guarantee double-entry conservation of funds with zero floating-point drift.
 */
object FinancialEngines {

    const val INITIAL_COMPANY_SHARES: Long = 1_000_000L
    const val INITIAL_SHARE_PRICE_CENTS: Long = 1_000L // 10.00 currency units
    const val INITIAL_COMPANY_VALUATION_CENTS: Long = INITIAL_COMPANY_SHARES * INITIAL_SHARE_PRICE_CENTS // 10,000,000.00
    const val CEO_MIN_OWNERSHIP_PCT: Double = 5.0
    const val REFERENDUM_PASS_THRESHOLD_PCT: Double = 60.0

    /**
     * Explicit game configuration for new citizen Main character onboarding.
     * Bounded, non-unlimited, and strictly awarded once per UID via idempotency lock.
     * ALT characters never receive additional onboarding grants.
     */
    const val INITIAL_PERSONAL_BALANCE_CENTS: Long = 25_000L // 250.00 personal stipend
    const val INITIAL_CAPITAL_BALANCE_CENTS: Long = 100_000L // 1,000.00 seed investment capital


    fun formatCurrency(cents: Long, currencyCode: String = "HOA"): String {
        val sign = if (cents < 0L) "-" else ""
        val absCents = kotlin.math.abs(cents)
        val whole = absCents / 100L
        val frac = (absCents % 100L).toString().padStart(2, '0')
        val formattedWhole = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(whole)
        return "$sign$formattedWhole.$frac $currencyCode"
    }

    // -------------------------------------------------------------------------
    // Update 4 & 5: Double-Entry Banking & Multi-Tier Tax Routing
    // -------------------------------------------------------------------------

    enum class AccountCategory {
        CITIZEN, CAMPAIGN, CORPORATE, TREASURY
    }

    enum class AccountType {
        PERSONAL, CAPITAL, CAMPAIGN, CORPORATE, NATIONAL_TREASURY, STATE_TREASURY, COUNTY_TREASURY, MARKET_ESCROW
    }

    sealed class TransferValidation {
        data object Valid : TransferValidation()
        data class Invalid(val reason: String) : TransferValidation()
    }

    fun validateTransfer(
        senderAccountId: String,
        receiverAccountId: String,
        senderCategory: AccountCategory,
        senderType: AccountType,
        receiverType: AccountType,
        senderBalanceCents: Long,
        amountCents: Long,
    ): TransferValidation {
        if (senderAccountId == receiverAccountId) {
            return TransferValidation.Invalid("Sender and receiver accounts must differ.")
        }
        if (amountCents <= 0L) {
            return TransferValidation.Invalid("Transfer amount must be positive.")
        }
        if (senderBalanceCents < amountCents) {
            return TransferValidation.Invalid("Insufficient account balance for atomic debit.")
        }
        // Campaign funds can NEVER be converted into personal funds
        if (senderCategory == AccountCategory.CAMPAIGN && receiverType == AccountType.PERSONAL) {
            return TransferValidation.Invalid(
                "Campaign funds are ring-fenced and cannot be converted into personal funds.",
            )
        }
        return TransferValidation.Valid
    }

    data class MultiTierTaxBreakdown(
        val grossCents: Long,
        val nationalTaxCents: Long,
        val stateTaxCents: Long,
        val countyTaxCents: Long,
        val netPayoutCents: Long,
    ) {
        val totalTaxCents: Long get() = nationalTaxCents + stateTaxCents + countyTaxCents
    }

    /**
     * Computes exact tax routing across National, State, and County treasuries.
     * Transfers between a citizen's own PERSONAL <-> CAPITAL accounts are strictly tax-exempt (0 tax)
     * to prevent double taxation.
     */
    fun computeTaxBreakdown(
        grossAmountCents: Long,
        nationalRatePct: Double,
        stateRatePct: Double,
        countyRatePct: Double,
        isOwnAccountTransfer: Boolean = false,
    ): MultiTierTaxBreakdown {
        if (grossAmountCents <= 0L || isOwnAccountTransfer) {
            return MultiTierTaxBreakdown(
                grossCents = grossAmountCents.coerceAtLeast(0L),
                nationalTaxCents = 0L,
                stateTaxCents = 0L,
                countyTaxCents = 0L,
                netPayoutCents = grossAmountCents.coerceAtLeast(0L),
            )
        }
        val safeNational = nationalRatePct.coerceIn(0.0, 60.0)
        val safeState = stateRatePct.coerceIn(0.0, 30.0)
        val safeCounty = countyRatePct.coerceIn(0.0, 20.0)

        val natTax = floor(grossAmountCents * (safeNational / 100.0)).toLong()
        val stateTax = floor(grossAmountCents * (safeState / 100.0)).toLong()
        val countyTax = floor(grossAmountCents * (safeCounty / 100.0)).toLong()
        val totalTax = (natTax + stateTax + countyTax).coerceAtMost(grossAmountCents)
        return MultiTierTaxBreakdown(
            grossCents = grossAmountCents,
            nationalTaxCents = natTax,
            stateTaxCents = stateTax,
            countyTaxCents = countyTax,
            netPayoutCents = grossAmountCents - totalTax,
        )
    }

    // -------------------------------------------------------------------------
    // Update 6: Corporate Equity, CEO 5% Rule, 2-for-1 Share Splits & Issuance
    // -------------------------------------------------------------------------

    fun ownershipPercent(sharesHeld: Long, totalShares: Long): Double {
        if (totalShares <= 0L || sharesHeld <= 0L) return 0.0
        return (sharesHeld.toDouble() / totalShares.toDouble()) * 100.0
    }

    fun meetsCeoMinimumOwnership(sharesHeld: Long, totalShares: Long): Boolean =
        ownershipPercent(sharesHeld, totalShares) + 1e-9 >= CEO_MIN_OWNERSHIP_PCT

    fun minimumSharesForCeo(totalShares: Long): Long =
        kotlin.math.ceil(totalShares * (CEO_MIN_OWNERSHIP_PCT / 100.0)).toLong()

    data class SplitResult(
        val newTotalShares: Long,
        val newSharePriceCents: Long,
        val preservedValuationCents: Long,
        val updatedHoldings: Map<String, Long>,
    )

    /**
     * Executes a 2-for-1 share split: doubles share counts, halves price per share,
     * preserves total equity valuation and exact ownership percentages without creating cash.
     */
    fun computeTwoForOneSplit(
        holdingsByEntity: Map<String, Long>,
        currentTotalShares: Long,
        currentSharePriceCents: Long,
    ): SplitResult {
        require(currentTotalShares > 0L) { "Company must have positive outstanding shares." }
        require(currentSharePriceCents >= 2L) { "Share price too low for a 2-for-1 split." }
        val newTotal = currentTotalShares * 2L
        val newPrice = currentSharePriceCents / 2L
        val doubledHoldings = holdingsByEntity.mapValues { (_, qty) -> qty * 2L }
        return SplitResult(
            newTotalShares = newTotal,
            newSharePriceCents = newPrice,
            preservedValuationCents = newTotal * newPrice,
            updatedHoldings = doubledHoldings,
        )
    }

    data class IssuanceResult(
        val newTotalShares: Long,
        val capitalRaisedCents: Long,
    )

    fun computeNewShareIssuance(
        currentTotalShares: Long,
        newSharesIssued: Long,
        issuePriceCents: Long,
    ): IssuanceResult {
        require(newSharesIssued > 0L) { "New shares issued must be positive." }
        require(issuePriceCents > 0L) { "Issue price must be positive." }
        return IssuanceResult(
            newTotalShares = currentTotalShares + newSharesIssued,
            capitalRaisedCents = newSharesIssued * issuePriceCents,
        )
    }

    // -------------------------------------------------------------------------
    // Update 7: Facility Production & Accounting Formula
    // -------------------------------------------------------------------------

    data class ProductionOutput(
        val producedUnits: Long,
        val consumedInputUnits: Long,
        val operatingCostCents: Long,
        val workforceEfficiency: Double,
        val inputAvailability: Double,
    )

    /**
     * Production = Building Capacity * Workforce Efficiency * Input Availability.
     * Ordinary facility employees are NPCs only, strictly capped by [maxWorkforce].
     * Unsold goods stay in inventory and never count as sales revenue until sold.
     */
    fun computeFacilityProduction(
        buildingCapacity: Long,
        npcEmployees: Int,
        maxWorkforce: Int,
        requiredInputsPerCycle: Long,
        availableInputInventory: Long,
        npcWagePerCycleCents: Long,
        maintenancePerCycleCents: Long,
    ): ProductionOutput {
        require(npcEmployees in 0..maxWorkforce) {
            "NPC workforce ($npcEmployees) exceeds facility capacity ($maxWorkforce)."
        }
        val workforceEff = if (maxWorkforce <= 0) 0.0 else (npcEmployees.toDouble() / maxWorkforce.toDouble()).coerceIn(0.0, 1.0)
        val inputAvail = if (requiredInputsPerCycle <= 0L) {
            1.0
        } else {
            (availableInputInventory.toDouble() / requiredInputsPerCycle.toDouble()).coerceIn(0.0, 1.0)
        }
        val produced = if (buildingCapacity <= 0L || npcEmployees <= 0) {
            0L
        } else {
            floor(buildingCapacity.toDouble() * workforceEff * inputAvail).toLong()
        }
        val consumedInputs = if (requiredInputsPerCycle <= 0L || produced == 0L) {
            0L
        } else {
            floor(requiredInputsPerCycle.toDouble() * inputAvail).toLong().coerceAtMost(availableInputInventory)
        }
        val operatingCost = (npcEmployees.toLong() * npcWagePerCycleCents.coerceAtLeast(0L)) +
            maintenancePerCycleCents.coerceAtLeast(0L)

        return ProductionOutput(
            producedUnits = produced,
            consumedInputUnits = consumedInputs,
            operatingCostCents = operatingCost,
            workforceEfficiency = workforceEff,
            inputAvailability = inputAvail,
        )
    }

    // -------------------------------------------------------------------------
    // Update 8: TSN County Marketplace Dynamic Pricing & Anti-Wash Validation
    // -------------------------------------------------------------------------

    fun validateMarketplaceTrade(
        buyerEntityId: String,
        sellerEntityId: String,
        buyerOwnerUid: String,
        sellerOwnerUid: String,
        orderPriceCents: Long,
        referencePriceCents: Long,
        quantity: Long,
    ): TransferValidation {
        if (quantity <= 0L || orderPriceCents <= 0L) {
            return TransferValidation.Invalid("Order quantity and unit price must be positive.")
        }
        if (buyerEntityId == sellerEntityId) {
            return TransferValidation.Invalid("Circular self-trading between the same entity is blocked.")
        }
        if (buyerOwnerUid == sellerOwnerUid && buyerOwnerUid.isNotBlank()) {
            return TransferValidation.Invalid("Wash trading between accounts owned by the same user is blocked.")
        }
        if (referencePriceCents > 0L) {
            val minAllowed = (referencePriceCents * 0.25).roundToLong().coerceAtLeast(1L)
            val maxAllowed = (referencePriceCents * 3.0).roundToLong()
            if (orderPriceCents < minAllowed || orderPriceCents > maxAllowed) {
                return TransferValidation.Invalid(
                    "Price manipulation guard: unit price must stay within 25%–300% of county index ($referencePriceCents cents).",
                )
            }
        }
        return TransferValidation.Valid
    }

    fun computeDynamicCountyPriceCents(
        basePriceCents: Long,
        countyDemandUnits: Long,
        countySupplyUnits: Long,
    ): Long {
        if (basePriceCents <= 0L) return 100L
        val totalVolume = max(10L, countyDemandUnits + countySupplyUnits)
        val imbalanceRatio = (countyDemandUnits - countySupplyUnits).toDouble() / totalVolume.toDouble()
        val multiplier = (1.0 + 0.35 * imbalanceRatio).coerceIn(0.45, 2.25)
        return (basePriceCents * multiplier).roundToLong().coerceAtLeast(10L)
    }

    // -------------------------------------------------------------------------
    // Update 12: 200-Day / 100-Year Era Referendum Evaluation
    // -------------------------------------------------------------------------

    enum class ReferendumChoice {
        CONTINUE_ERA,
        PARTIAL_RESET,
        FULL_RESET,
        ABSTAIN,
    }

    data class ReferendumTally(
        val continueVotes: Long,
        val partialResetVotes: Long,
        val fullResetVotes: Long,
        val abstainVotes: Long,
        val validVotes: Long,
        val outcome: ReferendumChoice,
        val winningPercentage: Double,
    )

    fun evaluateReferendum(
        continueVotes: Long,
        partialResetVotes: Long,
        fullResetVotes: Long,
        abstainVotes: Long,
    ): ReferendumTally {
        val valid = (continueVotes + partialResetVotes + fullResetVotes).coerceAtLeast(0L)
        if (valid == 0L) {
            return ReferendumTally(0L, 0L, 0L, abstainVotes, 0L, ReferendumChoice.CONTINUE_ERA, 0.0)
        }
        val partialPct = (partialResetVotes.toDouble() / valid.toDouble()) * 100.0
        val fullPct = (fullResetVotes.toDouble() / valid.toDouble()) * 100.0
        val continuePct = (continueVotes.toDouble() / valid.toDouble()) * 100.0

        val (outcome, pct) = when {
            fullPct >= REFERENDUM_PASS_THRESHOLD_PCT -> ReferendumChoice.FULL_RESET to fullPct
            partialPct >= REFERENDUM_PASS_THRESHOLD_PCT -> ReferendumChoice.PARTIAL_RESET to partialPct
            else -> ReferendumChoice.CONTINUE_ERA to continuePct
        }
        return ReferendumTally(
            continueVotes = continueVotes,
            partialResetVotes = partialResetVotes,
            fullResetVotes = fullResetVotes,
            abstainVotes = abstainVotes,
            validVotes = valid,
            outcome = outcome,
            winningPercentage = pct,
        )
    }
}
