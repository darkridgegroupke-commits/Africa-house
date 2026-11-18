package com.example.ui.state

import com.example.data.model.CampaignRecord
import com.example.data.model.CompanyFacility
import com.example.data.model.CompanyRecord
import com.example.data.model.ElectionRecord
import com.example.data.model.EmploymentContract
import com.example.data.model.GovernmentRecord
import com.example.data.model.ImmigrationRecord
import com.example.data.model.LoanRecord
import com.example.data.model.MarketOrder
import com.example.data.model.PlayerCharacter
import com.example.data.model.PrivacySecuritySettings
import com.example.domain.FinancialEngines

interface SimulationActions {
    fun selectCompany(companyId: Long)
    fun syncServerClock()
    fun advanceScheduledTick()
    fun checkUsernameAvailability(rawUsername: String)
    fun completeAccountRegistration(
        username: String,
        email: String,
        displayName: String,
        emailVerified: Boolean,
        onSuccess: () -> Unit = {},
    )
    fun syncEmailVerification(emailVerified: Boolean)
    fun createCharacter(
        fullName: String,
        gender: String,
        countryId: Long,
        stateId: Long,
        countyId: Long,
        occupation: String,
        biography: String,
        avatarStyle: String,
        avatarAccentIndex: Int,
        asAlt: Boolean,
    )
    fun createWizardCharacter(
        firstName: String,
        middleName: String,
        lastName: String,
        gender: String,
        dateOfBirth: String,
        nationalityCountryId: Long,
        birthCountryId: Long,
        birthStateId: Long,
        birthCountyId: Long,
        placeOfBirth: String,
        currentCountryId: Long,
        currentStateId: Long,
        currentCountyId: Long,
        settlementName: String,
        currencyCode: String,
        occupation: String,
        biography: String,
        backgroundStory: String,
        personalInterests: List<String>,
        avatarStyle: String,
        avatarAccentIndex: Int,
        avatarSkinToneIdx: Int,
        avatarHairStyle: String,
        avatarHairColorIdx: Int,
        avatarFaceStyle: String,
        avatarClothingStyle: String,
        avatarAccessory: String,
        asAlt: Boolean,
        onCreated: (PlayerCharacter) -> Unit = {},
    )
    fun switchCharacter(playerId: Long)
    fun updateAvatarAndProfile(playerId: Long, avatarStyle: String, accentIdx: Int, occupation: String, bio: String, publicVisible: Boolean)
    fun updateFullAvatarAndProfile(
        playerId: Long,
        avatarStyle: String,
        accentIdx: Int,
        skinToneIdx: Int,
        hairStyle: String,
        hairColorIdx: Int,
        faceStyle: String,
        clothingStyle: String,
        accessory: String,
        occupation: String,
        bio: String,
        interests: List<String>,
        publicVisible: Boolean,
    )
    fun softDeleteCharacter(playerId: Long)
    fun lookupPlayerById(playerId: Long)
    fun applyCrossBorderImmigration(targetCountryId: Long, targetStateId: Long, targetCountyId: Long)
    fun renewImmigrationPermit(record: ImmigrationRecord)
    fun saveSecuritySettings(settings: PrivacySecuritySettings)
    fun transferFunds(senderId: String, receiverId: String, amountCents: Long, note: String)
    fun signCivicEmployment(countryId: Long, stateId: Long, countyId: Long, jobTitle: String, hourlyWageCents: Long)
    fun collectSalaryCycle(contract: EmploymentContract, hours: Int = 8)
    fun takePersonalLoan(countyId: Long, principalCents: Long, collateral: String)
    fun repayLoan(loan: LoanRecord, amountCents: Long)
    fun purchaseTreasuryBond(countryId: Long, faceValueCents: Long)
    fun registerCompany(name: String, sector: String, ownershipType: String, countryId: Long, stateId: Long, countyId: Long, currency: String, capitalCents: Long)
    fun splitCompanyShares(company: CompanyRecord)
    fun issueCompanyShares(company: CompanyRecord, newShares: Long, priceCents: Long)
    fun buildCompanyFacility(company: CompanyRecord, specId: String, initialNpc: Int)
    fun adjustFacilityNpc(facility: CompanyFacility, npcCount: Int)
    fun runFacilityProduction(company: CompanyRecord, facility: CompanyFacility)
    fun placeMarketOrder(countyId: Long, commodity: String, side: String, quantity: Long, priceCents: Long, useCompanyEntity: Boolean)
    fun fillSellOrder(order: MarketOrder, qty: Long, countryId: Long, useCompany: Boolean)
    fun executeGovSpend(gov: GovernmentRecord, dept: String, purpose: String, amountCents: Long)
    fun updateGovTaxRates(gov: GovernmentRecord, primaryPct: Double, corpPct: Double, salesPct: Double)
    fun createParty(name: String, abbr: String, countryId: Long, manifesto: String)
    fun launchCampaign(system: String, office: String, city: String, countryId: Long, stateId: Long, currency: String, pitch: String)
    fun donateToCampaign(campaign: CampaignRecord, amountCents: Long)
    fun spendCampaignBudget(campaign: CampaignRecord, countyId: Long, amountCents: Long, eventNote: String)
    fun createElection(tier: String, jurisdictionId: Long, office: String, candidatesCsv: String)
    fun castVote(election: ElectionRecord, candidate: String)
    fun disputeElection(electionId: Long, reason: String)
    fun buyProperty(countyId: Long, type: String, title: String, valueCents: Long, monthlyLeaseCents: Long)
    fun voteReferendum(eraNumber: Int, choice: FinancialEngines.ReferendumChoice)
}

