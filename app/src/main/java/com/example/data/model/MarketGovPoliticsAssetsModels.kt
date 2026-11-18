package com.example.data.model

import androidx.annotation.Keep
import com.example.ui.navigation.HoaEntityRoutes
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class MarketOrder(
    @DocumentId val docId: String = "",
    val scope: String = "TSN_MARKET",
    val ownerUid: String = "",
    val entityId: String = "",
    val entityName: String = "",
    val entityBankAccountId: String = "",
    val countyId: Long = 301L,
    val commodityName: String = "",
    val orderSide: String = "SELL", // SELL, BUY, AUCTION
    val quantity: Long = 0L,
    val remainingQuantity: Long = 0L,
    val pricePerUnitCents: Long = 0L,
    val escrowAccountId: String = "",
    val orderStatus: String = "OPEN", // OPEN, PARTIAL, FILLED, CANCELLED
    val auctionListing: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class InventoryItem(
    @DocumentId val docId: String = "",
    val ownerUid: String = "",
    val holderCategory: String = "PERSONAL", // PERSONAL or COMPANY
    val holderEntityId: String = "",
    val countyId: Long = 301L,
    val commodityName: String = "",
    val quantity: Long = 0L,
    val averageUnitCostCents: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class TradeExecution(
    @DocumentId val docId: String = "",
    val scope: String = "TSN_TRADE",
    val orderId: String = "",
    val countyId: Long = 301L,
    val commodityName: String = "",
    val buyerEntityId: String = "",
    val sellerEntityId: String = "",
    val quantity: Long = 0L,
    val unitPriceCents: Long = 0L,
    val totalValueCents: Long = 0L,
    val salesTaxCents: Long = 0L,
    val txRef: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class GovernmentRecord(
    @DocumentId val docId: String = "",
    val scope: String = "GOVERNMENT",
    val jurisdictionLevel: String = "NATIONAL", // NATIONAL, STATE, COUNTY
    val jurisdictionId: Long = 101L,
    val jurisdictionName: String = "",
    val leaderUid: String = "CARETAKER_NPC",
    val leaderName: String = "Caretaker Civil Administration",
    val treasuryAccountId: String = "",
    val primaryTaxRatePct: Double = 18.0,
    val secondaryTaxRatePct: Double = 25.0,
    val salesTaxRatePct: Double = 7.5,
    val budgetInfrastructureCents: Long = 0L,
    val publicDebtCents: Long = 0L,
    val departments: List<String> = emptyList(),
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class GovernmentExpenditure(
    @DocumentId val docId: String = "",
    val scope: String = "GOV_SPEND",
    val govId: String = "",
    val approvedByUid: String = "",
    val approvedByName: String = "",
    val department: String = "Infrastructure",
    val recipientName: String = "",
    val purpose: String = "",
    val amountCents: Long = 0L,
    val txRef: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class PoliticalParty(
    @DocumentId val docId: String = "",
    val scope: String = "PARTY",
    val partyId: Long = 601L,
    val leaderUid: String = "",
    val leaderName: String = "",
    val countryId: Long = 101L,
    val name: String = "",
    val abbreviation: String = "",
    val manifestoSummary: String = "",
    val emblemStyle: String = "GOLD_STAR",
    val memberCount: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
) {
    fun canonicalUrl(): String = HoaEntityRoutes.party(partyId)
}

@Keep
@IgnoreExtraProperties
data class CampaignRecord(
    @DocumentId val docId: String = "",
    val scope: String = "CAMPAIGN",
    val ownerUid: String = "",
    val candidateName: String = "",
    val campaignSystem: String = "STATE", // STATE, NATIONAL, GENERAL
    val countryId: Long = 101L,
    val stateId: Long = 201L,
    val officeTitle: String = "",
    val headquartersCity: String = "",
    val campaignAccountId: String = "",
    val donationsTotalCents: Long = 0L,
    val expensesTotalCents: Long = 0L,
    val outreachEventsCount: Int = 0,
    val manifestoPitch: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ElectionRecord(
    @DocumentId val docId: String = "",
    val scope: String = "ELECTION",
    val electionId: Long = 801L,
    val electionTier: String = "COUNTY", // COUNTY, STATE, NATIONAL
    val jurisdictionId: Long = 301L,
    val officeTitle: String = "",
    val electionStatus: String = "OPEN", // OPEN, CLOSED, DISPUTED
    val candidates: List<String> = emptyList(),
    val voteCounts: Map<String, Long> = emptyMap(),
    val disputeNotes: String = "",
    val closesAtGameDay: Double = 360.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun canonicalUrl(): String = HoaEntityRoutes.election(electionId)
}

@Keep
@IgnoreExtraProperties
data class VoteBallot(
    @DocumentId val docId: String = "",
    val voterUid: String = "",
    val electionId: Long = 801L,
    val candidateChoice: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class PropertyAsset(
    @DocumentId val docId: String = "",
    val ownerUid: String = "",
    val ownerEntityId: String = "",
    val countyId: Long = 301L,
    val propertyType: String = "RESIDENTIAL", // LAND, RESIDENTIAL, COMMERCIAL, VEHICLE
    val title: String = "",
    val valueCents: Long = 0L,
    val monthlyLeaseCents: Long = 0L,
    val maintenanceCostCents: Long = 0L,
    val propertyTaxPaidCents: Long = 0L,
    val forSale: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class LoanRecord(
    @DocumentId val docId: String = "",
    val ownerUid: String = "",
    val borrowerEntityId: String = "",
    val borrowerAccountId: String = "",
    val lenderAccountId: String = "",
    val borrowerCategory: String = "PERSONAL", // PERSONAL or COMPANY
    val principalCents: Long = 0L,
    val principalRemainingCents: Long = 0L,
    val annualInterestPct: Double = 8.0,
    val collateralSummary: String = "",
    val loanStatus: String = "ACTIVE", // ACTIVE, REPAID, DEFAULTED
    val maturityGameDay: Double = 720.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class BondRecord(
    @DocumentId val docId: String = "",
    val scope: String = "BOND",
    val issuerType: String = "GOVERNMENT", // GOVERNMENT or CORPORATE
    val issuerName: String = "",
    val issuerAccountId: String = "",
    val holderUid: String = "",
    val faceValueCents: Long = 0L,
    val couponRatePct: Double = 6.5,
    val maturityGameDay: Double = 1080.0,
    val bondStatus: String = "ISSUED",
    @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class AuditLogRecord(
    @DocumentId val docId: String = "",
    val actorUid: String = "",
    val actionType: String = "",
    val entityRef: String = "",
    val detail: String = "",
    val gameDay: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ReferendumRecord(
    @DocumentId val docId: String = "",
    val scope: String = "REFERENDUM",
    val eraNumber: Int = 1,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED
    val continueVotes: Long = 0L,
    val partialResetVotes: Long = 0L,
    val fullResetVotes: Long = 0L,
    val abstainVotes: Long = 0L,
    val partialResetModules: List<String> = emptyList(),
    val outcome: String = "CONTINUE_ERA",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ReferendumVote(
    @DocumentId val docId: String = "",
    val voterUid: String = "",
    val eraNumber: Int = 1,
    val choice: String = "CONTINUE_ERA",
    @ServerTimestamp val createdAt: Timestamp? = null,
)
