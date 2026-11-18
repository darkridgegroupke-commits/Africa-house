package com.example.data.repository

import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.BankAccount
import com.example.data.model.BondRecord
import com.example.data.model.EmploymentContract
import com.example.data.model.LedgerTransaction
import com.example.data.model.LoanRecord
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
 * Updates 4, 5 & 11: Real double-entry banking ledger, Personal/Capital/Campaign accounts,
 * atomic transfers with idempotency keys, employment contracts, hourly salary payouts with
 * multi-tier National/State/County tax routing, loans (as liabilities), and bonds.
 */
class BankingAndEmploymentRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Google Sign-In required.")

    fun observeMyAccounts(uid: String): Flow<List<BankAccount>> = flow {
        val authed = requireUid()
        check(authed == uid)
        emitAll(
            db.collection("bank_accounts")
                .whereEqualTo("ownerUid", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(BankAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.docId }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == authed) handleFirestoreError(fe ?: e, OperationType.LIST, "bank_accounts")
                    throw e
                },
        )
    }

    fun observeTreasuryAccounts(): Flow<List<BankAccount>> = flow {
        val uid = requireUid()
        emitAll(
            db.collection("bank_accounts")
                .whereEqualTo("accountCategory", "TREASURY")
                .snapshots()
                .map { snap ->
                    snap.toObjects(BankAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.docId }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, "bank_accounts")
                    throw e
                },
        )
    }

    fun observeMyLedgerTransactions(uid: String): Flow<List<LedgerTransaction>> = flow {
        val authed = requireUid()
        check(authed == uid)
        emitAll(
            db.collection("ledger_transactions")
                .whereEqualTo("initiatedByUid", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(LedgerTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                        .take(50)
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == authed) handleFirestoreError(fe ?: e, OperationType.LIST, "ledger_transactions")
                    throw e
                },
        )
    }

    fun observeEmploymentContracts(uid: String): Flow<List<EmploymentContract>> = flow {
        val authed = requireUid()
        check(authed == uid)
        emitAll(
            db.collection("employment_contracts")
                .whereEqualTo("ownerUid", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(EmploymentContract::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == authed) handleFirestoreError(fe ?: e, OperationType.LIST, "employment_contracts")
                    throw e
                },
        )
    }

    fun observeMyLoans(uid: String): Flow<List<LoanRecord>> = flow {
        val authed = requireUid()
        check(authed == uid)
        emitAll(
            db.collection("loans")
                .whereEqualTo("ownerUid", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(LoanRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == authed) handleFirestoreError(fe ?: e, OperationType.LIST, "loans")
                    throw e
                },
        )
    }

    fun observeBonds(): Flow<List<BondRecord>> = flow {
        val uid = requireUid()
        emitAll(
            db.collection("bonds")
                .whereEqualTo("scope", "BOND")
                .snapshots()
                .map { snap ->
                    snap.toObjects(BondRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, "bonds")
                    throw e
                },
        )
    }

    /**
     * Provisions Personal, Capital, and Campaign accounts starting at exact 0 balance (never fake cash).
     */
    suspend fun ensureCitizenAccounts(currencyCode: String): Result<Unit> {
        return try {
            val uid = requireUid()
            val specs = listOf(
                Triple("acct_${uid}_personal", "PERSONAL", "CITIZEN"),
                Triple("acct_${uid}_capital", "CAPITAL", "CITIZEN"),
                Triple("acct_${uid}_campaign", "CAMPAIGN", "CAMPAIGN"),
            )
            specs.forEach { (acctId, type, category) ->
                val ref = db.collection("bank_accounts").document(acctId)
                if (!ref.get().await().exists()) {
                    ref.set(
                        mapOf(
                            "ownerUid" to uid,
                            "ownerEntityId" to uid,
                            "accountType" to type,
                            "accountCategory" to category,
                            "currencyCode" to currencyCode.take(6).ifBlank { "NGN" },
                            "balanceCents" to 0L,
                            "restricted" to (category == "CAMPAIGN"),
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "bank_accounts")
            Result.failure(e)
        }
    }

    /**
     * Atomic double-entry transfer between two segregated accounts.
     * Prevents double spending, duplicate payouts (via idempotencyKey), negative balances,
     * Campaign -> Personal leaks, and double taxation on own-account transfers.
     */
    suspend fun executeAtomicTransfer(
        senderAccountId: String,
        receiverAccountId: String,
        amountCents: Long,
        txType: String,
        reference: String,
        gameDay: Double,
        idempotencyKey: String = "tx_${UUID.randomUUID()}",
        publicAudit: Boolean = false,
    ): Result<String> {
        return try {
            val uid = requireUid()
            if (senderAccountId == receiverAccountId) {
                return Result.failure(IllegalArgumentException("Sender and receiver accounts must differ."))
            }
            if (amountCents <= 0L) {
                return Result.failure(IllegalArgumentException("Transfer amount must be positive."))
            }

            val senderRef = db.collection("bank_accounts").document(senderAccountId)
            val receiverRef = db.collection("bank_accounts").document(receiverAccountId)
            val idempRef = db.collection("idempotency_keys").document(idempotencyKey)
            val ledgerRef = db.collection("ledger_transactions").document()
            val auditRef = db.collection("audit_logs").document()
            val txRef = "LED-${UUID.randomUUID().toString().take(8).uppercase()}"

            val settledMsg = db.runTransaction { tx ->
                // All reads first
                val idempSnap = tx.get(idempRef)
                if (idempSnap.exists()) {
                    throw IllegalStateException("Duplicate transaction blocked (idempotency key already settled).")
                }
                val senderSnap = tx.get(senderRef)
                val receiverSnap = tx.get(receiverRef)
                if (!senderSnap.exists() || !receiverSnap.exists()) {
                    throw IllegalArgumentException("One or both ledger accounts do not exist.")
                }

                val senderCatStr = senderSnap.getString("accountCategory") ?: "CITIZEN"
                val senderTypeStr = senderSnap.getString("accountType") ?: "PERSONAL"
                val receiverTypeStr = receiverSnap.getString("accountType") ?: "PERSONAL"
                val senderBal = senderSnap.getLong("balanceCents") ?: 0L
                val receiverBal = receiverSnap.getLong("balanceCents") ?: 0L

                val senderCategory = runCatching { FinancialEngines.AccountCategory.valueOf(senderCatStr) }
                    .getOrDefault(FinancialEngines.AccountCategory.CITIZEN)
                val senderType = runCatching { FinancialEngines.AccountType.valueOf(senderTypeStr) }
                    .getOrDefault(FinancialEngines.AccountType.PERSONAL)
                val receiverType = runCatching { FinancialEngines.AccountType.valueOf(receiverTypeStr) }
                    .getOrDefault(FinancialEngines.AccountType.PERSONAL)

                val validation = FinancialEngines.validateTransfer(
                    senderAccountId = senderAccountId,
                    receiverAccountId = receiverAccountId,
                    senderCategory = senderCategory,
                    senderType = senderType,
                    receiverType = receiverType,
                    senderBalanceCents = senderBal,
                    amountCents = amountCents,
                )
                if (validation is FinancialEngines.TransferValidation.Invalid) {
                    throw IllegalStateException(validation.reason)
                }

                tx.update(
                    senderRef,
                    mapOf("balanceCents" to (senderBal - amountCents), "updatedAt" to FieldValue.serverTimestamp()),
                )
                tx.update(
                    receiverRef,
                    mapOf("balanceCents" to (receiverBal + amountCents), "updatedAt" to FieldValue.serverTimestamp()),
                )
                tx.set(
                    idempRef,
                    mapOf("ownerUid" to uid, "txRef" to txRef, "createdAt" to FieldValue.serverTimestamp()),
                )
                tx.set(
                    ledgerRef,
                    mapOf(
                        "txRef" to txRef,
                        "initiatedByUid" to uid,
                        "senderAccountId" to senderAccountId,
                        "receiverAccountId" to receiverAccountId,
                        "amountCents" to amountCents,
                        "taxAmountCents" to 0L,
                        "taxTreasuryAccountId" to "",
                        "txType" to txType.take(50),
                        "gameDay" to gameDay.coerceAtLeast(0.0),
                        "reference" to reference.trim().ifBlank { txType }.take(200),
                        "status" to "SETTLED",
                        "publicAudit" to publicAudit,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.set(
                    auditRef,
                    mapOf(
                        "actorUid" to uid,
                        "actionType" to "LEDGER_$txType".take(50),
                        "entityRef" to txRef,
                        "detail" to "$senderAccountId -> $receiverAccountId ($amountCents cents)",
                        "gameDay" to gameDay.coerceAtLeast(0.0),
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                "Settled $txRef (${FinancialEngines.formatCurrency(amountCents)})"
            }.await()

            Result.success(settledMsg)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "ledger_transactions")
            Result.failure(e)
        }
    }

    /**
     * Update 5: Sign an employment contract backed by a real treasury or company account.
     */
    suspend fun signEmploymentContract(
        playerId: Long,
        employerEntityId: String,
        employerName: String,
        employerAccountId: String,
        jobTitle: String,
        hourlyWageCents: Long,
        countryId: Long,
        stateId: Long,
        countyId: Long,
        currentGameDay: Double,
    ): Result<EmploymentContract> {
        return try {
            val uid = requireUid()
            val ref = db.collection("employment_contracts").document()
            val payload = mapOf(
                "ownerUid" to uid,
                "scope" to "EMPLOYMENT",
                "playerId" to playerId,
                "employerEntityId" to employerEntityId,
                "employerName" to employerName.take(80),
                "employerAccountId" to employerAccountId,
                "jobTitle" to jobTitle.trim().ifBlank { "Civic Officer" }.take(80),
                "hourlyWageCents" to hourlyWageCents.coerceAtLeast(500L),
                "countryId" to countryId,
                "stateId" to stateId,
                "countyId" to countyId,
                "totalPaidCents" to 0L,
                "totalTaxDeductedCents" to 0L,
                "unpaidLiabilityCents" to 0L,
                "lastPaidGameDay" to currentGameDay,
                "contractStatus" to "ACTIVE",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            ref.set(payload).await()
            Result.success(
                EmploymentContract(
                    docId = ref.id,
                    ownerUid = uid,
                    playerId = playerId,
                    employerEntityId = employerEntityId,
                    employerName = employerName,
                    employerAccountId = employerAccountId,
                    jobTitle = jobTitle,
                    hourlyWageCents = hourlyWageCents,
                    countryId = countryId,
                    stateId = stateId,
                    countyId = countyId,
                    lastPaidGameDay = currentGameDay,
                ),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "employment_contracts")
            Result.failure(e)
        }
    }

    /**
     * Update 5: Accrues and settles hourly salary against server clock, deducting National, State,
     * and County taxes directly into their respective treasuries. Uses idempotency keys per payroll cycle
     * and records unpaid liabilities if the employer account lacks funds.
     */
    suspend fun settleSalaryCycle(
        contract: EmploymentContract,
        hoursWorked: Int,
        nationalTaxPct: Double,
        stateTaxPct: Double,
        countyTaxPct: Double,
        currentGameDay: Double,
        cycleKey: String,
    ): Result<String> {
        return try {
            val uid = requireUid()
            check(contract.ownerUid == uid)
            if (hoursWorked <= 0) return Result.failure(IllegalArgumentException("Hours worked must be positive."))

            val grossPay = contract.hourlyWageCents * hoursWorked.toLong() + contract.unpaidLiabilityCents
            val tax = FinancialEngines.computeTaxBreakdown(
                grossAmountCents = grossPay,
                nationalRatePct = nationalTaxPct,
                stateRatePct = stateTaxPct,
                countyRatePct = countyTaxPct,
                isOwnAccountTransfer = false,
            )

            val contractRef = db.collection("employment_contracts").document(contract.docId)
            val employerAcctRef = db.collection("bank_accounts").document(contract.employerAccountId)
            val personalAcctRef = db.collection("bank_accounts").document("acct_${uid}_personal")
            val natTreasuryRef = db.collection("bank_accounts").document("treasury_country_${contract.countryId}")
            val stateTreasuryRef = db.collection("bank_accounts").document("treasury_state_${contract.stateId}")
            val countyTreasuryRef = db.collection("bank_accounts").document("treasury_county_${contract.countyId}")
            val idempRef = db.collection("idempotency_keys").document("sal_${contract.docId}_$cycleKey")
            val ledgerNetRef = db.collection("ledger_transactions").document()
            val ledgerTaxRef = db.collection("ledger_transactions").document()
            val notifRef = db.collection("users").document(uid).collection("notifications").document()

            val summary = db.runTransaction { tx ->
                // All reads first
                if (tx.get(idempRef).exists()) {
                    throw IllegalStateException("Salary for cycle $cycleKey already paid (duplicate blocked).")
                }
                val empSnap = tx.get(employerAcctRef)
                val perSnap = tx.get(personalAcctRef)
                val natSnap = tx.get(natTreasuryRef)
                val stSnap = tx.get(stateTreasuryRef)
                val ctSnap = tx.get(countyTreasuryRef)

                if (!perSnap.exists()) throw IllegalStateException("Personal bank account not initialized.")
                val empBal = empSnap.getLong("balanceCents") ?: 0L
                val perBal = perSnap.getLong("balanceCents") ?: 0L
                val natBal = natSnap.getLong("balanceCents") ?: 0L
                val stBal = stSnap.getLong("balanceCents") ?: 0L
                val ctBal = ctSnap.getLong("balanceCents") ?: 0L

                tx.set(
                    idempRef,
                    mapOf("ownerUid" to uid, "cycleKey" to cycleKey, "createdAt" to FieldValue.serverTimestamp()),
                )

                if (!empSnap.exists() || empBal < grossPay) {
                    // Record unpaid wage liability instead of inventing money
                    tx.update(
                        contractRef,
                        mapOf(
                            "unpaidLiabilityCents" to grossPay,
                            "lastPaidGameDay" to currentGameDay,
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                    tx.set(
                        notifRef,
                        mapOf(
                            "ownerUid" to uid,
                            "category" to "Salaries",
                            "title" to "Employer Wage Arrears Recorded",
                            "detail" to "${contract.employerName} owes ${FinancialEngines.formatCurrency(grossPay)} in unpaid wages.",
                            "referenceId" to contract.docId,
                            "readStatus" to false,
                            "createdAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                    return@runTransaction "Employer lacked liquidity; ${FinancialEngines.formatCurrency(grossPay)} added to Unpaid Wage Liabilities."
                }

                // Debit employer
                tx.update(
                    employerAcctRef,
                    mapOf("balanceCents" to (empBal - grossPay), "updatedAt" to FieldValue.serverTimestamp()),
                )
                // Credit citizen personal net pay
                tx.update(
                    personalAcctRef,
                    mapOf("balanceCents" to (perBal + tax.netPayoutCents), "updatedAt" to FieldValue.serverTimestamp()),
                )
                // Credit National, State & County treasuries unless employer IS that treasury (avoid self-credit overwrite)
                if (tax.nationalTaxCents > 0L && natSnap.exists() && natTreasuryRef.id != employerAcctRef.id) {
                    tx.update(
                        natTreasuryRef,
                        mapOf("balanceCents" to (natBal + tax.nationalTaxCents), "updatedAt" to FieldValue.serverTimestamp()),
                    )
                }
                if (tax.stateTaxCents > 0L && stSnap.exists() && stateTreasuryRef.id != employerAcctRef.id) {
                    tx.update(
                        stateTreasuryRef,
                        mapOf("balanceCents" to (stBal + tax.stateTaxCents), "updatedAt" to FieldValue.serverTimestamp()),
                    )
                }
                if (tax.countyTaxCents > 0L && ctSnap.exists() && countyTreasuryRef.id != employerAcctRef.id) {
                    tx.update(
                        countyTreasuryRef,
                        mapOf("balanceCents" to (ctBal + tax.countyTaxCents), "updatedAt" to FieldValue.serverTimestamp()),
                    )
                }

                tx.update(
                    contractRef,
                    mapOf(
                        "totalPaidCents" to (contract.totalPaidCents + tax.netPayoutCents),
                        "totalTaxDeductedCents" to (contract.totalTaxDeductedCents + tax.totalTaxCents),
                        "unpaidLiabilityCents" to 0L,
                        "lastPaidGameDay" to currentGameDay,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )

                val txRefNet = "SAL-${UUID.randomUUID().toString().take(8).uppercase()}"
                tx.set(
                    ledgerNetRef,
                    mapOf(
                        "txRef" to txRefNet,
                        "initiatedByUid" to uid,
                        "senderAccountId" to employerAcctRef.id,
                        "receiverAccountId" to personalAcctRef.id,
                        "amountCents" to tax.netPayoutCents.coerceAtLeast(1L),
                        "taxAmountCents" to tax.totalTaxCents,
                        "taxTreasuryAccountId" to natTreasuryRef.id,
                        "txType" to "SALARY_PAYOUT",
                        "gameDay" to currentGameDay,
                        "reference" to "Net Salary (${contract.jobTitle}) • Tax: ${tax.totalTaxCents}c",
                        "status" to "SETTLED",
                        "publicAudit" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                if (tax.totalTaxCents > 0L && natSnap.exists() && natTreasuryRef.id != employerAcctRef.id) {
                    tx.set(
                        ledgerTaxRef,
                        mapOf(
                            "txRef" to "TAX-${UUID.randomUUID().toString().take(8).uppercase()}",
                            "initiatedByUid" to uid,
                            "senderAccountId" to employerAcctRef.id,
                            "receiverAccountId" to natTreasuryRef.id,
                            "amountCents" to tax.totalTaxCents,
                            "taxAmountCents" to tax.totalTaxCents,
                            "taxTreasuryAccountId" to natTreasuryRef.id,
                            "txType" to "INCOME_TAX_ROUTING",
                            "gameDay" to currentGameDay,
                            "reference" to "Nat ${tax.nationalTaxCents}c / State ${tax.stateTaxCents}c / County ${tax.countyTaxCents}c",
                            "status" to "SETTLED",
                            "publicAudit" to true,
                            "createdAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                }
                tx.set(
                    notifRef,
                    mapOf(
                        "ownerUid" to uid,
                        "category" to "Salaries",
                        "title" to "Salary Paid: ${FinancialEngines.formatCurrency(tax.netPayoutCents)}",
                        "detail" to "Taxes routed (Nat ${tax.nationalTaxCents}c, State ${tax.stateTaxCents}c, County ${tax.countyTaxCents}c)",
                        "referenceId" to txRefNet,
                        "readStatus" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                "Paid ${FinancialEngines.formatCurrency(tax.netPayoutCents)} net salary (${FinancialEngines.formatCurrency(tax.totalTaxCents)} tax routed to treasuries)."
            }.await()

            Result.success(summary)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "employment_contracts/${contract.docId}")
            Result.failure(e)
        }
    }

    /**
     * Update 11: Personal or Company Loan — proceeds credited from Treasury/Lender as LIABILITY, never income.
     */
    suspend fun issueLoan(
        borrowerEntityId: String,
        borrowerAccountId: String,
        lenderAccountId: String,
        borrowerCategory: String,
        principalCents: Long,
        annualInterestPct: Double,
        collateralSummary: String,
        currentGameDay: Double,
    ): Result<LoanRecord> {
        return try {
            val uid = requireUid()
            if (principalCents <= 0L) return Result.failure(IllegalArgumentException("Loan principal must be positive."))

            val lenderRef = db.collection("bank_accounts").document(lenderAccountId)
            val borrowerRef = db.collection("bank_accounts").document(borrowerAccountId)
            val loanRef = db.collection("loans").document()
            val ledgerRef = db.collection("ledger_transactions").document()
            val maturity = currentGameDay + 720.0

            val created = db.runTransaction { tx ->
                val lenderSnap = tx.get(lenderRef)
                val borrowerSnap = tx.get(borrowerRef)
                val lenderBal = lenderSnap.getLong("balanceCents") ?: 0L
                val borrowerBal = borrowerSnap.getLong("balanceCents") ?: 0L
                if (!lenderSnap.exists() || lenderBal < principalCents) {
                    throw IllegalStateException("Lender reserve has insufficient liquidity for this loan.")
                }
                if (!borrowerSnap.exists()) {
                    throw IllegalStateException("Borrower account does not exist.")
                }

                tx.update(lenderRef, mapOf("balanceCents" to (lenderBal - principalCents), "updatedAt" to FieldValue.serverTimestamp()))
                tx.update(borrowerRef, mapOf("balanceCents" to (borrowerBal + principalCents), "updatedAt" to FieldValue.serverTimestamp()))
                tx.set(
                    loanRef,
                    mapOf(
                        "ownerUid" to uid,
                        "borrowerEntityId" to borrowerEntityId,
                        "borrowerAccountId" to borrowerAccountId,
                        "lenderAccountId" to lenderAccountId,
                        "borrowerCategory" to borrowerCategory,
                        "principalCents" to principalCents,
                        "principalRemainingCents" to principalCents,
                        "annualInterestPct" to annualInterestPct,
                        "collateralSummary" to collateralSummary.trim().ifBlank { "Personal/Capital Guarantee" }.take(120),
                        "loanStatus" to "ACTIVE",
                        "maturityGameDay" to maturity,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.set(
                    ledgerRef,
                    mapOf(
                        "txRef" to "LOAN-${UUID.randomUUID().toString().take(8).uppercase()}",
                        "initiatedByUid" to uid,
                        "senderAccountId" to lenderAccountId,
                        "receiverAccountId" to borrowerAccountId,
                        "amountCents" to principalCents,
                        "taxAmountCents" to 0L,
                        "taxTreasuryAccountId" to "",
                        "txType" to "LOAN_DISBURSEMENT_LIABILITY",
                        "gameDay" to currentGameDay,
                        "reference" to "Loan Liability (${annualInterestPct}% APR) — Not Income",
                        "status" to "SETTLED",
                        "publicAudit" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                LoanRecord(
                    docId = loanRef.id,
                    ownerUid = uid,
                    borrowerEntityId = borrowerEntityId,
                    borrowerAccountId = borrowerAccountId,
                    lenderAccountId = lenderAccountId,
                    borrowerCategory = borrowerCategory,
                    principalCents = principalCents,
                    principalRemainingCents = principalCents,
                    annualInterestPct = annualInterestPct,
                    collateralSummary = collateralSummary,
                    maturityGameDay = maturity,
                )
            }.await()
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "loans")
            Result.failure(e)
        }
    }

    suspend fun repayLoan(loan: LoanRecord, repayCents: Long, currentGameDay: Double): Result<Unit> {
        return try {
            val uid = requireUid()
            check(loan.ownerUid == uid)
            val actualRepay = repayCents.coerceAtMost(loan.principalRemainingCents)
            if (actualRepay <= 0L) return Result.failure(IllegalArgumentException("Loan already repaid."))

            val borrowerRef = db.collection("bank_accounts").document(loan.borrowerAccountId)
            val lenderRef = db.collection("bank_accounts").document(loan.lenderAccountId)
            val loanRef = db.collection("loans").document(loan.docId)
            val ledgerRef = db.collection("ledger_transactions").document()

            db.runTransaction { tx ->
                val bSnap = tx.get(borrowerRef)
                val lSnap = tx.get(lenderRef)
                val bBal = bSnap.getLong("balanceCents") ?: 0L
                val lBal = lSnap.getLong("balanceCents") ?: 0L
                if (bBal < actualRepay) throw IllegalStateException("Insufficient balance to repay loan.")

                val remaining = loan.principalRemainingCents - actualRepay
                tx.update(borrowerRef, mapOf("balanceCents" to (bBal - actualRepay), "updatedAt" to FieldValue.serverTimestamp()))
                tx.update(lenderRef, mapOf("balanceCents" to (lBal + actualRepay), "updatedAt" to FieldValue.serverTimestamp()))
                tx.update(
                    loanRef,
                    mapOf(
                        "principalRemainingCents" to remaining,
                        "loanStatus" to if (remaining == 0L) "REPAID" else "ACTIVE",
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.set(
                    ledgerRef,
                    mapOf(
                        "txRef" to "RPY-${UUID.randomUUID().toString().take(8).uppercase()}",
                        "initiatedByUid" to uid,
                        "senderAccountId" to loan.borrowerAccountId,
                        "receiverAccountId" to loan.lenderAccountId,
                        "amountCents" to actualRepay,
                        "taxAmountCents" to 0L,
                        "taxTreasuryAccountId" to "",
                        "txType" to "LOAN_REPAYMENT",
                        "gameDay" to currentGameDay,
                        "reference" to "Principal Repayment for Loan ${loan.docId}",
                        "status" to "SETTLED",
                        "publicAudit" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "loans/${loan.docId}")
            Result.failure(e)
        }
    }

    suspend fun purchaseBond(
        issuerType: String,
        issuerName: String,
        issuerAccountId: String,
        buyerAccountId: String,
        faceValueCents: Long,
        couponRatePct: Double,
        currentGameDay: Double,
    ): Result<BondRecord> {
        return try {
            val uid = requireUid()
            if (faceValueCents <= 0L) return Result.failure(IllegalArgumentException("Bond face value must be positive."))

            val buyerRef = db.collection("bank_accounts").document(buyerAccountId)
            val issuerRef = db.collection("bank_accounts").document(issuerAccountId)
            val bondRef = db.collection("bonds").document()
            val ledgerRef = db.collection("ledger_transactions").document()
            val maturity = currentGameDay + 1080.0

            val created = db.runTransaction { tx ->
                val bSnap = tx.get(buyerRef)
                val iSnap = tx.get(issuerRef)
                val bBal = bSnap.getLong("balanceCents") ?: 0L
                val iBal = iSnap.getLong("balanceCents") ?: 0L
                if (bBal < faceValueCents) throw IllegalStateException("Insufficient funds to subscribe to bond.")
                if (!iSnap.exists()) throw IllegalStateException("Issuer account not found.")

                tx.update(buyerRef, mapOf("balanceCents" to (bBal - faceValueCents), "updatedAt" to FieldValue.serverTimestamp()))
                tx.update(issuerRef, mapOf("balanceCents" to (iBal + faceValueCents), "updatedAt" to FieldValue.serverTimestamp()))
                tx.set(
                    bondRef,
                    mapOf(
                        "scope" to "BOND",
                        "issuerType" to issuerType,
                        "issuerName" to issuerName.take(80),
                        "issuerAccountId" to issuerAccountId,
                        "holderUid" to uid,
                        "faceValueCents" to faceValueCents,
                        "couponRatePct" to couponRatePct,
                        "maturityGameDay" to maturity,
                        "bondStatus" to "ISSUED",
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.set(
                    ledgerRef,
                    mapOf(
                        "txRef" to "BND-${UUID.randomUUID().toString().take(8).uppercase()}",
                        "initiatedByUid" to uid,
                        "senderAccountId" to buyerAccountId,
                        "receiverAccountId" to issuerAccountId,
                        "amountCents" to faceValueCents,
                        "taxAmountCents" to 0L,
                        "taxTreasuryAccountId" to "",
                        "txType" to "BOND_SUBSCRIPTION",
                        "gameDay" to currentGameDay,
                        "reference" to "$issuerType Bond: $issuerName ($couponRatePct%)",
                        "status" to "SETTLED",
                        "publicAudit" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                BondRecord(
                    docId = bondRef.id,
                    issuerType = issuerType,
                    issuerName = issuerName,
                    issuerAccountId = issuerAccountId,
                    holderUid = uid,
                    faceValueCents = faceValueCents,
                    couponRatePct = couponRatePct,
                    maturityGameDay = maturity,
                )
            }.await()
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "bonds")
            Result.failure(e)
        }
    }
}
