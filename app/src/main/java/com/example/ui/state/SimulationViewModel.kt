package com.example.ui.state

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
import com.example.data.model.AuditLogRecord
import com.example.data.model.BankAccount
import com.example.data.model.BoardResolution
import com.example.data.model.BondRecord
import com.example.data.model.CampaignRecord
import com.example.data.model.ClockTickLog
import com.example.data.model.CompanyFacility
import com.example.data.model.CompanyRecord
import com.example.data.model.ElectionRecord
import com.example.data.model.EmploymentContract
import com.example.data.model.GovernmentExpenditure
import com.example.data.model.GovernmentRecord
import com.example.data.model.ImmigrationRecord
import com.example.data.model.InventoryItem
import com.example.data.model.LedgerTransaction
import com.example.data.model.LoanRecord
import com.example.data.model.MarketOrder
import com.example.data.model.PlayerCharacter
import com.example.data.model.PoliticalParty
import com.example.data.model.PrivacySecuritySettings
import com.example.data.model.PropertyAsset
import com.example.data.model.ReferendumRecord
import com.example.data.model.ShareholderHolding
import com.example.data.model.TradeExecution
import com.example.data.model.WorldClock
import com.example.data.repository.BankingAndEmploymentRepository
import com.example.data.repository.ClockAndIdentityRepository
import com.example.data.repository.CompanyAndMarketRepository
import com.example.data.repository.GovernancePoliticsAssetsRepository
import com.example.data.seed.SectorFacilityCatalog
import com.example.domain.FinancialEngines
import com.example.domain.GameClockEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActionFeedback(
    val isBusy: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
)

data class SimulationSnapshot(
    val worldClock: WorldClock? = null,
    val tickLogs: List<ClockTickLog> = emptyList(),
    val myCharacters: List<PlayerCharacter> = emptyList(),
    val activeCharacterId: Long = 0L,
    val immigrationRecords: List<ImmigrationRecord> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val privacySecurity: PrivacySecuritySettings? = null,
    val myAccounts: List<BankAccount> = emptyList(),
    val treasuryAccounts: List<BankAccount> = emptyList(),
    val ledgerTransactions: List<LedgerTransaction> = emptyList(),
    val employmentContracts: List<EmploymentContract> = emptyList(),
    val loans: List<LoanRecord> = emptyList(),
    val bonds: List<BondRecord> = emptyList(),
    val companies: List<CompanyRecord> = emptyList(),
    val selectedCompanyId: Long = 0L,
    val selectedCompanyShareholders: List<ShareholderHolding> = emptyList(),
    val selectedCompanyFacilities: List<CompanyFacility> = emptyList(),
    val selectedCompanyResolutions: List<BoardResolution> = emptyList(),
    val marketOrders: List<MarketOrder> = emptyList(),
    val myInventory: List<InventoryItem> = emptyList(),
    val tradeHistory: List<TradeExecution> = emptyList(),
    val governments: List<GovernmentRecord> = emptyList(),
    val governmentExpenditures: List<GovernmentExpenditure> = emptyList(),
    val politicalParties: List<PoliticalParty> = emptyList(),
    val campaigns: List<CampaignRecord> = emptyList(),
    val elections: List<ElectionRecord> = emptyList(),
    val properties: List<PropertyAsset> = emptyList(),
    val auditLogs: List<AuditLogRecord> = emptyList(),
    val referendums: List<ReferendumRecord> = emptyList(),
) {
    val activeCharacter: PlayerCharacter?
        get() = myCharacters.firstOrNull { it.playerId == activeCharacterId && it.accountStatus == "ACTIVE" }
            ?: myCharacters.firstOrNull { it.characterRole == "MAIN" && it.accountStatus == "ACTIVE" }
            ?: myCharacters.firstOrNull { it.accountStatus == "ACTIVE" }

    val personalAccount: BankAccount?
        get() = myAccounts.firstOrNull { it.accountType == "PERSONAL" }

    val capitalAccount: BankAccount?
        get() = myAccounts.firstOrNull { it.accountType == "CAPITAL" }

    val campaignAccount: BankAccount?
        get() = myAccounts.firstOrNull { it.accountType == "CAMPAIGN" }

    val selectedCompany: CompanyRecord?
        get() = companies.firstOrNull { it.companyId == selectedCompanyId } ?: companies.firstOrNull()

    val currentGameDay: Double
        get() = worldClock?.totalGameDaysElapsed ?: 0.0

    val formattedCalendarDate: String
        get() = GameClockEngine.toGameCalendarDate(
            eraNumber = worldClock?.eraNumber ?: 1,
            totalGameDays = currentGameDay,
        ).formattedFull()
}

