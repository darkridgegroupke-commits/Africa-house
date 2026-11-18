package com.example.data.model

import androidx.annotation.Keep
import com.example.ui.navigation.HoaEntityRoutes
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class BankAccount(
    @DocumentId val docId: String = "",
    val ownerUid: String = "",
    val ownerEntityId: String = "",
    val accountType: String = "PERSONAL", // PERSONAL, CAPITAL, CAMPAIGN, CORPORATE, NATIONAL_TREASURY, STATE_TREASURY, COUNTY_TREASURY, MARKET_ESCROW
    val accountCategory: String = "CITIZEN", // CITIZEN, CAMPAIGN, CORPORATE, TREASURY
    val currencyCode: String = "NGN",
    val balanceCents: Long = 0L,
    val restricted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class LedgerTransaction(
    @DocumentId val docId: String = "",
    val txRef: String = "",
    val initiatedByUid: String = "",
    val senderAccountId: String = "",
    val receiverAccountId: String = "",
    val amountCents: Long = 0L,
    val taxAmountCents: Long = 0L,
    val taxTreasuryAccountId: String = "",
    val txType: String = "TRANSFER",
    val gameDay: Double = 0.0,
    val reference: String = "",
    val status: String = "SETTLED",
    val publicAudit: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class EmploymentContract(
    @DocumentId val docId: String = "",
    val ownerUid: String = "",
    val scope: String = "EMPLOYMENT",
    val playerId: Long = 0L,
    val employerEntityId: String = "", // e.g., gov_county_301 or company_501
    val employerName: String = "",
    val employerAccountId: String = "",
    val jobTitle: String = "",
    val hourlyWageCents: Long = 0L,
    val countryId: Long = 101L,
    val stateId: Long = 201L,
    val countyId: Long = 301L,
    val totalPaidCents: Long = 0L,
    val totalTaxDeductedCents: Long = 0L,
    val unpaidLiabilityCents: Long = 0L,
    val lastPaidGameDay: Double = 0.0,
    val contractStatus: String = "ACTIVE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class CompanyRecord(
    @DocumentId val docId: String = "",
    val scope: String = "COMPANY",
    val companyId: Long = 0L,
    val ownerUid: String = "",
    val ceoUid: String = "",
    val ceoPlayerId: Long = 0L,
    val ceoName: String = "",
    val name: String = "",
    val sector: String = "Farming",
    val ownershipType: String = "PRIVATE", // PRIVATE, PUBLIC, GOVERNMENT_OWNED
    val countryId: Long = 101L,
    val stateId: Long = 201L,
    val countyId: Long = 301L,
    val currencyCode: String = "NGN",
    val corporateAccountId: String = "",
    val registrationCertNumber: String = "",
    val totalShares: Long = 1_000_000L,
    val sharePriceCents: Long = 1_000L,
    val listedPublic: Boolean = false,
    val revenueYtdCents: Long = 0L,
    val operatingCostYtdCents: Long = 0L,
    val corporateTaxPaidCents: Long = 0L,
    val registeredAtGameDay: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun canonicalUrl(): String = HoaEntityRoutes.company(companyId)
    fun valuationCents(): Long = totalShares * sharePriceCents
    fun netProfitYtdCents(): Long = revenueYtdCents - operatingCostYtdCents - corporateTaxPaidCents
}

@Keep
@IgnoreExtraProperties
data class ShareholderHolding(
    @DocumentId val docId: String = "",
    val companyId: Long = 0L,
    val holderEntityId: String = "",
    val holderUid: String = "",
    val holderName: String = "",
    val sharesHeld: Long = 0L,
    val ceoEligible: Boolean = false,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class CompanyFacility(
    @DocumentId val docId: String = "",
    val ownerUid: String = "",
    val companyId: Long = 0L,
    val countyId: Long = 301L,
    val specId: String = "",
    val facilityName: String = "",
    val sector: String = "",
    val constructionCostCents: Long = 0L,
    val buildingCapacity: Long = 0L,
    val npcEmployees: Int = 0,
    val maxWorkforce: Int = 0,
    val npcWagePerDayCents: Long = 0L,
    val maintenancePerDayCents: Long = 0L,
    val requiredInputGood: String = "None",
    val requiredInputQty: Long = 0L,
    val outputGood: String = "",
    val facilityStatus: String = "OPERATIONAL",
    val readyAtGameDay: Double = 0.0,
    val lastProducedUnits: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class BoardResolution(
    @DocumentId val docId: String = "",
    val ownerUid: String = "",
    val companyId: Long = 0L,
    val resolutionType: String = "", // CEO_APPOINTMENT, SHARE_SPLIT_2_FOR_1, IPO_LAUNCH, NEW_SHARE_ISSUANCE, DIVIDEND_DECLARED
    val title: String = "",
    val summary: String = "",
    val approvedByName: String = "",
    val gameDay: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
)
