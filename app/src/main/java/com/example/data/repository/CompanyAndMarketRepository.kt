package com.example.data.repository

import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.BoardResolution
import com.example.data.model.CompanyFacility
import com.example.data.model.CompanyRecord
import com.example.data.model.InventoryItem
import com.example.data.model.MarketOrder
import com.example.data.model.ShareholderHolding
import com.example.data.model.TradeExecution
import com.example.data.seed.SectorFacilityCatalog
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
 * Updates 6, 7 & 8: Company Registration (1,000,000 initial shares @ 10 units), CEO 5% rule,
 * 2-for-1 splits, IPO/share issuance, Sector Facilities (NPC-only capacity limits, input-driven
 * production into inventory only), and TSN County Marketplace with anti-wash & escrow protections.
 */
class CompanyAndMarketRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Google Sign-In required.")

    fun observeCompanies(): Flow<List<CompanyRecord>> = flow {
        val uid = requireUid()
        emitAll(
            db.collection("companies")
                .whereEqualTo("scope", "COMPANY")
                .snapshots()
                .map { snap ->
                    snap.toObjects(CompanyRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.companyId }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, "companies")
                    throw e
                },
        )
    }

    fun observeShareholders(companyId: Long): Flow<List<ShareholderHolding>> = flow {
        val uid = requireUid()
        val path = "companies/$companyId/shareholders"
        emitAll(
            db.collection("companies").document(companyId.toString()).collection("shareholders")
                .whereEqualTo("companyId", companyId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(ShareholderHolding::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.sharesHeld }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, path)
                    throw e
                },
        )
    }

    fun observeFacilities(companyId: Long): Flow<List<CompanyFacility>> = flow {
        val uid = requireUid()
        val path = "companies/$companyId/facilities"
        emitAll(
            db.collection("companies").document(companyId.toString()).collection("facilities")
                .whereEqualTo("companyId", companyId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(CompanyFacility::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, path)
                    throw e
                },
        )
    }

    fun observeBoardResolutions(companyId: Long): Flow<List<BoardResolution>> = flow {
        val uid = requireUid()
        val path = "companies/$companyId/board_resolutions"
        emitAll(
            db.collection("companies").document(companyId.toString()).collection("board_resolutions")
                .whereEqualTo("companyId", companyId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(BoardResolution::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, path)
                    throw e
                },
        )
    }

    fun observeMarketOrders(): Flow<List<MarketOrder>> = flow {
        val uid = requireUid()
        emitAll(
            db.collection("market_orders")
                .whereEqualTo("scope", "TSN_MARKET")
                .snapshots()
                .map { snap ->
                    snap.toObjects(MarketOrder::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, "market_orders")
                    throw e
                },
        )
    }

    fun observeMyInventory(uid: String): Flow<List<InventoryItem>> = flow {
        val authed = requireUid()
        check(authed == uid)
        emitAll(
            db.collection("inventories")
                .whereEqualTo("ownerUid", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(InventoryItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .filter { it.quantity > 0L }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == authed) handleFirestoreError(fe ?: e, OperationType.LIST, "inventories")
                    throw e
                },
        )
    }

    fun observeTradeHistory(): Flow<List<TradeExecution>> = flow {
        val uid = requireUid()
        emitAll(
            db.collection("trade_history")
                .whereEqualTo("scope", "TSN_TRADE")
                .snapshots()
                .map { snap ->
                    snap.toObjects(TradeExecution::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                        .take(30)
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, "trade_history")
                    throw e
                },
        )
    }

    /**
     * Update 6: Registers Private, Public, or Government-Owned company with 1,000,000 initial shares @ 10.00.
     * Debits initial paid-in capital from founder's Capital account (if funded) into the Corporate bank account,
     * and allocates registration fee to National Treasury.
     */
    suspend fun registerCompany(
        companyName: String,
        sector: String,
        ownershipType: String, // PRIVATE, PUBLIC, GOVERNMENT_OWNED
        founderPlayerId: Long,
        founderName: String,
        countryId: Long,
        stateId: Long,
        countyId: Long,
        currencyCode: String,
        initialCapitalContributionCents: Long,
        currentGameDay: Double,
    ): Result<CompanyRecord> {
        return try {
            val uid = requireUid()
            val cleanName = companyName.trim()
            if (cleanName.length < 2) {
                return Result.failure(IllegalArgumentException("Company name must be at least 2 characters."))
            }
            val registrationFeeCents = 25_000L // 250.00 currency units
            val capitalAcctRef = db.collection("bank_accounts").document("acct_${uid}_capital")
            val natTreasuryRef = db.collection("bank_accounts").document("treasury_country_$countryId")
            val counterRef = db.collection("system_counters").document("company_seq")

            val created = db.runTransaction { tx ->
                // All reads first
                val counterSnap = tx.get(counterRef)
                val capitalSnap = tx.get(capitalAcctRef)
                val treasurySnap = tx.get(natTreasuryRef)

                val capitalBal = capitalSnap.getLong("balanceCents") ?: 0L
                val requiredFromFounder = registrationFeeCents + initialCapitalContributionCents.coerceAtLeast(0L)
                if (capitalBal < requiredFromFounder) {
                    throw IllegalStateException(
                        "Insufficient Capital Funds (need ${FinancialEngines.formatCurrency(requiredFromFounder)}, have ${FinancialEngines.formatCurrency(capitalBal)}).",
                    )
                }

                val allocatedId = if (!counterSnap.exists()) {
                    tx.set(counterRef, mapOf("nextId" to 502L))
                    501L
                } else {
                    val currentNext = counterSnap.getLong("nextId") ?: 501L
                    tx.update(counterRef, mapOf("nextId" to currentNext + 1L))
                    currentNext
                }

                val corpAcctId = "acct_corp_$allocatedId"
                val corpAcctRef = db.collection("bank_accounts").document(corpAcctId)
                val companyRef = db.collection("companies").document(allocatedId.toString())
                val holderRef = companyRef.collection("shareholders").document(uid)
                val resRef = companyRef.collection("board_resolutions").document()
                val certNo = "CERT-HOA-$countryId-$allocatedId"
                val isPublic = ownershipType == "PUBLIC"

                // Debit Capital Account
                tx.update(
                    capitalAcctRef,
                    mapOf("balanceCents" to (capitalBal - requiredFromFounder), "updatedAt" to FieldValue.serverTimestamp()),
                )
                // Credit National Treasury with registration fee
                val tBal = treasurySnap.getLong("balanceCents") ?: 0L
                if (treasurySnap.exists()) {
                    tx.update(
                        natTreasuryRef,
                        mapOf("balanceCents" to (tBal + registrationFeeCents), "updatedAt" to FieldValue.serverTimestamp()),
                    )
                }
                // Create Corporate Account with initial capital contribution
                tx.set(
                    corpAcctRef,
                    mapOf(
                        "ownerUid" to uid,
                        "ownerEntityId" to allocatedId.toString(),
                        "accountType" to "CORPORATE",
                        "accountCategory" to "CORPORATE",
                        "currencyCode" to currencyCode,
                        "balanceCents" to initialCapitalContributionCents.coerceAtLeast(0L),
                        "restricted" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )

                tx.set(
                    companyRef,
                    mapOf(
                        "scope" to "COMPANY",
                        "companyId" to allocatedId,
                        "ownerUid" to uid,
                        "ceoUid" to uid,
                        "ceoPlayerId" to founderPlayerId,
                        "ceoName" to founderName.take(80),
                        "name" to cleanName.take(80),
                        "sector" to sector.take(40),
                        "ownershipType" to ownershipType,
                        "countryId" to countryId,
                        "stateId" to stateId,
                        "countyId" to countyId,
                        "currencyCode" to currencyCode,
                        "corporateAccountId" to corpAcctId,
                        "registrationCertNumber" to certNo,
                        "totalShares" to FinancialEngines.INITIAL_COMPANY_SHARES,
                        "sharePriceCents" to FinancialEngines.INITIAL_SHARE_PRICE_CENTS,
                        "listedPublic" to isPublic,
                        "revenueYtdCents" to 0L,
                        "operatingCostYtdCents" to 0L,
                        "corporateTaxPaidCents" to 0L,
                        "registeredAtGameDay" to currentGameDay,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )

                tx.set(
                    holderRef,
                    mapOf(
                        "companyId" to allocatedId,
                        "holderEntityId" to uid,
                        "holderUid" to uid,
                        "holderName" to founderName.take(80),
                        "sharesHeld" to FinancialEngines.INITIAL_COMPANY_SHARES,
                        "ceoEligible" to true,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )

                tx.set(
                    resRef,
                    mapOf(
                        "ownerUid" to uid,
                        "companyId" to allocatedId,
                        "resolutionType" to "INCORPORATION",
                        "title" to "Certificate $certNo Issued",
                        "summary" to "1,000,000 shares @ 10.00 $currencyCode issued; $founderName appointed CEO (100% ownership).",
                        "approvedByName" to founderName,
                        "gameDay" to currentGameDay,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )

                val feeLedgerRef = db.collection("ledger_transactions").document()
                tx.set(
                    feeLedgerRef,
                    mapOf(
                        "txRef" to "INC-${UUID.randomUUID().toString().take(8).uppercase()}",
                        "initiatedByUid" to uid,
                        "senderAccountId" to capitalAcctRef.id,
                        "receiverAccountId" to natTreasuryRef.id,
                        "amountCents" to registrationFeeCents,
                        "taxAmountCents" to 0L,
                        "taxTreasuryAccountId" to "",
                        "txType" to "COMPANY_REGISTRATION_FEE",
                        "gameDay" to currentGameDay,
                        "reference" to "Incorporation Fee for $cleanName ($certNo)",
                        "status" to "SETTLED",
                        "publicAudit" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )

                CompanyRecord(
                    docId = allocatedId.toString(),
                    companyId = allocatedId,
                    ownerUid = uid,
                    ceoUid = uid,
                    ceoPlayerId = founderPlayerId,
                    ceoName = founderName,
                    name = cleanName,
                    sector = sector,
                    ownershipType = ownershipType,
                    countryId = countryId,
                    stateId = stateId,
                    countyId = countyId,
                    currencyCode = currencyCode,
                    corporateAccountId = corpAcctId,
                    registrationCertNumber = certNo,
                    listedPublic = isPublic,
                    registeredAtGameDay = currentGameDay,
                )
            }.await()

            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "companies")
            Result.failure(e)
        }
    }

    /**
     * Update 6: 2-for-1 Share Split preserving valuation & ownership percentages without minting cash.
     */
    suspend fun executeTwoForOneSplit(company: CompanyRecord, currentGameDay: Double): Result<String> {
        return try {
            val uid = requireUid()
            check(company.ownerUid == uid || company.ceoUid == uid)
            val companyRef = db.collection("companies").document(company.companyId.toString())
            val holdersSnap = companyRef.collection("shareholders").whereEqualTo("companyId", company.companyId).get().await()
            val holders = holdersSnap.toObjects(ShareholderHolding::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val holdingsMap = holders.associate { it.docId to it.sharesHeld }
            val split = FinancialEngines.computeTwoForOneSplit(
                holdingsByEntity = holdingsMap,
                currentTotalShares = company.totalShares,
                currentSharePriceCents = company.sharePriceCents,
            )

            val batch = db.batch()
            batch.update(
                companyRef,
                mapOf(
                    "totalShares" to split.newTotalShares,
                    "sharePriceCents" to split.newSharePriceCents,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
            holders.forEach { holder ->
                val newQty = split.updatedHoldings[holder.docId] ?: (holder.sharesHeld * 2L)
                batch.update(
                    companyRef.collection("shareholders").document(holder.docId),
                    mapOf(
                        "sharesHeld" to newQty,
                        "ceoEligible" to FinancialEngines.meetsCeoMinimumOwnership(newQty, split.newTotalShares),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
            }
            batch.set(
                companyRef.collection("board_resolutions").document(),
                mapOf(
                    "ownerUid" to uid,
                    "companyId" to company.companyId,
                    "resolutionType" to "SHARE_SPLIT_2_FOR_1",
                    "title" to "2-for-1 Share Split Executed",
                    "summary" to "Shares doubled to ${split.newTotalShares}; share price adjusted to ${FinancialEngines.formatCurrency(split.newSharePriceCents, company.currencyCode)}; valuation preserved.",
                    "approvedByName" to company.ceoName,
                    "gameDay" to currentGameDay,
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            )
            batch.commit().await()
            Result.success("2-for-1 Split complete: ${split.newTotalShares} shares @ ${FinancialEngines.formatCurrency(split.newSharePriceCents, company.currencyCode)}")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "companies/${company.companyId}")
            Result.failure(e)
        }
    }

    /**
     * Update 6: Approved IPO / New Share Issuance raising real capital from Capital Funds into Corporate Account.
     */
    suspend fun executeIpoOrIssuance(
        company: CompanyRecord,
        newSharesIssued: Long,
        issuePriceCents: Long,
        subscriberName: String,
        currentGameDay: Double,
    ): Result<String> {
        return try {
            val uid = requireUid()
            val issuance = FinancialEngines.computeNewShareIssuance(company.totalShares, newSharesIssued, issuePriceCents)
            val capitalAcctRef = db.collection("bank_accounts").document("acct_${uid}_capital")
            val corpAcctRef = db.collection("bank_accounts").document(company.corporateAccountId)
            val companyRef = db.collection("companies").document(company.companyId.toString())
            val holderRef = companyRef.collection("shareholders").document(uid)
            val resRef = companyRef.collection("board_resolutions").document()
            val ledgerRef = db.collection("ledger_transactions").document()

            db.runTransaction { tx ->
                val capSnap = tx.get(capitalAcctRef)
                val corpSnap = tx.get(corpAcctRef)
                val holderSnap = tx.get(holderRef)
                val capBal = capSnap.getLong("balanceCents") ?: 0L
                val corpBal = corpSnap.getLong("balanceCents") ?: 0L
                if (capBal < issuance.capitalRaisedCents) {
                    throw IllegalStateException("Insufficient Capital Funds to subscribe ${FinancialEngines.formatCurrency(issuance.capitalRaisedCents)}.")
                }
                val prevShares = holderSnap.getLong("sharesHeld") ?: 0L
                val updatedShares = prevShares + newSharesIssued

                tx.update(capitalAcctRef, mapOf("balanceCents" to (capBal - issuance.capitalRaisedCents), "updatedAt" to FieldValue.serverTimestamp()))
                tx.update(corpAcctRef, mapOf("balanceCents" to (corpBal + issuance.capitalRaisedCents), "updatedAt" to FieldValue.serverTimestamp()))
                tx.update(
                    companyRef,
                    mapOf(
                        "totalShares" to issuance.newTotalShares,
                        "sharePriceCents" to issuePriceCents,
                        "listedPublic" to true,
                        "ownershipType" to "PUBLIC",
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                if (holderSnap.exists()) {
                    tx.update(
                        holderRef,
                        mapOf(
                            "sharesHeld" to updatedShares,
                            "ceoEligible" to FinancialEngines.meetsCeoMinimumOwnership(updatedShares, issuance.newTotalShares),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                } else {
                    tx.set(
                        holderRef,
                        mapOf(
                            "companyId" to company.companyId,
                            "holderEntityId" to uid,
                            "holderUid" to uid,
                            "holderName" to subscriberName,
                            "sharesHeld" to updatedShares,
                            "ceoEligible" to FinancialEngines.meetsCeoMinimumOwnership(updatedShares, issuance.newTotalShares),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                }
                tx.set(
                    resRef,
                    mapOf(
                        "ownerUid" to uid,
                        "companyId" to company.companyId,
                        "resolutionType" to "IPO_OR_ISSUANCE",
                        "title" to "Approved Issuance (+$newSharesIssued Shares)",
                        "summary" to "Raised ${FinancialEngines.formatCurrency(issuance.capitalRaisedCents, company.currencyCode)} via public/board share issuance.",
                        "approvedByName" to company.ceoName,
                        "gameDay" to currentGameDay,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.set(
                    ledgerRef,
                    mapOf(
                        "txRef" to "IPO-${UUID.randomUUID().toString().take(8).uppercase()}",
                        "initiatedByUid" to uid,
                        "senderAccountId" to capitalAcctRef.id,
                        "receiverAccountId" to corpAcctRef.id,
                        "amountCents" to issuance.capitalRaisedCents,
                        "taxAmountCents" to 0L,
                        "taxTreasuryAccountId" to "",
                        "txType" to "EQUITY_CAPITAL_RAISE",
                        "gameDay" to currentGameDay,
                        "reference" to "Share Issuance ($newSharesIssued shares) in ${company.name}",
                        "status" to "SETTLED",
                        "publicAudit" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
            }.await()
            Result.success("Issued $newSharesIssued shares raising ${FinancialEngines.formatCurrency(issuance.capitalRaisedCents, company.currencyCode)}")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "companies/${company.companyId}")
            Result.failure(e)
        }
    }

    /**
     * Update 6: Controlled CEO appointment enforcing minimum 5% equity rule.
     */
    suspend fun appointCeoWithFivePercentRule(
        company: CompanyRecord,
        candidateUid: String,
        candidatePlayerId: Long,
        candidateName: String,
        grantControlledFivePctIfNeeded: Boolean,
        currentGameDay: Double,
    ): Result<String> {
        return try {
            val uid = requireUid()
            check(company.ownerUid == uid)
            val companyRef = db.collection("companies").document(company.companyId.toString())
            val candidateRef = companyRef.collection("shareholders").document(candidateUid)
            val founderRef = companyRef.collection("shareholders").document(uid)
            val resRef = companyRef.collection("board_resolutions").document()

            val msg = db.runTransaction { tx ->
                val candSnap = tx.get(candidateRef)
                val founderSnap = tx.get(founderRef)
                val candShares = candSnap.getLong("sharesHeld") ?: 0L
                val founderShares = founderSnap.getLong("sharesHeld") ?: 0L
                val minReq = FinancialEngines.minimumSharesForCeo(company.totalShares)

                var finalCandShares = candShares
                if (!FinancialEngines.meetsCeoMinimumOwnership(candShares, company.totalShares)) {
                    if (!grantControlledFivePctIfNeeded) {
                        throw IllegalStateException("CEO candidate must own at least 5% ($minReq shares).")
                    }
                    val needed = minReq - candShares
                    if (founderShares < needed) {
                        throw IllegalStateException("Owner does not hold enough shares to transfer 5% CEO stake.")
                    }
                    finalCandShares += needed
                    tx.update(
                        founderRef,
                        mapOf(
                            "sharesHeld" to (founderShares - needed),
                            "ceoEligible" to FinancialEngines.meetsCeoMinimumOwnership(founderShares - needed, company.totalShares),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                    if (candSnap.exists()) {
                        tx.update(
                            candidateRef,
                            mapOf(
                                "sharesHeld" to finalCandShares,
                                "ceoEligible" to true,
                                "updatedAt" to FieldValue.serverTimestamp(),
                            ),
                        )
                    } else {
                        tx.set(
                            candidateRef,
                            mapOf(
                                "companyId" to company.companyId,
                                "holderEntityId" to candidateUid,
                                "holderUid" to candidateUid,
                                "holderName" to candidateName,
                                "sharesHeld" to finalCandShares,
                                "ceoEligible" to true,
                                "updatedAt" to FieldValue.serverTimestamp(),
                            ),
                        )
                    }
                }

                tx.update(
                    companyRef,
                    mapOf(
                        "ceoUid" to candidateUid,
                        "ceoPlayerId" to candidatePlayerId,
                        "ceoName" to candidateName,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.set(
                    resRef,
                    mapOf(
                        "ownerUid" to uid,
                        "companyId" to company.companyId,
                        "resolutionType" to "CEO_APPOINTMENT",
                        "title" to "CEO Appointed: $candidateName",
                        "summary" to "Verified >=5% ownership ($finalCandShares / ${company.totalShares} shares).",
                        "approvedByName" to candidateName,
                        "gameDay" to currentGameDay,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                "$candidateName appointed CEO (${String.format(java.util.Locale.US, "%.2f", FinancialEngines.ownershipPercent(finalCandShares, company.totalShares))}% equity)."
            }.await()
            Result.success(msg)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "companies/${company.companyId}")
            Result.failure(e)
        }
    }

    /**
     * Update 7: Construct sector facility by paying government-set building cost from Corporate Account to County Treasury.
     */
    suspend fun buildFacility(
        company: CompanyRecord,
        spec: SectorFacilityCatalog.FacilitySpec,
        initialNpcStaff: Int,
        currentGameDay: Double,
    ): Result<CompanyFacility> {
        return try {
            val uid = requireUid()
            check(company.ownerUid == uid || company.ceoUid == uid)
            val clampedNpc = initialNpcStaff.coerceIn(0, spec.maxWorkforce)
            val corpAcctRef = db.collection("bank_accounts").document(company.corporateAccountId)
            val countyTreasuryRef = db.collection("bank_accounts").document("treasury_county_${company.countyId}")
            val facilityRef = db.collection("companies").document(company.companyId.toString()).collection("facilities").document()
            val ledgerRef = db.collection("ledger_transactions").document()

            val facility = db.runTransaction { tx ->
                val corpSnap = tx.get(corpAcctRef)
                val ctSnap = tx.get(countyTreasuryRef)
                val corpBal = corpSnap.getLong("balanceCents") ?: 0L
                val ctBal = ctSnap.getLong("balanceCents") ?: 0L
                if (corpBal < spec.constructionCostCents) {
                    throw IllegalStateException(
                        "Corporate account needs ${FinancialEngines.formatCurrency(spec.constructionCostCents, company.currencyCode)} to construct ${spec.displayName}.",
                    )
                }

                tx.update(corpAcctRef, mapOf("balanceCents" to (corpBal - spec.constructionCostCents), "updatedAt" to FieldValue.serverTimestamp()))
                if (ctSnap.exists()) {
                    tx.update(countyTreasuryRef, mapOf("balanceCents" to (ctBal + spec.constructionCostCents), "updatedAt" to FieldValue.serverTimestamp()))
                }
                tx.set(
                    facilityRef,
                    mapOf(
                        "ownerUid" to uid,
                        "companyId" to company.companyId,
                        "countyId" to company.countyId,
                        "specId" to spec.specId,
                        "facilityName" to spec.displayName,
                        "sector" to spec.sector,
                        "constructionCostCents" to spec.constructionCostCents,
                        "buildingCapacity" to spec.buildingCapacity,
                        "npcEmployees" to clampedNpc,
                        "maxWorkforce" to spec.maxWorkforce,
                        "npcWagePerDayCents" to spec.npcWagePerDayCents,
                        "maintenancePerDayCents" to spec.maintenancePerDayCents,
                        "requiredInputGood" to spec.requiredInputGood,
                        "requiredInputQty" to spec.requiredInputQty,
                        "outputGood" to spec.outputGood,
                        "facilityStatus" to "OPERATIONAL",
                        "readyAtGameDay" to (currentGameDay + spec.constructionGameDays),
                        "lastProducedUnits" to 0L,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.set(
                    ledgerRef,
                    mapOf(
                        "txRef" to "FAC-${UUID.randomUUID().toString().take(8).uppercase()}",
                        "initiatedByUid" to uid,
                        "senderAccountId" to corpAcctRef.id,
                        "receiverAccountId" to countyTreasuryRef.id,
                        "amountCents" to spec.constructionCostCents,
                        "taxAmountCents" to 0L,
                        "taxTreasuryAccountId" to "",
                        "txType" to "FACILITY_CONSTRUCTION",
                        "gameDay" to currentGameDay,
                        "reference" to "Build ${spec.displayName} in /county/${company.countyId}",
                        "status" to "SETTLED",
                        "publicAudit" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                CompanyFacility(
                    docId = facilityRef.id,
                    ownerUid = uid,
                    companyId = company.companyId,
                    countyId = company.countyId,
                    specId = spec.specId,
                    facilityName = spec.displayName,
                    sector = spec.sector,
                    constructionCostCents = spec.constructionCostCents,
                    buildingCapacity = spec.buildingCapacity,
                    npcEmployees = clampedNpc,
                    maxWorkforce = spec.maxWorkforce,
                    npcWagePerDayCents = spec.npcWagePerDayCents,
                    maintenancePerDayCents = spec.maintenancePerDayCents,
                    requiredInputGood = spec.requiredInputGood,
                    requiredInputQty = spec.requiredInputQty,
                    outputGood = spec.outputGood,
                )
            }.await()
            Result.success(facility)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "companies/${company.companyId}/facilities")
            Result.failure(e)
        }
    }

    suspend fun updateNpcStaffing(facility: CompanyFacility, npcEmployees: Int): Result<Unit> {
        return try {
            requireUid()
            if (npcEmployees !in 0..facility.maxWorkforce) {
                return Result.failure(IllegalArgumentException("NPC staff must be between 0 and ${facility.maxWorkforce}."))
            }
            db.collection("companies").document(facility.companyId.toString())
                .collection("facilities").document(facility.docId)
                .update(
                    mapOf(
                        "npcEmployees" to npcEmployees,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "companies/${facility.companyId}/facilities/${facility.docId}")
            Result.failure(e)
        }
    }

    /**
     * Update 7: Runs real production formula: Building Capacity * Workforce Efficiency * Input Availability.
     * Debits NPC wages & maintenance; consumes required input goods from Company Inventory;
     * places output into Company Inventory ONLY (never counts unsold output as revenue!).
     */
    suspend fun runProductionCycle(
        company: CompanyRecord,
        facility: CompanyFacility,
        currentGameDay: Double,
    ): Result<String> {
        return try {
            val uid = requireUid()
            check(company.ownerUid == uid || company.ceoUid == uid)

            val corpAcctRef = db.collection("bank_accounts").document(company.corporateAccountId)
            val countyTreasuryRef = db.collection("bank_accounts").document("treasury_county_${company.countyId}")
            val companyRef = db.collection("companies").document(company.companyId.toString())
            val facilityRef = companyRef.collection("facilities").document(facility.docId)

            val safeOutputKey = facility.outputGood.lowercase().replace(Regex("[^a-z0-9]+"), "_")
            val outputInvRef = db.collection("inventories").document("inv_co_${company.companyId}_$safeOutputKey")
            val inputInvRef = if (facility.requiredInputGood != "None" && facility.requiredInputQty > 0L) {
                val safeInKey = facility.requiredInputGood.lowercase().replace(Regex("[^a-z0-9]+"), "_")
                db.collection("inventories").document("inv_co_${company.companyId}_$safeInKey")
            } else {
                null
            }
            val ledgerRef = db.collection("ledger_transactions").document()

            val msg = db.runTransaction { tx ->
                // All reads first
                val corpSnap = tx.get(corpAcctRef)
                val ctSnap = tx.get(countyTreasuryRef)
                val outInvSnap = tx.get(outputInvRef)
                val inInvSnap = inputInvRef?.let { tx.get(it) }

                val availableInputs = inInvSnap?.getLong("quantity") ?: 0L
                val result = FinancialEngines.computeFacilityProduction(
                    buildingCapacity = facility.buildingCapacity,
                    npcEmployees = facility.npcEmployees,
                    maxWorkforce = facility.maxWorkforce,
                    requiredInputsPerCycle = facility.requiredInputQty,
                    availableInputInventory = availableInputs,
                    npcWagePerCycleCents = facility.npcWagePerDayCents,
                    maintenancePerCycleCents = facility.maintenancePerDayCents,
                )

                val corpBal = corpSnap.getLong("balanceCents") ?: 0L
                val ctBal = ctSnap.getLong("balanceCents") ?: 0L
                if (corpBal < result.operatingCostCents) {
                    throw IllegalStateException("Insufficient Corporate balance to pay NPC wages & maintenance (${FinancialEngines.formatCurrency(result.operatingCostCents, company.currencyCode)}).")
                }

                // Debit operating cost
                if (result.operatingCostCents > 0L) {
                    tx.update(corpAcctRef, mapOf("balanceCents" to (corpBal - result.operatingCostCents), "updatedAt" to FieldValue.serverTimestamp()))
                    if (ctSnap.exists()) {
                        tx.update(countyTreasuryRef, mapOf("balanceCents" to (ctBal + result.operatingCostCents), "updatedAt" to FieldValue.serverTimestamp()))
                    }
                    tx.set(
                        ledgerRef,
                        mapOf(
                            "txRef" to "OPX-${UUID.randomUUID().toString().take(8).uppercase()}",
                            "initiatedByUid" to uid,
                            "senderAccountId" to corpAcctRef.id,
                            "receiverAccountId" to countyTreasuryRef.id,
                            "amountCents" to result.operatingCostCents,
                            "taxAmountCents" to 0L,
                            "taxTreasuryAccountId" to "",
                            "txType" to "FACILITY_OPERATING_COST",
                            "gameDay" to currentGameDay,
                            "reference" to "${facility.facilityName} NPC payroll & maintenance",
                            "status" to "SETTLED",
                            "publicAudit" to false,
                            "createdAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                }

                // Consume input inventory
                if (inputInvRef != null && result.consumedInputUnits > 0L) {
                    tx.update(
                        inputInvRef,
                        mapOf(
                            "quantity" to (availableInputs - result.consumedInputUnits).coerceAtLeast(0L),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                }

                // Add produced units strictly to Company Inventory (revenue remains 0 until sold)
                val existingOutQty = outInvSnap.getLong("quantity") ?: 0L
                val unitCost = if (result.producedUnits > 0L) result.operatingCostCents / result.producedUnits else 0L
                if (outInvSnap.exists()) {
                    tx.update(
                        outputInvRef,
                        mapOf(
                            "quantity" to (existingOutQty + result.producedUnits),
                            "averageUnitCostCents" to unitCost,
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                } else {
                    tx.set(
                        outputInvRef,
                        mapOf(
                            "ownerUid" to uid,
                            "holderCategory" to "COMPANY",
                            "holderEntityId" to company.companyId.toString(),
                            "countyId" to company.countyId,
                            "commodityName" to facility.outputGood,
                            "quantity" to result.producedUnits,
                            "averageUnitCostCents" to unitCost,
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                }

                tx.update(
                    companyRef,
                    mapOf(
                        "operatingCostYtdCents" to (company.operatingCostYtdCents + result.operatingCostCents),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.update(
                    facilityRef,
                    mapOf(
                        "lastProducedUnits" to result.producedUnits,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )

                "Produced ${result.producedUnits} units of ${facility.outputGood} into Company Inventory (OpEx ${FinancialEngines.formatCurrency(result.operatingCostCents, company.currencyCode)})."
            }.await()

            Result.success(msg)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "inventories")
            Result.failure(e)
        }
    }

    /**
     * Update 8: Places a BUY, SELL, or AUCTION order on the county-specific TSN Marketplace.
     * Escrows inventory for SELL/AUCTION orders and escrows funds for BUY orders.
     */
    suspend fun placeMarketOrder(
        entityId: String,
        entityName: String,
        entityBankAccountId: String,
        holderCategory: String, // PERSONAL or COMPANY
        countyId: Long,
        commodityName: String,
        orderSide: String, // SELL, BUY, AUCTION
        quantity: Long,
        pricePerUnitCents: Long,
        currentGameDay: Double,
    ): Result<MarketOrder> {
        return try {
            val uid = requireUid()
            if (quantity <= 0L || pricePerUnitCents <= 0L) {
                return Result.failure(IllegalArgumentException("Quantity and unit price must be positive."))
            }
            val refCommodity = SectorFacilityCatalog.findCommodityByName(commodityName)
            val basePrice = refCommodity?.basePriceCents ?: pricePerUnitCents
            val minP = (basePrice * 0.25).toLong().coerceAtLeast(1L)
            val maxP = (basePrice * 3.0).toLong()
            if (pricePerUnitCents < minP || pricePerUnitCents > maxP) {
                return Result.failure(IllegalArgumentException("Anti-manipulation guard: price must stay within $minP–$maxP cents for $commodityName."))
            }

            val orderRef = db.collection("market_orders").document()
            val safeKey = commodityName.lowercase().replace(Regex("[^a-z0-9]+"), "_")
            val invDocId = if (holderCategory == "COMPANY") "inv_co_${entityId}_$safeKey" else "inv_per_${uid}_$safeKey"
            val invRef = db.collection("inventories").document(invDocId)
            val bankRef = db.collection("bank_accounts").document(entityBankAccountId)
            val escrowRef = db.collection("bank_accounts").document("escrow_county_$countyId")
            val totalOrderCents = quantity * pricePerUnitCents
            val ledgerRef = db.collection("ledger_transactions").document()

            val created = db.runTransaction { tx ->
                if (orderSide == "BUY") {
                    val bSnap = tx.get(bankRef)
                    val escSnap = tx.get(escrowRef)
                    val bBal = bSnap.getLong("balanceCents") ?: 0L
                    val escBal = escSnap.getLong("balanceCents") ?: 0L
                    if (bBal < totalOrderCents) {
                        throw IllegalStateException("Insufficient balance to escrow BUY order (${FinancialEngines.formatCurrency(totalOrderCents)}).")
                    }
                    tx.update(bankRef, mapOf("balanceCents" to (bBal - totalOrderCents), "updatedAt" to FieldValue.serverTimestamp()))
                    tx.update(escrowRef, mapOf("balanceCents" to (escBal + totalOrderCents), "updatedAt" to FieldValue.serverTimestamp()))
                    tx.set(
                        ledgerRef,
                        mapOf(
                            "txRef" to "ESC-${UUID.randomUUID().toString().take(8).uppercase()}",
                            "initiatedByUid" to uid,
                            "senderAccountId" to bankRef.id,
                            "receiverAccountId" to escrowRef.id,
                            "amountCents" to totalOrderCents,
                            "taxAmountCents" to 0L,
                            "taxTreasuryAccountId" to "",
                            "txType" to "MARKET_ORDER_ESCROW",
                            "gameDay" to currentGameDay,
                            "reference" to "Escrow BUY $quantity $commodityName @ $pricePerUnitCents c",
                            "status" to "SETTLED",
                            "publicAudit" to false,
                            "createdAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                } else {
                    val iSnap = tx.get(invRef)
                    val currentQty = iSnap.getLong("quantity") ?: 0L
                    if (!iSnap.exists() || currentQty < quantity) {
                        throw IllegalStateException("Insufficient $holderCategory inventory of $commodityName (have $currentQty, need $quantity).")
                    }
                    tx.update(invRef, mapOf("quantity" to (currentQty - quantity), "updatedAt" to FieldValue.serverTimestamp()))
                }

                tx.set(
                    orderRef,
                    mapOf(
                        "scope" to "TSN_MARKET",
                        "ownerUid" to uid,
                        "entityId" to entityId,
                        "entityName" to entityName.take(80),
                        "entityBankAccountId" to entityBankAccountId,
                        "countyId" to countyId,
                        "commodityName" to commodityName,
                        "orderSide" to orderSide,
                        "quantity" to quantity,
                        "remainingQuantity" to quantity,
                        "pricePerUnitCents" to pricePerUnitCents,
                        "escrowAccountId" to escrowRef.id,
                        "orderStatus" to "OPEN",
                        "auctionListing" to (orderSide == "AUCTION"),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                MarketOrder(
                    docId = orderRef.id,
                    ownerUid = uid,
                    entityId = entityId,
                    entityName = entityName,
                    entityBankAccountId = entityBankAccountId,
                    countyId = countyId,
                    commodityName = commodityName,
                    orderSide = orderSide,
                    quantity = quantity,
                    remainingQuantity = quantity,
                    pricePerUnitCents = pricePerUnitCents,
                    escrowAccountId = escrowRef.id,
                    auctionListing = (orderSide == "AUCTION"),
                )
            }.await()
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "market_orders")
            Result.failure(e)
        }
    }

    /**
     * Update 8: Fills an open SELL or AUCTION order cleanly with anti-wash checks, sales tax routing
     * to National Treasury, buyer inventory delivery, and immutable trade history.
     */
    suspend fun fillSellOrder(
        order: MarketOrder,
        buyerEntityId: String,
        buyerBankAccountId: String,
        buyerHolderCategory: String, // PERSONAL or COMPANY
        quantityToBuy: Long,
        salesTaxPct: Double,
        countryId: Long,
        currentGameDay: Double,
    ): Result<String> {
        return try {
            val uid = requireUid()
            val refCommodity = SectorFacilityCatalog.findCommodityByName(order.commodityName)
            val validation = FinancialEngines.validateMarketplaceTrade(
                buyerEntityId = buyerEntityId,
                sellerEntityId = order.entityId,
                buyerOwnerUid = uid,
                sellerOwnerUid = order.ownerUid,
                orderPriceCents = order.pricePerUnitCents,
                referencePriceCents = refCommodity?.basePriceCents ?: order.pricePerUnitCents,
                quantity = quantityToBuy,
            )
            if (validation is FinancialEngines.TransferValidation.Invalid) {
                return Result.failure(IllegalStateException(validation.reason))
            }

            val orderRef = db.collection("market_orders").document(order.docId)
            val buyerBankRef = db.collection("bank_accounts").document(buyerBankAccountId)
            val sellerBankRef = db.collection("bank_accounts").document(order.entityBankAccountId)
            val natTreasuryRef = db.collection("bank_accounts").document("treasury_country_$countryId")
            val safeKey = order.commodityName.lowercase().replace(Regex("[^a-z0-9]+"), "_")
            val buyerInvDocId = if (buyerHolderCategory == "COMPANY") "inv_co_${buyerEntityId}_$safeKey" else "inv_per_${uid}_$safeKey"
            val buyerInvRef = db.collection("inventories").document(buyerInvDocId)
            val tradeRef = db.collection("trade_history").document()
            val ledgerRef = db.collection("ledger_transactions").document()

            val summary = db.runTransaction { tx ->
                // All reads first
                val orderSnap = tx.get(orderRef)
                val bBankSnap = tx.get(buyerBankRef)
                val sBankSnap = tx.get(sellerBankRef)
                val tSnap = tx.get(natTreasuryRef)
                val invSnap = tx.get(buyerInvRef)

                val remaining = orderSnap.getLong("remainingQuantity") ?: 0L
                val fillQty = quantityToBuy.coerceAtMost(remaining)
                if (fillQty <= 0L) throw IllegalStateException("Order has no remaining units.")

                val grossValue = fillQty * order.pricePerUnitCents
                val tax = FinancialEngines.computeTaxBreakdown(
                    grossAmountCents = grossValue,
                    nationalRatePct = salesTaxPct,
                    stateRatePct = 0.0,
                    countyRatePct = 0.0,
                )

                val bBal = bBankSnap.getLong("balanceCents") ?: 0L
                val sBal = sBankSnap.getLong("balanceCents") ?: 0L
                val tBal = tSnap.getLong("balanceCents") ?: 0L
                if (bBal < grossValue) throw IllegalStateException("Insufficient buyer balance.")

                // Debit buyer, credit seller (net) + treasury (sales tax)
                tx.update(buyerBankRef, mapOf("balanceCents" to (bBal - grossValue), "updatedAt" to FieldValue.serverTimestamp()))
                if (sBankSnap.exists()) {
                    tx.update(sellerBankRef, mapOf("balanceCents" to (sBal + tax.netPayoutCents), "updatedAt" to FieldValue.serverTimestamp()))
                }
                if (tSnap.exists() && tax.totalTaxCents > 0L) {
                    tx.update(natTreasuryRef, mapOf("balanceCents" to (tBal + tax.totalTaxCents), "updatedAt" to FieldValue.serverTimestamp()))
                }

                val newRem = remaining - fillQty
                tx.update(
                    orderRef,
                    mapOf(
                        "remainingQuantity" to newRem,
                        "orderStatus" to if (newRem == 0L) "FILLED" else "PARTIAL",
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )

                val existingBuyerQty = invSnap.getLong("quantity") ?: 0L
                if (invSnap.exists()) {
                    tx.update(buyerInvRef, mapOf("quantity" to (existingBuyerQty + fillQty), "updatedAt" to FieldValue.serverTimestamp()))
                } else {
                    tx.set(
                        buyerInvRef,
                        mapOf(
                            "ownerUid" to uid,
                            "holderCategory" to buyerHolderCategory,
                            "holderEntityId" to buyerEntityId,
                            "countyId" to order.countyId,
                            "commodityName" to order.commodityName,
                            "quantity" to fillQty,
                            "averageUnitCostCents" to order.pricePerUnitCents,
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                }

                val txRef = "TSN-${UUID.randomUUID().toString().take(8).uppercase()}"
                tx.set(
                    tradeRef,
                    mapOf(
                        "scope" to "TSN_TRADE",
                        "orderId" to order.docId,
                        "countyId" to order.countyId,
                        "commodityName" to order.commodityName,
                        "buyerEntityId" to buyerEntityId,
                        "sellerEntityId" to order.entityId,
                        "quantity" to fillQty,
                        "unitPriceCents" to order.pricePerUnitCents,
                        "totalValueCents" to grossValue,
                        "salesTaxCents" to tax.totalTaxCents,
                        "txRef" to txRef,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.set(
                    ledgerRef,
                    mapOf(
                        "txRef" to txRef,
                        "initiatedByUid" to uid,
                        "senderAccountId" to buyerBankAccountId,
                        "receiverAccountId" to order.entityBankAccountId,
                        "amountCents" to grossValue,
                        "taxAmountCents" to tax.totalTaxCents,
                        "taxTreasuryAccountId" to natTreasuryRef.id,
                        "txType" to "TSN_MARKET_TRADE",
                        "gameDay" to currentGameDay,
                        "reference" to "Bought $fillQty ${order.commodityName} in /county/${order.countyId}",
                        "status" to "SETTLED",
                        "publicAudit" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                "Trade $txRef settled: $fillQty ${order.commodityName} (${FinancialEngines.formatCurrency(grossValue)}, tax ${tax.totalTaxCents}c)."
            }.await()

            Result.success(summary)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "trade_history")
            Result.failure(e)
        }
    }
}
