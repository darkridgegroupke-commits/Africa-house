package com.example.data.repository

import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.AuditLogRecord
import com.example.data.model.CampaignRecord
import com.example.data.model.ElectionRecord
import com.example.data.model.GovernmentExpenditure
import com.example.data.model.GovernmentRecord
import com.example.data.model.PoliticalParty
import com.example.data.model.PropertyAsset
import com.example.data.model.ReferendumRecord
import com.example.domain.FinancialEngines
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Updates 9, 10, 11 & 12: National/State/County Governments (14 departments & audited treasuries),
 * Political Parties, 3 segregated Campaign Systems (State/National/General), Multi-tier Elections
 * with single-ballot enforcement & disputes, Property Assets, Audit Logs, and 200-Day Era Referendum.
 */
class GovernancePoliticsAssetsRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Google Sign-In required.")

    fun observeGovernments(): Flow<List<GovernmentRecord>> = flow {
        val uid = requireUid()
        emitAll(
            db.collection("governments")
                .whereEqualTo("scope", "GOVERNMENT")
                .snapshots()
                .map { snap ->
                    snap.toObjects(GovernmentRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.jurisdictionId }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, "governments")
                    throw e
                },
        )
    }

    fun observeGovernmentExpenditures(): Flow<List<GovernmentExpenditure>> = flow {
        val uid = requireUid()
        emitAll(
            db.collection("government_expenditures")
                .whereEqualTo("scope", "GOV_SPEND")
                .snapshots()
                .map { snap ->
                    snap.toObjects(GovernmentExpenditure::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                        .take(30)
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, "government_expenditures")
                    throw e
                },
        )
    }

    fun observePoliticalParties(): Flow<List<PoliticalParty>> = flow {
        val uid = requireUid()
        emitAll(
            db.collection("political_parties")
                .whereEqualTo("scope", "PARTY")
                .snapshots()
                .map { snap ->
                    snap.toObjects(PoliticalParty::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.partyId }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, "political_parties")
                    throw e
                },
        )
    }

    fun observeCampaigns(): Flow<List<CampaignRecord>> = flow {
        val uid = requireUid()
        emitAll(
            db.collection("campaigns")
                .whereEqualTo("scope", "CAMPAIGN")
                .snapshots()
                .map { snap ->
                    snap.toObjects(CampaignRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, "campaigns")
                    throw e
                },
        )
    }

    fun observeElections(): Flow<List<ElectionRecord>> = flow {
        val uid = requireUid()
        emitAll(
            db.collection("elections")
                .whereEqualTo("scope", "ELECTION")
                .snapshots()
                .map { snap ->
                    snap.toObjects(ElectionRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.electionId }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, "elections")
                    throw e
                },
        )
    }

    fun observeMyProperties(uid: String): Flow<List<PropertyAsset>> = flow {
        val authed = requireUid()
        check(authed == uid)
        emitAll(
            db.collection("properties")
                .whereEqualTo("ownerUid", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(PropertyAsset::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == authed) handleFirestoreError(fe ?: e, OperationType.LIST, "properties")
                    throw e
                },
        )
    }

    fun observeAuditLogs(uid: String): Flow<List<AuditLogRecord>> = flow {
        val authed = requireUid()
        check(authed == uid)
        emitAll(
            db.collection("audit_logs")
                .whereEqualTo("actorUid", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(AuditLogRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                        .take(40)
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == authed) handleFirestoreError(fe ?: e, OperationType.LIST, "audit_logs")
                    throw e
                },
        )
    }

    fun observeReferendums(): Flow<List<ReferendumRecord>> = flow {
        val uid = requireUid()
        emitAll(
            db.collection("referendums")
                .whereEqualTo("scope", "REFERENDUM")
                .snapshots()
                .map { snap ->
                    snap.toObjects(ReferendumRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.eraNumber }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, "referendums")
                    throw e
                },
        )
    }

    suspend fun updateTaxPolicy(
        gov: GovernmentRecord,
        primaryRatePct: Double,
        secondaryRatePct: Double,
        salesRatePct: Double,
        currentGameDay: Double,
    ): Result<Unit> {
        return try {
            val uid = requireUid()
            db.collection("governments").document(gov.docId).update(
                mapOf(
                    "primaryTaxRatePct" to primaryRatePct.coerceIn(0.0, 50.0),
                    "secondaryTaxRatePct" to secondaryRatePct.coerceIn(0.0, 50.0),
                    "salesTaxRatePct" to salesRatePct.coerceIn(0.0, 35.0),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            db.collection("audit_logs").document().set(
                mapOf(
                    "actorUid" to uid,
                    "actionType" to "TAX_POLICY_UPDATE",
                    "entityRef" to gov.docId,
                    "detail" to "${gov.jurisdictionName}: Primary $primaryRatePct%, Corp $secondaryRatePct%, Sales $salesRatePct%",
                    "gameDay" to currentGameDay,
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "governments/${gov.docId}")
            Result.failure(e)
        }
    }

    /**
     * Update 9: Audited government treasury disbursement to a recipient account (never invents money).
     */
    suspend fun executeGovernmentExpenditure(
        gov: GovernmentRecord,
        department: String,
        recipientAccountId: String,
        recipientName: String,
        purpose: String,
        amountCents: Long,
        approverName: String,
        currentGameDay: Double,
    ): Result<String> {
        return try {
            val uid = requireUid()
            if (amountCents <= 0L) return Result.failure(IllegalArgumentException("Expenditure amount must be positive."))

            val treasuryRef = db.collection("bank_accounts").document(gov.treasuryAccountId)
            val recipientRef = db.collection("bank_accounts").document(recipientAccountId)
            val expRef = db.collection("government_expenditures").document()
            val ledgerRef = db.collection("ledger_transactions").document()
            val txRef = "GOV-${UUID.randomUUID().toString().take(8).uppercase()}"

            val msg = db.runTransaction { tx ->
                val tSnap = tx.get(treasuryRef)
                val rSnap = tx.get(recipientRef)
                val tBal = tSnap.getLong("balanceCents") ?: 0L
                val rBal = rSnap.getLong("balanceCents") ?: 0L
                if (!tSnap.exists() || tBal < amountCents) {
                    throw IllegalStateException("Treasury ${gov.treasuryAccountId} has insufficient funds.")
                }
                if (!rSnap.exists()) throw IllegalStateException("Recipient account does not exist.")

                tx.update(treasuryRef, mapOf("balanceCents" to (tBal - amountCents), "updatedAt" to FieldValue.serverTimestamp()))
                tx.update(recipientRef, mapOf("balanceCents" to (rBal + amountCents), "updatedAt" to FieldValue.serverTimestamp()))
                tx.set(
                    expRef,
                    mapOf(
                        "scope" to "GOV_SPEND",
                        "govId" to gov.docId,
                        "approvedByUid" to uid,
                        "approvedByName" to approverName.take(80),
                        "department" to department.take(40),
                        "recipientName" to recipientName.take(80),
                        "purpose" to purpose.trim().ifBlank { "$department Public Works" }.take(200),
                        "amountCents" to amountCents,
                        "txRef" to txRef,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.set(
                    ledgerRef,
                    mapOf(
                        "txRef" to txRef,
                        "initiatedByUid" to uid,
                        "senderAccountId" to gov.treasuryAccountId,
                        "receiverAccountId" to recipientAccountId,
                        "amountCents" to amountCents,
                        "taxAmountCents" to 0L,
                        "taxTreasuryAccountId" to "",
                        "txType" to "GOVERNMENT_EXPENDITURE",
                        "gameDay" to currentGameDay,
                        "reference" to "[$department] $purpose -> $recipientName".take(200),
                        "status" to "SETTLED",
                        "publicAudit" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                "Disbursed ${FinancialEngines.formatCurrency(amountCents)} via $department ($txRef)."
            }.await()

            Result.success(msg)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "government_expenditures")
            Result.failure(e)
        }
    }

    /**
     * Update 10: Establish a political party with permanent ID >= 601.
     */
    suspend fun createPoliticalParty(
        name: String,
        abbreviation: String,
        countryId: Long,
        leaderName: String,
        manifestoSummary: String,
    ): Result<PoliticalParty> {
        return try {
            val uid = requireUid()
            val cleanName = name.trim()
            if (cleanName.length < 2) return Result.failure(IllegalArgumentException("Party name too short."))
            val counterRef = db.collection("system_counters").document("party_seq")

            val created = db.runTransaction { tx ->
                val snap = tx.get(counterRef)
                val partyId = if (!snap.exists()) {
                    tx.set(counterRef, mapOf("nextId" to 602L))
                    601L
                } else {
                    val cur = snap.getLong("nextId") ?: 601L
                    tx.update(counterRef, mapOf("nextId" to cur + 1L))
                    cur
                }
                val partyRef = db.collection("political_parties").document(partyId.toString())
                tx.set(
                    partyRef,
                    mapOf(
                        "scope" to "PARTY",
                        "partyId" to partyId,
                        "leaderUid" to uid,
                        "leaderName" to leaderName.take(80),
                        "countryId" to countryId,
                        "name" to cleanName.take(80),
                        "abbreviation" to abbreviation.trim().uppercase().take(8),
                        "manifestoSummary" to manifestoSummary.trim().ifBlank { "Continental prosperity & good governance" }.take(240),
                        "emblemStyle" to "GOLD_STAR",
                        "memberCount" to 1L,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                PoliticalParty(
                    docId = partyId.toString(),
                    partyId = partyId,
                    leaderUid = uid,
                    leaderName = leaderName,
                    countryId = countryId,
                    name = cleanName,
                    abbreviation = abbreviation.trim().uppercase(),
                    manifestoSummary = manifestoSummary,
                )
            }.await()
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "political_parties")
            Result.failure(e)
        }
    }

    /**
     * Update 10: Launch one of the 3 strictly separated Campaign Systems (STATE, NATIONAL, GENERAL),
     * each provisioned with its own dedicated ring-fenced CAMPAIGN bank account.
     */
    suspend fun launchCampaign(
        campaignSystem: String, // STATE, NATIONAL, GENERAL
        candidateName: String,
        officeTitle: String,
        headquartersCity: String,
        countryId: Long,
        stateId: Long,
        currencyCode: String,
        manifestoPitch: String,
    ): Result<CampaignRecord> {
        return try {
            val uid = requireUid()
            val campaignId = "cmp_${campaignSystem.lowercase()}_${uid.take(8)}_${System.currentTimeMillis() % 10000}"
            val cmpAcctId = "acct_$campaignId"
            val cmpAcctRef = db.collection("bank_accounts").document(cmpAcctId)
            val cmpRef = db.collection("campaigns").document(campaignId)

            val batch = db.batch()
            batch.set(
                cmpAcctRef,
                mapOf(
                    "ownerUid" to uid,
                    "ownerEntityId" to campaignId,
                    "accountType" to "CAMPAIGN",
                    "accountCategory" to "CAMPAIGN",
                    "currencyCode" to currencyCode,
                    "balanceCents" to 0L,
                    "restricted" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
            batch.set(
                cmpRef,
                mapOf(
                    "scope" to "CAMPAIGN",
                    "ownerUid" to uid,
                    "candidateName" to candidateName.take(80),
                    "campaignSystem" to campaignSystem,
                    "countryId" to countryId,
                    "stateId" to stateId,
                    "officeTitle" to officeTitle.trim().ifBlank { "$campaignSystem Candidate" }.take(80),
                    "headquartersCity" to headquartersCity.trim().ifBlank { "Capital" }.take(60),
                    "campaignAccountId" to cmpAcctId,
                    "donationsTotalCents" to 0L,
                    "expensesTotalCents" to 0L,
                    "outreachEventsCount" to 0,
                    "manifestoPitch" to manifestoPitch.trim().take(240),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
            batch.commit().await()
            Result.success(
                CampaignRecord(
                    docId = campaignId,
                    ownerUid = uid,
                    candidateName = candidateName,
                    campaignSystem = campaignSystem,
                    countryId = countryId,
                    stateId = stateId,
                    officeTitle = officeTitle,
                    headquartersCity = headquartersCity,
                    campaignAccountId = cmpAcctId,
                    manifestoPitch = manifestoPitch,
                ),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "campaigns")
            Result.failure(e)
        }
    }

    suspend fun donateToCampaign(
        campaign: CampaignRecord,
        donorPersonalAccountId: String,
        amountCents: Long,
        currentGameDay: Double,
    ): Result<Unit> {
        return try {
            val uid = requireUid()
            if (amountCents <= 0L) return Result.failure(IllegalArgumentException("Donation must be positive."))

            val donorRef = db.collection("bank_accounts").document(donorPersonalAccountId)
            val cmpAcctRef = db.collection("bank_accounts").document(campaign.campaignAccountId)
            val cmpRef = db.collection("campaigns").document(campaign.docId)
            val ledgerRef = db.collection("ledger_transactions").document()

            db.runTransaction { tx ->
                val dSnap = tx.get(donorRef)
                val cSnap = tx.get(cmpAcctRef)
                val dBal = dSnap.getLong("balanceCents") ?: 0L
                val cBal = cSnap.getLong("balanceCents") ?: 0L
                if (dBal < amountCents) throw IllegalStateException("Insufficient balance for campaign contribution.")

                tx.update(donorRef, mapOf("balanceCents" to (dBal - amountCents), "updatedAt" to FieldValue.serverTimestamp()))
                tx.update(cmpAcctRef, mapOf("balanceCents" to (cBal + amountCents), "updatedAt" to FieldValue.serverTimestamp()))
                tx.update(
                    cmpRef,
                    mapOf(
                        "donationsTotalCents" to (campaign.donationsTotalCents + amountCents),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.set(
                    ledgerRef,
                    mapOf(
                        "txRef" to "CMP-${UUID.randomUUID().toString().take(8).uppercase()}",
                        "initiatedByUid" to uid,
                        "senderAccountId" to donorPersonalAccountId,
                        "receiverAccountId" to campaign.campaignAccountId,
                        "amountCents" to amountCents,
                        "taxAmountCents" to 0L,
                        "taxTreasuryAccountId" to "",
                        "txType" to "CAMPAIGN_DONATION",
                        "gameDay" to currentGameDay,
                        "reference" to "${campaign.campaignSystem} Campaign Donation -> ${campaign.candidateName}",
                        "status" to "SETTLED",
                        "publicAudit" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "campaigns/${campaign.docId}")
            Result.failure(e)
        }
    }

    suspend fun recordCampaignOutreachSpend(
        campaign: CampaignRecord,
        countyTreasuryAccountId: String,
        amountCents: Long,
        eventDescription: String,
        currentGameDay: Double,
    ): Result<Unit> {
        return try {
            val uid = requireUid()
            check(campaign.ownerUid == uid)
            if (amountCents <= 0L) return Result.failure(IllegalArgumentException("Outreach spend must be positive."))

            val cmpAcctRef = db.collection("bank_accounts").document(campaign.campaignAccountId)
            val treasuryRef = db.collection("bank_accounts").document(countyTreasuryAccountId)
            val cmpRef = db.collection("campaigns").document(campaign.docId)
            val ledgerRef = db.collection("ledger_transactions").document()

            db.runTransaction { tx ->
                val cSnap = tx.get(cmpAcctRef)
                val tSnap = tx.get(treasuryRef)
                val cBal = cSnap.getLong("balanceCents") ?: 0L
                val tBal = tSnap.getLong("balanceCents") ?: 0L
                if (cBal < amountCents) throw IllegalStateException("Campaign budget insufficient for outreach spend.")

                tx.update(cmpAcctRef, mapOf("balanceCents" to (cBal - amountCents), "updatedAt" to FieldValue.serverTimestamp()))
                if (tSnap.exists()) {
                    tx.update(treasuryRef, mapOf("balanceCents" to (tBal + amountCents), "updatedAt" to FieldValue.serverTimestamp()))
                }
                tx.update(
                    cmpRef,
                    mapOf(
                        "expensesTotalCents" to (campaign.expensesTotalCents + amountCents),
                        "outreachEventsCount" to (campaign.outreachEventsCount + 1),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.set(
                    ledgerRef,
                    mapOf(
                        "txRef" to "OUT-${UUID.randomUUID().toString().take(8).uppercase()}",
                        "initiatedByUid" to uid,
                        "senderAccountId" to campaign.campaignAccountId,
                        "receiverAccountId" to countyTreasuryAccountId,
                        "amountCents" to amountCents,
                        "taxAmountCents" to 0L,
                        "taxTreasuryAccountId" to "",
                        "txType" to "CAMPAIGN_EXPENSE",
                        "gameDay" to currentGameDay,
                        "reference" to "[${campaign.campaignSystem}] $eventDescription".take(200),
                        "status" to "SETTLED",
                        "publicAudit" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "campaigns/${campaign.docId}")
            Result.failure(e)
        }
    }

    /**
     * Update 10: Elections with single-ballot-per-citizen security & dispute filing.
     */
    suspend fun createElection(
        electionTier: String,
        jurisdictionId: Long,
        officeTitle: String,
        candidates: List<String>,
        currentGameDay: Double,
    ): Result<ElectionRecord> {
        return try {
            requireUid()
            val counterRef = db.collection("system_counters").document("election_seq")
            val created = db.runTransaction { tx ->
                val snap = tx.get(counterRef)
                val electionId = if (!snap.exists()) {
                    tx.set(counterRef, mapOf("nextId" to 802L))
                    801L
                } else {
                    val cur = snap.getLong("nextId") ?: 801L
                    tx.update(counterRef, mapOf("nextId" to cur + 1L))
                    cur
                }
                val cleanCandidates = candidates.map { it.trim() }.filter { it.isNotBlank() }.distinct()
                val initialCounts = cleanCandidates.associateWith { 0L }
                val elRef = db.collection("elections").document(electionId.toString())
                tx.set(
                    elRef,
                    mapOf(
                        "scope" to "ELECTION",
                        "electionId" to electionId,
                        "electionTier" to electionTier,
                        "jurisdictionId" to jurisdictionId,
                        "officeTitle" to officeTitle.trim().ifBlank { "Governor" }.take(80),
                        "electionStatus" to "OPEN",
                        "candidates" to cleanCandidates,
                        "voteCounts" to initialCounts,
                        "disputeNotes" to "",
                        "closesAtGameDay" to (currentGameDay + 90.0),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                ElectionRecord(
                    docId = electionId.toString(),
                    electionId = electionId,
                    electionTier = electionTier,
                    jurisdictionId = jurisdictionId,
                    officeTitle = officeTitle,
                    candidates = cleanCandidates,
                    voteCounts = initialCounts,
                    closesAtGameDay = currentGameDay + 90.0,
                )
            }.await()
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "elections")
            Result.failure(e)
        }
    }

    suspend fun castElectionBallot(election: ElectionRecord, candidateChoice: String): Result<Unit> {
        return try {
            val uid = requireUid()
            val elRef = db.collection("elections").document(election.electionId.toString())
            val ballotRef = elRef.collection("ballots").document(uid)

            db.runTransaction { tx ->
                val ballotSnap = tx.get(ballotRef)
                if (ballotSnap.exists()) throw IllegalStateException("Ballot already cast for election #${election.electionId}.")
                val elSnap = tx.get(elRef)
                @Suppress("UNCHECKED_CAST")
                val existingCounts = (elSnap.get("voteCounts") as? Map<String, Long>)?.toMutableMap() ?: mutableMapOf()
                existingCounts[candidateChoice] = (existingCounts[candidateChoice] ?: 0L) + 1L

                tx.set(
                    ballotRef,
                    mapOf(
                        "voterUid" to uid,
                        "electionId" to election.electionId,
                        "candidateChoice" to candidateChoice,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.update(
                    elRef,
                    mapOf("voteCounts" to existingCounts, "updatedAt" to FieldValue.serverTimestamp()),
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "elections/${election.electionId}/ballots")
            Result.failure(e)
        }
    }

    suspend fun fileElectionDispute(electionId: Long, reason: String): Result<Unit> {
        return try {
            requireUid()
            db.collection("elections").document(electionId.toString()).update(
                mapOf(
                    "electionStatus" to "DISPUTED",
                    "disputeNotes" to reason.trim().take(200),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "elections/$electionId")
            Result.failure(e)
        }
    }

    /**
     * Update 11: Acquire Property (Land, Residential, Commercial, Vehicle) paying County Treasury.
     */
    suspend fun acquireProperty(
        countyId: Long,
        buyerAccountId: String,
        propertyType: String,
        title: String,
        valueCents: Long,
        monthlyLeaseCents: Long,
        currentGameDay: Double,
    ): Result<PropertyAsset> {
        return try {
            val uid = requireUid()
            val buyerRef = db.collection("bank_accounts").document(buyerAccountId)
            val ctTreasuryRef = db.collection("bank_accounts").document("treasury_county_$countyId")
            val propRef = db.collection("properties").document()
            val ledgerRef = db.collection("ledger_transactions").document()

            val created = db.runTransaction { tx ->
                val bSnap = tx.get(buyerRef)
                val ctSnap = tx.get(ctTreasuryRef)
                val bBal = bSnap.getLong("balanceCents") ?: 0L
                val ctBal = ctSnap.getLong("balanceCents") ?: 0L
                if (bBal < valueCents) throw IllegalStateException("Insufficient funds to acquire $title.")

                tx.update(buyerRef, mapOf("balanceCents" to (bBal - valueCents), "updatedAt" to FieldValue.serverTimestamp()))
                if (ctSnap.exists()) {
                    tx.update(ctTreasuryRef, mapOf("balanceCents" to (ctBal + valueCents), "updatedAt" to FieldValue.serverTimestamp()))
                }
                tx.set(
                    propRef,
                    mapOf(
                        "ownerUid" to uid,
                        "ownerEntityId" to uid,
                        "countyId" to countyId,
                        "propertyType" to propertyType,
                        "title" to title.trim().ifBlank { "$propertyType Deed" }.take(80),
                        "valueCents" to valueCents,
                        "monthlyLeaseCents" to monthlyLeaseCents.coerceAtLeast(0L),
                        "maintenanceCostCents" to (valueCents / 100L),
                        "propertyTaxPaidCents" to 0L,
                        "forSale" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.set(
                    ledgerRef,
                    mapOf(
                        "txRef" to "PRP-${UUID.randomUUID().toString().take(8).uppercase()}",
                        "initiatedByUid" to uid,
                        "senderAccountId" to buyerAccountId,
                        "receiverAccountId" to ctTreasuryRef.id,
                        "amountCents" to valueCents,
                        "taxAmountCents" to 0L,
                        "taxTreasuryAccountId" to "",
                        "txType" to "PROPERTY_ACQUISITION",
                        "gameDay" to currentGameDay,
                        "reference" to "Property Deed: $title in /county/$countyId",
                        "status" to "SETTLED",
                        "publicAudit" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                PropertyAsset(
                    docId = propRef.id,
                    ownerUid = uid,
                    ownerEntityId = uid,
                    countyId = countyId,
                    propertyType = propertyType,
                    title = title,
                    valueCents = valueCents,
                    monthlyLeaseCents = monthlyLeaseCents,
                    maintenanceCostCents = valueCents / 100L,
                )
            }.await()
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "properties")
            Result.failure(e)
        }
    }

    /**
     * Update 12: 200-Day / 100-In-Game-Year Era Referendum & Legal Payment Compliance Retention.
     */
    suspend fun ensureActiveReferendum(eraNumber: Int): Result<Unit> {
        return try {
            requireUid()
            val ref = db.collection("referendums").document("era_$eraNumber")
            if (!ref.get().await().exists()) {
                ref.set(
                    mapOf(
                        "scope" to "REFERENDUM",
                        "eraNumber" to eraNumber,
                        "status" to "ACTIVE",
                        "continueVotes" to 0L,
                        "partialResetVotes" to 0L,
                        "fullResetVotes" to 0L,
                        "abstainVotes" to 0L,
                        "partialResetModules" to emptyList<String>(),
                        "outcome" to "CONTINUE_ERA",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "referendums/era_$eraNumber")
            Result.failure(e)
        }
    }

    suspend fun castReferendumVote(
        eraNumber: Int,
        choice: FinancialEngines.ReferendumChoice,
        partialModules: List<String> = emptyList(),
        currentGameDay: Double,
    ): Result<FinancialEngines.ReferendumTally> {
        return try {
            val uid = requireUid()
            val refDoc = db.collection("referendums").document("era_$eraNumber")
            val voteDoc = refDoc.collection("votes").document(uid)
            val retentionDoc = db.collection("legal_retention_records").document()

            val tally = db.runTransaction { tx ->
                if (tx.get(voteDoc).exists()) {
                    throw IllegalStateException("You have already cast your referendum vote for Era $eraNumber.")
                }
                val snap = tx.get(refDoc)
                var cont = snap.getLong("continueVotes") ?: 0L
                var part = snap.getLong("partialResetVotes") ?: 0L
                var full = snap.getLong("fullResetVotes") ?: 0L
                var abs = snap.getLong("abstainVotes") ?: 0L

                when (choice) {
                    FinancialEngines.ReferendumChoice.CONTINUE_ERA -> cont++
                    FinancialEngines.ReferendumChoice.PARTIAL_RESET -> part++
                    FinancialEngines.ReferendumChoice.FULL_RESET -> full++
                    FinancialEngines.ReferendumChoice.ABSTAIN -> abs++
                }
                val evaluated = FinancialEngines.evaluateReferendum(cont, part, full, abs)

                tx.set(
                    voteDoc,
                    mapOf(
                        "voterUid" to uid,
                        "eraNumber" to eraNumber,
                        "choice" to choice.name,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.update(
                    refDoc,
                    mapOf(
                        "continueVotes" to cont,
                        "partialResetVotes" to part,
                        "fullResetVotes" to full,
                        "abstainVotes" to abs,
                        "partialResetModules" to partialModules.take(8),
                        "outcome" to evaluated.outcome.name,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.set(
                    retentionDoc,
                    mapOf(
                        "ownerUid" to uid,
                        "eraNumber" to eraNumber,
                        "recordType" to "ERA_COMPLIANCE_ARCHIVE",
                        "summary" to "Legal payment & referendum ballot retained independently of era resets (Game Day ${currentGameDay.toLong()}).",
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                evaluated
            }.await()
            Result.success(tally)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "referendums/era_$eraNumber")
            Result.failure(e)
        }
    }
}