fun SimulationViewModel.asActions(): SimulationActions = object : SimulationActions {
    override fun selectCompany(companyId: Long) = this@asActions.selectCompany(companyId)
    override fun syncServerClock() { this@asActions.syncServerClock() }
    override fun advanceScheduledTick() { this@asActions.advanceScheduledTick() }
    override fun checkUsernameAvailability(rawUsername: String) = this@asActions.checkUsernameAvailability(rawUsername)
    override fun completeAccountRegistration(username: String, email: String, displayName: String, emailVerified: Boolean, onSuccess: () -> Unit) {
        this@asActions.completeAccountRegistration(username, email, displayName, emailVerified, onSuccess)
    }
    override fun syncEmailVerification(emailVerified: Boolean) {
        this@asActions.syncEmailVerification(emailVerified)
    }
    override fun createCharacter(fullName: String, gender: String, countryId: Long, stateId: Long, countyId: Long, occupation: String, biography: String, avatarStyle: String, avatarAccentIndex: Int, asAlt: Boolean) {
        this@asActions.createCharacter(fullName, gender, countryId, stateId, countyId, occupation, biography, avatarStyle, avatarAccentIndex, asAlt)
    }
    override fun createWizardCharacter(
        firstName: String, middleName: String, lastName: String, gender: String, dateOfBirth: String,
        nationalityCountryId: Long, birthCountryId: Long, birthStateId: Long, birthCountyId: Long, placeOfBirth: String,
        currentCountryId: Long, currentStateId: Long, currentCountyId: Long, settlementName: String, currencyCode: String,
        occupation: String, biography: String, backgroundStory: String, personalInterests: List<String>,
        avatarStyle: String, avatarAccentIndex: Int, avatarSkinToneIdx: Int, avatarHairStyle: String,
        avatarHairColorIdx: Int, avatarFaceStyle: String, avatarClothingStyle: String, avatarAccessory: String,
        asAlt: Boolean, onCreated: (PlayerCharacter) -> Unit,
    ) {
        this@asActions.createWizardCharacter(
            firstName, middleName, lastName, gender, dateOfBirth,
            nationalityCountryId, birthCountryId, birthStateId, birthCountyId, placeOfBirth,
            currentCountryId, currentStateId, currentCountyId, settlementName, currencyCode,
            occupation, biography, backgroundStory, personalInterests,
            avatarStyle, avatarAccentIndex, avatarSkinToneIdx, avatarHairStyle,
            avatarHairColorIdx, avatarFaceStyle, avatarClothingStyle, avatarAccessory,
            asAlt, onCreated,
        )
    }
    override fun switchCharacter(playerId: Long) { this@asActions.switchCharacter(playerId) }
    override fun updateAvatarAndProfile(playerId: Long, avatarStyle: String, accentIdx: Int, occupation: String, bio: String, publicVisible: Boolean) {
        this@asActions.updateAvatarAndProfile(playerId, avatarStyle, accentIdx, occupation, bio, publicVisible)
    }
    override fun updateFullAvatarAndProfile(
        playerId: Long, avatarStyle: String, accentIdx: Int, skinToneIdx: Int, hairStyle: String,
        hairColorIdx: Int, faceStyle: String, clothingStyle: String, accessory: String,
        occupation: String, bio: String, interests: List<String>, publicVisible: Boolean,
    ) {
        this@asActions.updateFullAvatarAndProfile(
            playerId, avatarStyle, accentIdx, skinToneIdx, hairStyle,
            hairColorIdx, faceStyle, clothingStyle, accessory,
            occupation, bio, interests, publicVisible,
        )
    }
    override fun softDeleteCharacter(playerId: Long) { this@asActions.softDeleteCharacter(playerId) }
    override fun lookupPlayerById(playerId: Long) = this@asActions.lookupPlayerById(playerId)
    override fun applyCrossBorderImmigration(targetCountryId: Long, targetStateId: Long, targetCountyId: Long) {
        this@asActions.applyCrossBorderImmigration(targetCountryId, targetStateId, targetCountyId)
    }
    override fun renewImmigrationPermit(record: ImmigrationRecord) { this@asActions.renewImmigrationPermit(record) }
    override fun saveSecuritySettings(settings: PrivacySecuritySettings) { this@asActions.saveSecuritySettings(settings) }
    override fun transferFunds(senderId: String, receiverId: String, amountCents: Long, note: String) {
        this@asActions.transferFunds(senderId, receiverId, amountCents, note)
    }
    override fun signCivicEmployment(countryId: Long, stateId: Long, countyId: Long, jobTitle: String, hourlyWageCents: Long) {
        this@asActions.signCivicEmployment(countryId, stateId, countyId, jobTitle, hourlyWageCents)
    }
    override fun collectSalaryCycle(contract: EmploymentContract, hours: Int) {
        this@asActions.collectSalaryCycle(contract, hours)
    }
    override fun takePersonalLoan(countyId: Long, principalCents: Long, collateral: String) {
        this@asActions.takePersonalLoan(countyId, principalCents, collateral)
    }
    override fun repayLoan(loan: LoanRecord, amountCents: Long) {
        this@asActions.repayLoan(loan, amountCents)
    }
    override fun purchaseTreasuryBond(countryId: Long, faceValueCents: Long) {
        this@asActions.purchaseTreasuryBond(countryId, faceValueCents)
    }
    override fun registerCompany(name: String, sector: String, ownershipType: String, countryId: Long, stateId: Long, countyId: Long, currency: String, capitalCents: Long) {
        this@asActions.registerCompany(name, sector, ownershipType, countryId, stateId, countyId, currency, capitalCents)
    }
    override fun splitCompanyShares(company: CompanyRecord) { this@asActions.splitCompanyShares(company) }
    override fun issueCompanyShares(company: CompanyRecord, newShares: Long, priceCents: Long) {
        this@asActions.issueCompanyShares(company, newShares, priceCents)
    }
    override fun buildCompanyFacility(company: CompanyRecord, specId: String, initialNpc: Int) {
        this@asActions.buildCompanyFacility(company, specId, initialNpc)
    }
    override fun adjustFacilityNpc(facility: CompanyFacility, npcCount: Int) {
        this@asActions.adjustFacilityNpc(facility, npcCount)
    }
    override fun runFacilityProduction(company: CompanyRecord, facility: CompanyFacility) {
        this@asActions.runFacilityProduction(company, facility)
    }
    override fun placeMarketOrder(countyId: Long, commodity: String, side: String, quantity: Long, priceCents: Long, useCompanyEntity: Boolean) {
        this@asActions.placeMarketOrder(countyId, commodity, side, quantity, priceCents, useCompanyEntity)
    }
    override fun fillSellOrder(order: MarketOrder, qty: Long, countryId: Long, useCompany: Boolean) {
        this@asActions.fillSellOrder(order, qty, countryId, useCompany)
    }
    override fun executeGovSpend(gov: GovernmentRecord, dept: String, purpose: String, amountCents: Long) {
        this@asActions.executeGovSpend(gov, dept, purpose, amountCents)
    }
    override fun updateGovTaxRates(gov: GovernmentRecord, primaryPct: Double, corpPct: Double, salesPct: Double) {
        this@asActions.updateGovTaxRates(gov, primaryPct, corpPct, salesPct)
    }
    override fun createParty(name: String, abbr: String, countryId: Long, manifesto: String) {
        this@asActions.createParty(name, abbr, countryId, manifesto)
    }
    override fun launchCampaign(system: String, office: String, city: String, countryId: Long, stateId: Long, currency: String, pitch: String) {
        this@asActions.launchCampaign(system, office, city, countryId, stateId, currency, pitch)
    }
    override fun donateToCampaign(campaign: CampaignRecord, amountCents: Long) {
        this@asActions.donateToCampaign(campaign, amountCents)
    }
    override fun spendCampaignBudget(campaign: CampaignRecord, countyId: Long, amountCents: Long, eventNote: String) {
        this@asActions.spendCampaignBudget(campaign, countyId, amountCents, eventNote)
    }
    override fun createElection(tier: String, jurisdictionId: Long, office: String, candidatesCsv: String) {
        this@asActions.createElection(tier, jurisdictionId, office, candidatesCsv)
    }
    override fun castVote(election: ElectionRecord, candidate: String) {
        this@asActions.castVote(election, candidate)
    }
    override fun disputeElection(electionId: Long, reason: String) {
        this@asActions.disputeElection(electionId, reason)
    }
    override fun buyProperty(countyId: Long, type: String, title: String, valueCents: Long, monthlyLeaseCents: Long) {
        this@asActions.buyProperty(countyId, type, title, valueCents, monthlyLeaseCents)
    }
    override fun voteReferendum(eraNumber: Int, choice: FinancialEngines.ReferendumChoice) {
        this@asActions.voteReferendum(eraNumber, choice)
    }
}

data class SimulationShellBindings(
    val snapshot: SimulationSnapshot = SimulationSnapshot(),
    val feedback: ActionFeedback = ActionFeedback(),
    val playerLookupResult: Pair<String, PlayerCharacter?>? = null,
    val usernameAvailability: Pair<String, Boolean?>? = null,
    val actions: SimulationActions? = null,
)