@OptIn(ExperimentalCoroutinesApi::class)
class SimulationViewModel(
    private val uid: String,
    private val clockIdentityRepo: ClockAndIdentityRepository,
    private val bankingRepo: BankingAndEmploymentRepository,
    private val companyMarketRepo: CompanyAndMarketRepository,
    private val govPoliticsRepo: GovernancePoliticsAssetsRepository,
) : ViewModel() {

    private val _selectedCompanyId = MutableStateFlow(0L)
    private val _activeCharacterOverride = MutableStateFlow(0L)
    private val _feedback = MutableStateFlow(ActionFeedback())
    val feedback: StateFlow<ActionFeedback> = _feedback.asStateFlow()

    private val _playerLookupResult = MutableStateFlow<Pair<String, PlayerCharacter?>?>(null)
    val playerLookupResult: StateFlow<Pair<String, PlayerCharacter?>?> = _playerLookupResult.asStateFlow()

    private val _usernameAvailability = MutableStateFlow<Pair<String, Boolean?>?>(null)
    val usernameAvailability: StateFlow<Pair<String, Boolean?>?> = _usernameAvailability.asStateFlow()

    init {
        viewModelScope.launch {
            clockIdentityRepo.ensureAndSyncServerClock()
            bankingRepo.ensureCitizenAccounts("NGN")
            govPoliticsRepo.ensureActiveReferendum(1)
        }
    }

    private val selectedCompanyShareholdersFlow = _selectedCompanyId.flatMapLatest { id ->
        if (id <= 0L) flowOf(emptyList()) else companyMarketRepo.observeShareholders(id)
    }

    private val selectedCompanyFacilitiesFlow = _selectedCompanyId.flatMapLatest { id ->
        if (id <= 0L) flowOf(emptyList()) else companyMarketRepo.observeFacilities(id)
    }

    private val selectedCompanyResolutionsFlow = _selectedCompanyId.flatMapLatest { id ->
        if (id <= 0L) flowOf(emptyList()) else companyMarketRepo.observeBoardResolutions(id)
    }

    private val identityGroupFlow = combine(
        clockIdentityRepo.observeWorldClock(),
        clockIdentityRepo.observeClockTickLogs(),
        clockIdentityRepo.observeMyCharacters(uid),
        _activeCharacterOverride,
        clockIdentityRepo.observeImmigrationRecords(uid),
        clockIdentityRepo.observeNotifications(uid),
        clockIdentityRepo.observePrivacySecurity(uid),
    ) { vals ->
        @Suppress("UNCHECKED_CAST")
        Triple(
            Triple(vals[0] as WorldClock?, vals[1] as List<ClockTickLog>, vals[2] as List<PlayerCharacter>),
            vals[3] as Long,
            Triple(vals[4] as List<ImmigrationRecord>, vals[5] as List<AppNotification>, vals[6] as PrivacySecuritySettings?),
        )
    }

    private val financeGroupFlow = combine(
        bankingRepo.observeMyAccounts(uid),
        bankingRepo.observeTreasuryAccounts(),
        bankingRepo.observeMyLedgerTransactions(uid),
        bankingRepo.observeEmploymentContracts(uid),
        bankingRepo.observeMyLoans(uid),
        bankingRepo.observeBonds(),
    ) { vals ->
        @Suppress("UNCHECKED_CAST")
        Pair(
            Triple(vals[0] as List<BankAccount>, vals[1] as List<BankAccount>, vals[2] as List<LedgerTransaction>),
            Triple(vals[3] as List<EmploymentContract>, vals[4] as List<LoanRecord>, vals[5] as List<BondRecord>),
        )
    }

    private val companyMarketGroupFlow = combine(
        companyMarketRepo.observeCompanies(),
        _selectedCompanyId,
        selectedCompanyShareholdersFlow,
        selectedCompanyFacilitiesFlow,
        selectedCompanyResolutionsFlow,
        companyMarketRepo.observeMarketOrders(),
        companyMarketRepo.observeMyInventory(uid),
        companyMarketRepo.observeTradeHistory(),
    ) { vals ->
        @Suppress("UNCHECKED_CAST")
        val companies = vals[0] as List<CompanyRecord>
        val selId = (vals[1] as Long).let { if (it <= 0L) companies.firstOrNull()?.companyId ?: 0L else it }
        if ((vals[1] as Long) <= 0L && selId > 0L) {
            _selectedCompanyId.value = selId
        }
        Triple(
            Pair(companies, selId),
            Triple(vals[2] as List<ShareholderHolding>, vals[3] as List<CompanyFacility>, vals[4] as List<BoardResolution>),
            Triple(vals[5] as List<MarketOrder>, vals[6] as List<InventoryItem>, vals[7] as List<TradeExecution>),
        )
    }

    private val govPoliticsGroupFlow = combine(
        govPoliticsRepo.observeGovernments(),
        govPoliticsRepo.observeGovernmentExpenditures(),
        govPoliticsRepo.observePoliticalParties(),
        govPoliticsRepo.observeCampaigns(),
        govPoliticsRepo.observeElections(),
        govPoliticsRepo.observeMyProperties(uid),
        govPoliticsRepo.observeAuditLogs(uid),
        govPoliticsRepo.observeReferendums(),
    ) { vals ->
        @Suppress("UNCHECKED_CAST")
        Triple(
            Triple(vals[0] as List<GovernmentRecord>, vals[1] as List<GovernmentExpenditure>, vals[2] as List<PoliticalParty>),
            Triple(vals[3] as List<CampaignRecord>, vals[4] as List<ElectionRecord>, vals[5] as List<PropertyAsset>),
            Pair(vals[6] as List<AuditLogRecord>, vals[7] as List<ReferendumRecord>),
        )
    }

    val snapshot: StateFlow<SimulationSnapshot> = combine(
        identityGroupFlow,
        financeGroupFlow,
        companyMarketGroupFlow,
        govPoliticsGroupFlow,
    ) { idGroup, finGroup, coGroup, govGroup ->
        SimulationSnapshot(
            worldClock = idGroup.first.first,
            tickLogs = idGroup.first.second,
            myCharacters = idGroup.first.third,
            activeCharacterId = idGroup.second,
            immigrationRecords = idGroup.third.first,
            notifications = idGroup.third.second,
            privacySecurity = idGroup.third.third,
            myAccounts = finGroup.first.first,
            treasuryAccounts = finGroup.first.second,
            ledgerTransactions = finGroup.first.third,
            employmentContracts = finGroup.second.first,
            loans = finGroup.second.second,
            bonds = finGroup.second.third,
            companies = coGroup.first.first,
            selectedCompanyId = coGroup.first.second,
            selectedCompanyShareholders = coGroup.second.first,
            selectedCompanyFacilities = coGroup.second.second,
            selectedCompanyResolutions = coGroup.second.third,
            marketOrders = coGroup.third.first,
            myInventory = coGroup.third.second,
            tradeHistory = coGroup.third.third,
            governments = govGroup.first.first,
            governmentExpenditures = govGroup.first.second,
            politicalParties = govGroup.first.third,
            campaigns = govGroup.second.first,
            elections = govGroup.second.second,
            properties = govGroup.second.third,
            auditLogs = govGroup.third.first,
            referendums = govGroup.third.second,
        )
    }
        .catch { e ->
            if (e is CancellationException) throw e
            Log.w(TAG, "Simulation snapshot stream error", e)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = SimulationSnapshot(),
        )

    private fun runAction(label: String, block: suspend () -> Result<Any?>) {
        viewModelScope.launch {
            _feedback.value = ActionFeedback(isBusy = true, message = "$label...", isError = false)
            block().fold(
                onSuccess = { res ->
                    val text = (res as? String) ?: "$label completed."
                    _feedback.value = ActionFeedback(isBusy = false, message = text, isError = false)
                },
                onFailure = { err ->
                    _feedback.value = ActionFeedback(
                        isBusy = false,
                        message = err.localizedMessage ?: "$label failed.",
                        isError = true,
                    )
                },
            )
        }
    }

    fun selectCompany(companyId: Long) {
        _selectedCompanyId.value = companyId
    }

    fun syncServerClock() = runAction("Syncing Server Clock") {
        clockIdentityRepo.ensureAndSyncServerClock().map { clock ->
            val cal = GameClockEngine.toGameCalendarDate(clock.eraNumber, clock.totalGameDaysElapsed)
            "Server clock synced: ${cal.formattedFull()} (Tick #${clock.lastProcessedTickIndex})"
        }
    }

    fun advanceScheduledTick() = runAction("Processing Scheduled World Tick") {
        clockIdentityRepo.recordScheduledTick().map { clock ->
            "Processed Tick #${clock.lastProcessedTickIndex} (Game Day ${clock.totalGameDaysElapsed.toLong()})"
        }
    }

    fun checkUsernameAvailability(rawUsername: String) {
        viewModelScope.launch {
            _usernameAvailability.value = rawUsername to null
            clockIdentityRepo.checkUsernameAvailability(rawUsername).fold(
                onSuccess = { available -> _usernameAvailability.value = rawUsername to available },
                onFailure = { _usernameAvailability.value = rawUsername to null },
            )
        }
    }

    fun completeAccountRegistration(
        username: String,
        email: String,
        displayName: String,
        emailVerified: Boolean,
        onSuccess: () -> Unit = {},
    ) = viewModelScope.launch {
        _feedback.value = ActionFeedback(isBusy = true, message = "Registering account...", isError = false)
        clockIdentityRepo.completeAccountRegistration(username, email, displayName, emailVerified).fold(
            onSuccess = { clean ->
                _feedback.value = ActionFeedback(isBusy = false, message = "Registered @$clean successfully.", isError = false)
                onSuccess()
            },
            onFailure = { err ->
                _feedback.value = ActionFeedback(isBusy = false, message = err.localizedMessage ?: "Registration failed.", isError = true)
            },
        )
    }

    fun syncEmailVerification(emailVerified: Boolean) = viewModelScope.launch {
        clockIdentityRepo.syncEmailVerificationStatus(emailVerified)
    }

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
    ) = runAction("Registering Citizen") {
        val currentDay = snapshot.value.currentGameDay
        val dob = GameClockEngine.toGameCalendarDate(1, currentDay).formattedShort()
        clockIdentityRepo.createCharacter(
            fullName = fullName,
            genderPresentation = gender,
            dateOfBirth = dob,
            nationalityCountryId = countryId,
            placeOfBirth = "County $countyId",
            currentCountryId = countryId,
            currentStateId = stateId,
            currentCountyId = countyId,
            occupation = occupation,
            biography = biography,
            avatarStyle = avatarStyle,
            avatarAccentIndex = avatarAccentIndex,
            asAltCharacter = asAlt,
        ).map { p -> "${p.characterRole} Citizen #${p.playerId} (${p.fullName}) created." }
    }

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
    ) = viewModelScope.launch {
        _feedback.value = ActionFeedback(isBusy = true, message = "Creating citizen identity...", isError = false)
        val fullName = com.example.domain.OnboardingEngine.buildFullName(firstName, middleName, lastName)
        clockIdentityRepo.createCharacter(
            fullName = fullName,
            genderPresentation = gender,
            dateOfBirth = dateOfBirth,
            nationalityCountryId = nationalityCountryId,
            placeOfBirth = placeOfBirth,
            currentCountryId = currentCountryId,
            currentStateId = currentStateId,
            currentCountyId = currentCountyId,
            occupation = occupation,
            biography = biography,
            avatarStyle = avatarStyle,
            avatarAccentIndex = avatarAccentIndex,
            asAltCharacter = asAlt,
            firstName = firstName,
            middleName = middleName,
            lastName = lastName,
            birthCountryId = birthCountryId,
            birthStateId = birthStateId,
            birthCountyId = birthCountyId,
            settlementName = settlementName,
            backgroundStory = backgroundStory,
            personalInterests = personalInterests,
            avatarSkinToneIdx = avatarSkinToneIdx,
            avatarHairStyle = avatarHairStyle,
            avatarHairColorIdx = avatarHairColorIdx,
            avatarFaceStyle = avatarFaceStyle,
            avatarClothingStyle = avatarClothingStyle,
            avatarAccessory = avatarAccessory,
            currencyCode = currencyCode,
        ).fold(
            onSuccess = { p ->
                _activeCharacterOverride.value = p.playerId
                _feedback.value = ActionFeedback(
                    isBusy = false,
                    message = "Welcome, ${p.fullName} (#${p.playerId})! Settled in ${p.settlementName.ifBlank { "County ${p.currentCountyId}" }}.",
                    isError = false,
                )
                onCreated(p)
            },
            onFailure = { err ->
                _feedback.value = ActionFeedback(
                    isBusy = false,
                    message = err.localizedMessage ?: "Character creation failed.",
                    isError = true,
                )
            },
        )
    }

    fun switchCharacter(playerId: Long) = runAction("Switching Character") {
        _activeCharacterOverride.value = playerId
        clockIdentityRepo.switchActiveCharacter(playerId).map { "Switched active character to #$playerId" }
    }

    fun updateAvatarAndProfile(playerId: Long, avatarStyle: String, accentIdx: Int, occupation: String, bio: String, publicVisible: Boolean) =
        runAction("Updating Profile") {
            clockIdentityRepo.updateAvatarAndBio(playerId, avatarStyle, accentIdx, occupation, bio, publicVisible)
                .map { "Profile #$playerId updated." }
        }

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
    ) = runAction("Saving Avatar & Citizen Details") {
        clockIdentityRepo.updateAvatarAndBio(
            playerId = playerId,
            avatarStyle = avatarStyle,
            avatarAccentIndex = accentIdx,
            occupation = occupation,
            biography = bio,
            publicVisible = publicVisible,
            avatarSkinToneIdx = skinToneIdx,
            avatarHairStyle = hairStyle,
            avatarHairColorIdx = hairColorIdx,
            avatarFaceStyle = faceStyle,
            avatarClothingStyle = clothingStyle,
            avatarAccessory = accessory,
            personalInterests = interests,
        ).map { "Updated appearance & details for #$playerId." }
    }

    fun softDeleteCharacter(playerId: Long) = runAction("Deleting Character Record") {
        clockIdentityRepo.softDeleteCharacter(playerId).map { "Character #$playerId now resolves as 'User Deleted'." }
    }

    fun lookupPlayerById(playerId: Long) {
        viewModelScope.launch {
            clockIdentityRepo.lookupPublicPlayerById(playerId).onSuccess {
                _playerLookupResult.value = it
            }
        }
    }

    fun applyCrossBorderImmigration(targetCountryId: Long, targetStateId: Long, targetCountyId: Long) =
        runAction("Processing Entry Permit") {
            val active = snapshot.value.activeCharacter
                ?: return@runAction Result.failure(IllegalStateException("Create a Citizen character in Profile first."))
            clockIdentityRepo.processCrossBorderImmigration(
                player = active,
                targetCountryId = targetCountryId,
                targetStateId = targetStateId,
                targetCountyId = targetCountyId,
                currentGameDay = snapshot.value.currentGameDay,
            ).map { rec -> "Approved Exit Clearance & 10-Yr Entry Permit ${rec.entryPermitCode}" }
        }

    fun renewImmigrationPermit(record: ImmigrationRecord) = runAction("Renewing Entry Permit") {
        clockIdentityRepo.renewImmigrationPermit(record, snapshot.value.currentGameDay)
            .map { "Renewed ${record.entryPermitCode} for 10 in-game years." }
    }

    fun saveSecuritySettings(settings: PrivacySecuritySettings) = runAction("Saving Security & Privacy Settings") {
        val activeId = snapshot.value.activeCharacter?.playerId ?: 0L
        clockIdentityRepo.savePrivacySecuritySettings(settings, activeId).map { "Security, privacy, notification & social preferences saved." }
    }

    fun transferFunds(senderId: String, receiverId: String, amountCents: Long, note: String) =
        runAction("Executing Atomic Ledger Transfer") {
            bankingRepo.executeAtomicTransfer(
                senderAccountId = senderId,
                receiverAccountId = receiverId,
                amountCents = amountCents,
                txType = "ACCOUNT_TRANSFER",
                reference = note,
                gameDay = snapshot.value.currentGameDay,
            )
        }

    fun signCivicEmployment(countryId: Long, stateId: Long, countyId: Long, jobTitle: String, hourlyWageCents: Long) =
        runAction("Signing Employment Contract") {
            val char = snapshot.value.activeCharacter
                ?: return@runAction Result.failure(IllegalStateException("Register a Citizen profile first."))
            bankingRepo.signEmploymentContract(
                playerId = char.playerId,
                employerEntityId = "gov_county_$countyId",
                employerName = "County $countyId Civil Service",
                employerAccountId = "treasury_county_$countyId",
                jobTitle = jobTitle,
                hourlyWageCents = hourlyWageCents,
                countryId = countryId,
                stateId = stateId,
                countyId = countyId,
                currentGameDay = snapshot.value.currentGameDay,
            ).map { c -> "Signed ${c.jobTitle} contract (${FinancialEngines.formatCurrency(c.hourlyWageCents)}/hr)." }
        }

    fun collectSalaryCycle(contract: EmploymentContract, hours: Int = 8) = runAction("Settling Payroll & Taxes") {
        val natGov = snapshot.value.governments.firstOrNull { it.docId == "gov_country_${contract.countryId}" }
        val stGov = snapshot.value.governments.firstOrNull { it.docId == "gov_state_${contract.stateId}" }
        val ctGov = snapshot.value.governments.firstOrNull { it.docId == "gov_county_${contract.countyId}" }
        val cycleKey = "t${snapshot.value.worldClock?.lastProcessedTickIndex ?: 0}_${System.currentTimeMillis()}"
        bankingRepo.settleSalaryCycle(
            contract = contract,
            hoursWorked = hours,
            nationalTaxPct = natGov?.primaryTaxRatePct ?: 18.0,
            stateTaxPct = stGov?.primaryTaxRatePct ?: 4.0,
            countyTaxPct = ctGov?.primaryTaxRatePct ?: 2.0,
            currentGameDay = snapshot.value.currentGameDay,
            cycleKey = cycleKey,
        )
    }

    fun takePersonalLoan(countyId: Long, principalCents: Long, collateral: String) = runAction("Originating Loan Liability") {
        val personalAcct = snapshot.value.personalAccount?.docId
            ?: return@runAction Result.failure(IllegalStateException("Personal bank account unavailable."))
        bankingRepo.issueLoan(
            borrowerEntityId = uid,
            borrowerAccountId = personalAcct,
            lenderAccountId = "treasury_country_${(countyId / 100L).let { if (it in 1..2) 101L else 101L }}".let {
                val matched = snapshot.value.treasuryAccounts.firstOrNull { acct -> acct.ownerEntityId == "gov_county_$countyId" }
                matched?.docId ?: "treasury_county_$countyId"
            },
            borrowerCategory = "PERSONAL",
            principalCents = principalCents,
            annualInterestPct = 8.5,
            collateralSummary = collateral,
            currentGameDay = snapshot.value.currentGameDay,
        ).map { "Loan liability recorded: ${FinancialEngines.formatCurrency(principalCents)}" }
    }

    fun repayLoan(loan: LoanRecord, amountCents: Long) = runAction("Repaying Loan") {
        bankingRepo.repayLoan(loan, amountCents, snapshot.value.currentGameDay).map { "Loan repayment settled." }
    }

    fun purchaseTreasuryBond(countryId: Long, faceValueCents: Long) = runAction("Subscribing Treasury Bond") {
        val capitalAcct = snapshot.value.capitalAccount?.docId
            ?: return@runAction Result.failure(IllegalStateException("Capital account unavailable."))
        bankingRepo.purchaseBond(
            issuerType = "GOVERNMENT",
            issuerName = "Republic Treasury /world/$countryId",
            issuerAccountId = "treasury_country_$countryId",
            buyerAccountId = capitalAcct,
            faceValueCents = faceValueCents,
            couponRatePct = 7.25,
            currentGameDay = snapshot.value.currentGameDay,
        ).map { "Treasury Bond issued (${FinancialEngines.formatCurrency(faceValueCents)} @ 7.25%)." }
    }

    fun registerCompany(name: String, sector: String, ownershipType: String, countryId: Long, stateId: Long, countyId: Long, currency: String, capitalCents: Long) =
        runAction("Incorporating Company") {
            val active = snapshot.value.activeCharacter
                ?: return@runAction Result.failure(IllegalStateException("Register a Citizen profile first."))
            companyMarketRepo.registerCompany(
                companyName = name,
                sector = sector,
                ownershipType = ownershipType,
                founderPlayerId = active.playerId,
                founderName = active.fullName,
                countryId = countryId,
                stateId = stateId,
                countyId = countyId,
                currencyCode = currency,
                initialCapitalContributionCents = capitalCents,
                currentGameDay = snapshot.value.currentGameDay,
            ).map { c ->
                _selectedCompanyId.value = c.companyId
                "Registered ${c.name} (${c.registrationCertNumber}) • 1,000,000 shares @ 10.00"
            }
        }

    fun splitCompanyShares(company: CompanyRecord) = runAction("Executing 2-for-1 Share Split") {
        companyMarketRepo.executeTwoForOneSplit(company, snapshot.value.currentGameDay)
    }

    fun issueCompanyShares(company: CompanyRecord, newShares: Long, priceCents: Long) = runAction("Issuing Equity") {
        val name = snapshot.value.activeCharacter?.fullName ?: "Citizen Investor"
        companyMarketRepo.executeIpoOrIssuance(company, newShares, priceCents, name, snapshot.value.currentGameDay)
    }

    fun buildCompanyFacility(company: CompanyRecord, specId: String, initialNpc: Int) = runAction("Constructing Facility") {
        val spec = SectorFacilityCatalog.findSpec(specId)
            ?: return@runAction Result.failure(IllegalArgumentException("Unknown facility type."))
        companyMarketRepo.buildFacility(company, spec, initialNpc, snapshot.value.currentGameDay)
            .map { f -> "Constructed ${f.facilityName} (${f.npcEmployees}/${f.maxWorkforce} NPCs)" }
    }

    fun adjustFacilityNpc(facility: CompanyFacility, npcCount: Int) = runAction("Updating NPC Workforce") {
        companyMarketRepo.updateNpcStaffing(facility, npcCount).map { "Staff updated to $npcCount/${facility.maxWorkforce} NPCs." }
    }

    fun runFacilityProduction(company: CompanyRecord, facility: CompanyFacility) = runAction("Running Production Cycle") {
        companyMarketRepo.runProductionCycle(company, facility, snapshot.value.currentGameDay)
    }

    fun placeMarketOrder(
        countyId: Long,
        commodity: String,
        side: String,
        quantity: Long,
        priceCents: Long,
        useCompanyEntity: Boolean,
    ) = runAction("Posting TSN Order") {
        val company = snapshot.value.selectedCompany
        val entityId = if (useCompanyEntity && company != null) company.companyId.toString() else uid
        val entityName = if (useCompanyEntity && company != null) company.name else (snapshot.value.activeCharacter?.fullName ?: "Citizen")
        val acctId = if (useCompanyEntity && company != null) company.corporateAccountId else (snapshot.value.personalAccount?.docId ?: "acct_${uid}_personal")
        val cat = if (useCompanyEntity && company != null) "COMPANY" else "PERSONAL"
        companyMarketRepo.placeMarketOrder(
            entityId = entityId,
            entityName = entityName,
            entityBankAccountId = acctId,
            holderCategory = cat,
            countyId = countyId,
            commodityName = commodity,
            orderSide = side,
            quantity = quantity,
            pricePerUnitCents = priceCents,
            currentGameDay = snapshot.value.currentGameDay,
        ).map { "Posted $side order: $quantity $commodity @ ${FinancialEngines.formatCurrency(priceCents)}" }
    }

    fun fillSellOrder(order: MarketOrder, qty: Long, countryId: Long, useCompany: Boolean) = runAction("Settling TSN Trade") {
        val company = snapshot.value.selectedCompany
        val buyerId = if (useCompany && company != null) company.companyId.toString() else uid
        val buyerAcct = if (useCompany && company != null) company.corporateAccountId else (snapshot.value.personalAccount?.docId ?: "acct_${uid}_personal")
        val cat = if (useCompany && company != null) "COMPANY" else "PERSONAL"
        val natGov = snapshot.value.governments.firstOrNull { it.docId == "gov_country_$countryId" }
        companyMarketRepo.fillSellOrder(
            order = order,
            buyerEntityId = buyerId,
            buyerBankAccountId = buyerAcct,
            buyerHolderCategory = cat,
            quantityToBuy = qty,
            salesTaxPct = natGov?.salesTaxRatePct ?: 7.5,
            countryId = countryId,
            currentGameDay = snapshot.value.currentGameDay,
        )
    }

    fun executeGovSpend(gov: GovernmentRecord, dept: String, purpose: String, amountCents: Long) =
        runAction("Disbursing Public Budget") {
            val recipientAcct = snapshot.value.selectedCompany?.corporateAccountId
                ?: snapshot.value.personalAccount?.docId
                ?: return@runAction Result.failure(IllegalStateException("No recipient account ready."))
            val recipientName = snapshot.value.selectedCompany?.name
                ?: snapshot.value.activeCharacter?.fullName
                ?: "Citizen Contractor"
            govPoliticsRepo.executeGovernmentExpenditure(
                gov = gov,
                department = dept,
                recipientAccountId = recipientAcct,
                recipientName = recipientName,
                purpose = purpose,
                amountCents = amountCents,
                approverName = snapshot.value.activeCharacter?.fullName ?: "Treasury Auditor",
                currentGameDay = snapshot.value.currentGameDay,
            )
        }

    fun updateGovTaxRates(gov: GovernmentRecord, primaryPct: Double, corpPct: Double, salesPct: Double) =
        runAction("Updating Tax Policy") {
            govPoliticsRepo.updateTaxPolicy(gov, primaryPct, corpPct, salesPct, snapshot.value.currentGameDay)
                .map { "Updated ${gov.jurisdictionName} tax rates." }
        }

    fun createParty(name: String, abbr: String, countryId: Long, manifesto: String) = runAction("Registering Political Party") {
        val leader = snapshot.value.activeCharacter?.fullName ?: "Citizen Founder"
        govPoliticsRepo.createPoliticalParty(name, abbr, countryId, leader, manifesto)
            .map { p -> "Party #${p.partyId} (${p.abbreviation}) registered at ${p.canonicalUrl()}" }
    }

    fun launchCampaign(system: String, office: String, city: String, countryId: Long, stateId: Long, currency: String, pitch: String) =
        runAction("Launching $system Campaign") {
            val cand = snapshot.value.activeCharacter?.fullName ?: "Citizen Candidate"
            govPoliticsRepo.launchCampaign(system, cand, office, city, countryId, stateId, currency, pitch)
                .map { c -> "Launched ${c.campaignSystem} Campaign with segregated account ${c.campaignAccountId}" }
        }

    fun donateToCampaign(campaign: CampaignRecord, amountCents: Long) = runAction("Donating to Campaign") {
        val personal = snapshot.value.personalAccount?.docId
            ?: return@runAction Result.failure(IllegalStateException("Personal account unavailable."))
        govPoliticsRepo.donateToCampaign(campaign, personal, amountCents, snapshot.value.currentGameDay)
            .map { "Contributed ${FinancialEngines.formatCurrency(amountCents)} to ${campaign.campaignSystem} campaign." }
    }

    fun spendCampaignBudget(campaign: CampaignRecord, countyId: Long, amountCents: Long, eventNote: String) =
        runAction("Recording Campaign Outreach") {
            govPoliticsRepo.recordCampaignOutreachSpend(
                campaign = campaign,
                countyTreasuryAccountId = "treasury_county_$countyId",
                amountCents = amountCents,
                eventDescription = eventNote,
                currentGameDay = snapshot.value.currentGameDay,
            ).map { "Recorded ${campaign.campaignSystem} outreach spend (${FinancialEngines.formatCurrency(amountCents)})." }
        }

    fun createElection(tier: String, jurisdictionId: Long, office: String, candidatesCsv: String) =
        runAction("Opening Election Ballot") {
            val list = candidatesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
            govPoliticsRepo.createElection(tier, jurisdictionId, office, list, snapshot.value.currentGameDay)
                .map { e -> "Election #${e.electionId} opened at ${e.canonicalUrl()}" }
        }

    fun castVote(election: ElectionRecord, candidate: String) = runAction("Casting Secure Ballot") {
        govPoliticsRepo.castElectionBallot(election, candidate).map { "Vote recorded for $candidate." }
    }

    fun disputeElection(electionId: Long, reason: String) = runAction("Filing Election Dispute") {
        govPoliticsRepo.fileElectionDispute(electionId, reason).map { "Election #$electionId marked DISPUTED for review." }
    }

    fun buyProperty(countyId: Long, type: String, title: String, valueCents: Long, monthlyLeaseCents: Long) =
        runAction("Acquiring Property Deed") {
            val personal = snapshot.value.personalAccount?.docId
                ?: return@runAction Result.failure(IllegalStateException("Personal account unavailable."))
            govPoliticsRepo.acquireProperty(countyId, personal, type, title, valueCents, monthlyLeaseCents, snapshot.value.currentGameDay)
                .map { p -> "Deed recorded: ${p.title}" }
        }

    fun voteReferendum(eraNumber: Int, choice: FinancialEngines.ReferendumChoice) = runAction("Casting Referendum Vote") {
        govPoliticsRepo.castReferendumVote(
            eraNumber = eraNumber,
            choice = choice,
            partialModules = if (choice == FinancialEngines.ReferendumChoice.PARTIAL_RESET) listOf("Companies", "Elections") else emptyList(),
            currentGameDay = snapshot.value.currentGameDay,
        ).map { tally ->
            "Vote recorded. Current outcome: ${tally.outcome.name} (${tally.validVotes} valid votes)."
        }
    }

    private companion object {
        const val TAG = "SimulationVM"
    }
}
