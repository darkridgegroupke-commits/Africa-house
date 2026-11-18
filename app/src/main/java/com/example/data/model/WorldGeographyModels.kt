package com.example.data.model

import androidx.annotation.Keep
import com.example.ui.navigation.HoaEntityRoutes
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserAccount(
    @DocumentId val docId: String = "",
    val userId: String = "",
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val emailVerified: Boolean = false,
    val termsAcceptedAt: Timestamp? = null,
    val role: String = "citizen",
    val activeCountryId: Long = 0L,
    val activeStateId: Long = 0L,
    val activeCountyId: Long = 0L,
    val mainPlayerId: Long = 0L,
    val activePlayerId: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class UsernameReservation(
    @DocumentId val docId: String = "",
    val username: String = "",
    val usernameLower: String = "",
    val ownerUid: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class InternalRelocation(
    @DocumentId val docId: String = "",
    val countryId: Long = 0L,
    val fromStateId: Long = 0L,
    val fromCountyId: Long = 0L,
    val toStateId: Long = 0L,
    val toCountyId: Long = 0L,
    val relocationType: String = "INTERNAL_RELOCATION",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class Country(
    @DocumentId val docId: String = "",
    val countryId: Long = 0L,
    val worldScope: String = "AFRICA",
    val name: String = "",
    val isoCode: String = "",
    val flagEmoji: String = "",
    val capitalCity: String = "",
    val currencyCode: String = "",
    val currencyName: String = "",
    val parliamentName: String = "",
    val parliamentSeats: Long = 0L,
    val population: Long = 0L,
    val infrastructureIndex: Long = 0L,
    val incomeTaxRatePct: Double = 0.0,
    val corporateTaxRatePct: Double = 0.0,
    val salesTaxRatePct: Double = 0.0,
    val immigrationRenewalYears: Long = 10L,
    val naturalResources: List<String> = emptyList(),
    val industries: List<String> = emptyList(),
    val treasuryStatus: String = "",
    val activePartiesCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
) {
    fun canonicalUrl(): String = HoaEntityRoutes.world(countryId)
}

@Keep
@IgnoreExtraProperties
data class StateRegion(
    @DocumentId val docId: String = "",
    val stateId: Long = 0L,
    val countryId: Long = 0L,
    val name: String = "",
    val capitalCity: String = "",
    val governorTitle: String = "",
    val departments: List<String> = emptyList(),
    val stateTaxRatePct: Double = 0.0,
    val infrastructureIndex: Long = 0L,
    val population: Long = 0L,
    val economicFocus: String = "",
    val treasuryStatus: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
) {
    fun canonicalUrl(): String = HoaEntityRoutes.state(stateId)
}

@Keep
@IgnoreExtraProperties
data class County(
    @DocumentId val docId: String = "",
    val countyId: Long = 0L,
    val stateId: Long = 0L,
    val countryId: Long = 0L,
    val name: String = "",
    val settlements: List<String> = emptyList(),
    val localServices: List<String> = emptyList(),
    val infrastructureIndex: Long = 0L,
    val population: Long = 0L,
    val registeredBusinesses: Long = 0L,
    val marketplaceStatus: String = "",
    val economicSummary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
) {
    fun canonicalUrl(): String = HoaEntityRoutes.county(countyId)
}
