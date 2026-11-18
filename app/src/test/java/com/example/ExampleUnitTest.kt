package com.example

import com.example.data.model.PlayerCharacter
import com.example.data.seed.AfricanWorldCatalog
import com.example.data.seed.SectorFacilityCatalog
import com.example.domain.FinancialEngines
import com.example.domain.GameClockEngine
import com.example.ui.navigation.HoaEntityRoutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun update1_world_geography_catalog_has_unique_ids_and_clean_routes() {
        assertEquals(12, AfricanWorldCatalog.countries.size)
        assertEquals(24, AfricanWorldCatalog.states.size)
        assertEquals(28, AfricanWorldCatalog.counties.size)
        assertEquals(14, AfricanWorldCatalog.statutoryDepartments.size)

        val countryIds = AfricanWorldCatalog.countries.map { it.countryId }.toSet()
        val stateIds = AfricanWorldCatalog.states.map { it.stateId }.toSet()
        val countyIds = AfricanWorldCatalog.counties.map { it.countyId }.toSet()

        assertEquals(12, countryIds.size)
        assertEquals(24, stateIds.size)
        assertEquals(28, countyIds.size)
        assertTrue(AfricanWorldCatalog.states.all { it.countryId in countryIds })
        assertTrue(AfricanWorldCatalog.counties.all { it.stateId in stateIds && it.countryId in countryIds })

        assertEquals("/world/101", HoaEntityRoutes.world(101L))
        assertEquals("/state/201", HoaEntityRoutes.state(201L))
        assertEquals("/county/301", HoaEntityRoutes.county(301L))
    }

    @Test
    fun update2_server_clock_time_conversions_and_idempotent_catchup() {
        assertEquals(360.0, GameClockEngine.realHoursToGameDays(48.0), 1e-6)
        assertEquals(180.0, GameClockEngine.realHoursToGameDays(24.0), 1e-6)
        assertEquals(90.0, GameClockEngine.realHoursToGameDays(12.0), 1e-6)
        assertEquals(7.5, GameClockEngine.realHoursToGameDays(1.0), 1e-6)
        assertEquals(20.0, GameClockEngine.gameYearsToRealDays(10.0), 1e-6)
        assertEquals(200.0, GameClockEngine.gameYearsToRealDays(100.0), 1e-6)

        val cal = GameClockEngine.toGameCalendarDate(eraNumber = 1, totalGameDays = 360.0)
        assertEquals(2, cal.year)
        assertEquals(1, cal.month)
        assertEquals(1, cal.day)

        val refDate = GameClockEngine.toGameCalendarDate(eraNumber = 1, totalGameDays = 36_000.0)
        assertTrue(refDate.isReferendumMilestoneReached())

        // Offline recovery catch-up window across 48 real hours = 360 ticks (capped cleanly per batch)
        val batch1 = GameClockEngine.computeCatchUpWindow(
            lastProcessedTickIndex = 0L,
            eraStartMillis = 0L,
            serverNowMillis = 48L * 3_600_000L,
        )
        assertEquals(360, batch1.ticksToProcess)
        assertEquals(360L, batch1.targetTickIndex)

        // Running again at same server time processes 0 duplicate ticks
        val duplicateCheck = GameClockEngine.computeCatchUpWindow(
            lastProcessedTickIndex = batch1.targetTickIndex,
            eraStartMillis = 0L,
            serverNowMillis = 48L * 3_600_000L,
        )
        assertFalse(duplicateCheck.hasPendingTicks)
        assertEquals(0, duplicateCheck.ticksToProcess)

        // 10-year permit expiry = 3,600 game days later
        assertEquals(3610.0, GameClockEngine.permitExpiryGameDay(10.0), 1e-6)
    }

    @Test
    fun update3_identity_status_resolution_has_no_xp_and_resolves_deleted_users() {
        val active = PlayerCharacter(playerId = 12453L, fullName = "Amina Okonkwo", accountStatus = "ACTIVE")
        val deleted = PlayerCharacter(playerId = 12454L, fullName = "Removed Citizen", accountStatus = "DELETED")
        val suspended = PlayerCharacter(playerId = 12455L, fullName = "Suspended", accountStatus = "SUSPENDED")
        val blank = PlayerCharacter(playerId = 12456L, fullName = "", accountStatus = "ACTIVE")

        assertEquals("Amina Okonkwo", active.resolvePublicDisplayName())
        assertEquals("User Deleted", deleted.resolvePublicDisplayName())
        assertEquals("Account Suspended", suspended.resolvePublicDisplayName())
        assertEquals("Player Not Found", blank.resolvePublicDisplayName())
    }

    @Test
    fun updates4_and_5_banking_blocks_campaign_to_personal_and_own_transfers_are_zero_tax() {
        val blocked = FinancialEngines.validateTransfer(
            senderAccountId = "acct_campaign",
            receiverAccountId = "acct_personal",
            senderCategory = FinancialEngines.AccountCategory.CAMPAIGN,
            senderType = FinancialEngines.AccountType.CAMPAIGN,
            receiverType = FinancialEngines.AccountType.PERSONAL,
            senderBalanceCents = 50_000L,
            amountCents = 10_000L,
        )
        assertTrue(blocked is FinancialEngines.TransferValidation.Invalid)

        val ownTransferTax = FinancialEngines.computeTaxBreakdown(
            grossAmountCents = 100_000L,
            nationalRatePct = 18.0,
            stateRatePct = 4.5,
            countyRatePct = 2.0,
            isOwnAccountTransfer = true,
        )
        assertEquals(0L, ownTransferTax.totalTaxCents)
        assertEquals(100_000L, ownTransferTax.netPayoutCents)

        val salaryTax = FinancialEngines.computeTaxBreakdown(
            grossAmountCents = 100_000L,
            nationalRatePct = 18.0,
            stateRatePct = 4.5,
            countyRatePct = 2.0,
            isOwnAccountTransfer = false,
        )
        assertEquals(18_000L, salaryTax.nationalTaxCents)
        assertEquals(4_500L, salaryTax.stateTaxCents)
        assertEquals(2_000L, salaryTax.countyTaxCents)
        assertEquals(24_500L, salaryTax.totalTaxCents)
        assertEquals(75_500L, salaryTax.netPayoutCents)
    }

    @Test
    fun updates6_and_7_company_shares_splits_ceo_min_and_facility_production() {
        assertEquals(1_000_000L, FinancialEngines.INITIAL_COMPANY_SHARES)
        assertEquals(1_000L, FinancialEngines.INITIAL_SHARE_PRICE_CENTS)
        assertEquals(1_000_000_000L, FinancialEngines.INITIAL_COMPANY_VALUATION_CENTS)

        assertTrue(FinancialEngines.meetsCeoMinimumOwnership(50_000L, 1_000_000L))
        assertFalse(FinancialEngines.meetsCeoMinimumOwnership(49_999L, 1_000_000L))

        val split = FinancialEngines.computeTwoForOneSplit(
            holdingsByEntity = mapOf("founder" to 600_000L, "treasury" to 400_000L),
            currentTotalShares = 1_000_000L,
            currentSharePriceCents = 1_000L,
        )
        assertEquals(2_000_000L, split.newTotalShares)
        assertEquals(500L, split.newSharePriceCents)
        assertEquals(1_000_000_000L, split.preservedValuationCents)
        assertEquals(1_200_000L, split.updatedHoldings["founder"])

        val prod = FinancialEngines.computeFacilityProduction(
            buildingCapacity = 100L,
            npcEmployees = 10,
            maxWorkforce = 20,
            requiredInputsPerCycle = 20L,
            availableInputInventory = 10L,
            npcWagePerCycleCents = 500L,
            maintenancePerCycleCents = 1_000L,
        )
        // 100 * 0.5 workforceEff * 0.5 inputAvail = 25 units
        assertEquals(25L, prod.producedUnits)
        assertEquals(10L, prod.consumedInputUnits)
        assertEquals(6_000L, prod.operatingCostCents)

        assertTrue(SectorFacilityCatalog.facilities.isNotEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun update7_overstaffing_npc_beyond_max_workforce_throws() {
        FinancialEngines.computeFacilityProduction(
            buildingCapacity = 100L,
            npcEmployees = 25,
            maxWorkforce = 20,
            requiredInputsPerCycle = 0L,
            availableInputInventory = 0L,
            npcWagePerCycleCents = 500L,
            maintenancePerCycleCents = 1_000L,
        )
    }

    @Test
    fun updates8_and_12_tsn_anti_wash_and_referendum_60_percent_threshold() {
        val sameEntity = FinancialEngines.validateMarketplaceTrade(
            buyerEntityId = "company_501",
            sellerEntityId = "company_501",
            buyerOwnerUid = "u1",
            sellerOwnerUid = "u2",
            orderPriceCents = 1_000L,
            referencePriceCents = 1_000L,
            quantity = 5L,
        )
        assertTrue(sameEntity is FinancialEngines.TransferValidation.Invalid)

        val sameOwnerWash = FinancialEngines.validateMarketplaceTrade(
            buyerEntityId = "user_u1",
            sellerEntityId = "company_501",
            buyerOwnerUid = "u1",
            sellerOwnerUid = "u1",
            orderPriceCents = 1_000L,
            referencePriceCents = 1_000L,
            quantity = 5L,
        )
        assertTrue(sameOwnerWash is FinancialEngines.TransferValidation.Invalid)

        val priceManipulation = FinancialEngines.validateMarketplaceTrade(
            buyerEntityId = "user_u2",
            sellerEntityId = "company_501",
            buyerOwnerUid = "u2",
            sellerOwnerUid = "u1",
            orderPriceCents = 50_000L,
            referencePriceCents = 1_000L,
            quantity = 5L,
        )
        assertTrue(priceManipulation is FinancialEngines.TransferValidation.Invalid)

        val highDemand = FinancialEngines.computeDynamicCountyPriceCents(1_000L, countyDemandUnits = 90L, countySupplyUnits = 10L)
        val highSupply = FinancialEngines.computeDynamicCountyPriceCents(1_000L, countyDemandUnits = 10L, countySupplyUnits = 90L)
        assertTrue(highDemand > 1_000L && highSupply < 1_000L)

        val below60 = FinancialEngines.evaluateReferendum(
            continueVotes = 41L,
            partialResetVotes = 59L,
            fullResetVotes = 0L,
            abstainVotes = 20L,
        )
        assertEquals(FinancialEngines.ReferendumChoice.CONTINUE_ERA, below60.outcome)

        val passes60 = FinancialEngines.evaluateReferendum(
            continueVotes = 40L,
            partialResetVotes = 0L,
            fullResetVotes = 60L,
            abstainVotes = 50L,
        )
        assertEquals(FinancialEngines.ReferendumChoice.FULL_RESET, passes60.outcome)
    }
}
