package com.example.data.model

import androidx.annotation.Keep
import com.example.ui.navigation.HoaEntityRoutes
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class WorldClock(
    @DocumentId val docId: String = "",
    val scope: String = "WORLD",
    val eraNumber: Int = 1,
    @ServerTimestamp val eraStartTime: Timestamp? = null,
    @ServerTimestamp val lastTickTime: Timestamp? = null,
    val lastProcessedTickIndex: Long = 0L,
    val totalGameDaysElapsed: Double = 0.0,
)

@Keep
@IgnoreExtraProperties
data class ClockTickLog(
    @DocumentId val docId: String = "",
    val scope: String = "CLOCK_TICK",
    val tickIndex: Long = 0L,
    val gameDaysAtTick: Double = 0.0,
    val eventsProcessed: String = "",
    val processedByUid: String = "",
    @ServerTimestamp val processedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class PlayerCharacter(
    @DocumentId val docId: String = "",
    val playerId: Long = 0L,
    val ownerUid: String = "",
    val characterRole: String = "MAIN", // MAIN or ALT
    val fullName: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val genderPresentation: String = "Non-specified",
    val dateOfBirth: String = "Yr 1, Jan 1",
    val nationalityCountryId: Long = 101L,
    val placeOfBirth: String = "",
    val birthCountryId: Long = 101L,
    val birthStateId: Long = 201L,
    val birthCountyId: Long = 301L,
    val currentCountryId: Long = 101L,
    val currentStateId: Long = 201L,
    val currentCountyId: Long = 301L,
    val settlementName: String = "",
    val occupation: String = "Citizen",
    val biography: String = "",
    val backgroundStory: String = "",
    val personalInterests: List<String> = emptyList(),
    val avatarStyle: String = "ROYAL_KENTE",
    val avatarAccentIndex: Int = 0,
    val avatarSkinToneIdx: Int = 2,
    val avatarHairStyle: String = "SHORT_FADE",
    val avatarHairColorIdx: Int = 0,
    val avatarFaceStyle: String = "CONFIDENT",
    val avatarClothingStyle: String = "KENTE_ROBE",
    val avatarAccessory: String = "NONE",
    val accountStatus: String = "ACTIVE", // ACTIVE, SUSPENDED, DELETED
    val publicVisible: Boolean = true,
    val showNationality: Boolean = true,
    val showResidence: Boolean = true,
    val showOccupation: Boolean = true,
    val showActivity: Boolean = false,
    val showNetWorth: Boolean = false,
    val showPersonalBalance: Boolean = false,
    val showCapitalBalance: Boolean = false,
    val showEmployment: Boolean = true,
    val showCompanies: Boolean = true,
    val showPolitics: Boolean = true,
    val showPersonalIdentity: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun canonicalUrl(): String = HoaEntityRoutes.player(playerId)

    fun resolvePublicDisplayName(): String = when (accountStatus) {
        "DELETED" -> "User Deleted"
        "SUSPENDED" -> "Account Suspended"
        else -> fullName.ifBlank { "Player Not Found" }
    }
}

@Keep
@IgnoreExtraProperties
data class ImmigrationRecord(
    @DocumentId val docId: String = "",
    val ownerUid: String = "",
    val playerId: Long = 0L,
    val fromCountryId: Long = 0L,
    val toCountryId: Long = 0L,
    val toStateId: Long = 0L,
    val toCountyId: Long = 0L,
    val exitClearanceCode: String = "",
    val entryPermitCode: String = "",
    val issuedAtGameDay: Double = 0.0,
    val expiresAtGameDay: Double = 3600.0,
    val permitStatus: String = "APPROVED", // APPROVED, EXPIRED, RENEWED
    val feePaidCents: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class PrivacySecuritySettings(
    @DocumentId val docId: String = "",
    val ownerUid: String = "",
    val twoFactorEnabled: Boolean = true,
    val emailVerified: Boolean = true,
    val showNationality: Boolean = true,
    val showResidence: Boolean = true,
    val showOccupation: Boolean = true,
    val showPublicActivity: Boolean = false,
    val showNetWorth: Boolean = false,
    val showPersonalBalance: Boolean = false,
    val showCapitalBalance: Boolean = false,
    val showEmployment: Boolean = true,
    val showCompanies: Boolean = true,
    val showPolitics: Boolean = true,
    val showPersonalIdentity: Boolean = false,
    val notifySalaries: Boolean = true,
    val notifyTaxes: Boolean = true,
    val notifyElections: Boolean = true,
    val notifyCompanies: Boolean = true,
    val notifyMarketplace: Boolean = true,
    val allowFriendRequests: Boolean = true,
    val allowDirectMessages: Boolean = true,
    val publicProfileSearchable: Boolean = true,
    val rateLimitWindowSec: Int = 60,
    val antiAbuseSignalMode: String = "CARRIER_NAT_SAFE_FINGERPRINT",
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val docId: String = "",
    val ownerUid: String = "",
    val category: String = "System",
    val title: String = "",
    val detail: String = "",
    val referenceId: String = "",
    val readStatus: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
)
