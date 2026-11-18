package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.Country
import com.example.data.model.County
import com.example.data.model.InternalRelocation
import com.example.data.model.StateRegion
import com.example.data.model.UserAccount
import com.example.data.seed.AfricanWorldCatalog
import com.example.data.seed.AfricanWorldCatalog.toCreateMap
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

class WorldGeographyRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id),
        ),
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must sign in with Google before accessing Firestore.")

    fun observeUserAccount(userId: String): Flow<UserAccount?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Authenticated user mismatch" }
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snap ->
                    snap.toObject(UserAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.GET, docRef.path)
                    }
                    throw error
                },
        )
    }

    fun observeCountries(): Flow<List<Country>> = flow {
        val uid = requireUserId()
        val path = "countries"
        emitAll(
            db.collection(path)
                .whereEqualTo("worldScope", "AFRICA")
                .snapshots()
                .map { snap ->
                    snap.toObjects(Country::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.countryId }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                },
        )
    }

    fun observeStatesForCountry(countryId: Long): Flow<List<StateRegion>> = flow {
        val uid = requireUserId()
        val path = "states"
        emitAll(
            db.collection(path)
                .whereEqualTo("countryId", countryId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(StateRegion::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.stateId }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                },
        )
    }

    fun observeCountiesForState(stateId: Long): Flow<List<County>> = flow {
        val uid = requireUserId()
        val path = "counties"
        emitAll(
            db.collection(path)
                .whereEqualTo("stateId", stateId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(County::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.countyId }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                },
        )
    }

    fun observeAllCounties(): Flow<List<County>> = flow {
        val uid = requireUserId()
        val path = "counties"
        emitAll(
            db.collection(path)
                .whereGreaterThanOrEqualTo("stateId", 200L)
                .snapshots()
                .map { snap ->
                    snap.toObjects(County::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.countyId }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                },
        )
    }

    fun observeRelocations(userId: String): Flow<List<InternalRelocation>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Authenticated user mismatch" }
        val path = "users/$userId/relocations"
        emitAll(
            db.collection("users")
                .document(userId)
                .collection("relocations")
                .snapshots()
                .map { snap ->
                    snap.toObjects(InternalRelocation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                },
        )
    }

    suspend fun ensureUserAccount(userId: String, displayName: String): Result<UserAccount> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Cannot initialize another user's account" }
            val docRef = db.collection("users").document(userId)
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val existing = snapshot.toObject(
                    UserAccount::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                )
                if (existing != null) return Result.success(existing)
            }
            val safeName = displayName.trim().ifBlank { "African Citizen" }.take(80)
            val payload = mapOf(
                "userId" to uid,
                "displayName" to safeName,
                "role" to "citizen",
                "activeCountryId" to 0L,
                "activeStateId" to 0L,
                "activeCountyId" to 0L,
                "mainPlayerId" to 0L,
                "activePlayerId" to 0L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            docRef.set(payload).await()
            val created = docRef.get().await().toObject(
                UserAccount::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            ) ?: UserAccount(userId = uid, displayName = safeName)
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun ensureWorldSeeded(): Result<Unit> {
        var currentPath = "countries"
        return try {
            val uid = requireUserId()
            val existingCountries = db.collection("countries")
                .whereEqualTo("worldScope", "AFRICA")
                .get()
                .await()

            val existingCountryIds = existingCountries.documents.mapNotNull { it.id.toLongOrNull() }.toSet()
            val missingCountries = AfricanWorldCatalog.countries.filter { it.countryId !in existingCountryIds }
            if (missingCountries.isNotEmpty()) {
                missingCountries.chunked(40).forEach { chunk ->
                    val batch = db.batch()
                    chunk.forEach { country ->
                        batch.set(db.collection("countries").document(country.countryId.toString()), country.toCreateMap())
                    }
                    batch.commit().await()
                }
            }

            currentPath = "states"
            val existingStates = db.collection("states")
                .whereGreaterThanOrEqualTo("countryId", 100L)
                .get()
                .await()
            val existingStateIds = existingStates.documents.mapNotNull { it.id.toLongOrNull() }.toSet()
            val missingStates = AfricanWorldCatalog.states.filter { it.stateId !in existingStateIds }
            if (missingStates.isNotEmpty()) {
                missingStates.chunked(40).forEach { chunk ->
                    val batch = db.batch()
                    chunk.forEach { state ->
                        batch.set(db.collection("states").document(state.stateId.toString()), state.toCreateMap())
                    }
                    batch.commit().await()
                }
            }

            currentPath = "counties"
            val existingCounties = db.collection("counties")
                .whereGreaterThanOrEqualTo("stateId", 200L)
                .get()
                .await()
            val existingCountyIds = existingCounties.documents.mapNotNull { it.id.toLongOrNull() }.toSet()
            val missingCounties = AfricanWorldCatalog.counties.filter { it.countyId !in existingCountyIds }
            if (missingCounties.isNotEmpty()) {
                missingCounties.chunked(40).forEach { chunk ->
                    val batch = db.batch()
                    chunk.forEach { county ->
                        batch.set(db.collection("counties").document(county.countyId.toString()), county.toCreateMap())
                    }
                    batch.commit().await()
                }
            }

            // Seed initial governments & sovereign treasuries + county market escrow accounts if missing
            currentPath = "governments"
            val existingGovs = db.collection("governments")
                .whereEqualTo("scope", "GOVERNMENT")
                .get()
                .await()
                .documents
                .map { it.id }
                .toSet()

            val govWrites = mutableListOf<Pair<String, Map<String, Any>>>()
            val treasuryWrites = mutableListOf<Pair<String, Map<String, Any>>>()

            AfricanWorldCatalog.countries.forEach { c ->
                val govId = "gov_country_${c.countryId}"
                val treasuryId = "treasury_country_${c.countryId}"
                if (govId !in existingGovs) {
                    govWrites += govId to mapOf(
                        "scope" to "GOVERNMENT",
                        "jurisdictionLevel" to "NATIONAL",
                        "jurisdictionId" to c.countryId,
                        "jurisdictionName" to c.name,
                        "leaderUid" to "CARETAKER_NPC",
                        "leaderName" to "${c.parliamentName} (Caretaker)",
                        "treasuryAccountId" to treasuryId,
                        "primaryTaxRatePct" to c.incomeTaxRatePct,
                        "secondaryTaxRatePct" to c.corporateTaxRatePct,
                        "salesTaxRatePct" to c.salesTaxRatePct,
                        "budgetInfrastructureCents" to 250_000_000_00L,
                        "publicDebtCents" to 0L,
                        "departments" to AfricanWorldCatalog.statutoryDepartments,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                    treasuryWrites += treasuryId to mapOf(
                        "ownerUid" to uid,
                        "ownerEntityId" to govId,
                        "accountType" to "NATIONAL_TREASURY",
                        "accountCategory" to "TREASURY",
                        "currencyCode" to c.currencyCode,
                        "balanceCents" to 500_000_000_00L,
                        "restricted" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                }
            }

            AfricanWorldCatalog.states.forEach { s ->
                val govId = "gov_state_${s.stateId}"
                val treasuryId = "treasury_state_${s.stateId}"
                val currency = AfricanWorldCatalog.countries.firstOrNull { it.countryId == s.countryId }?.currencyCode ?: "NGN"
                if (govId !in existingGovs) {
                    govWrites += govId to mapOf(
                        "scope" to "GOVERNMENT",
                        "jurisdictionLevel" to "STATE",
                        "jurisdictionId" to s.stateId,
                        "jurisdictionName" to s.name,
                        "leaderUid" to "CARETAKER_NPC",
                        "leaderName" to "${s.governorTitle} (Caretaker)",
                        "treasuryAccountId" to treasuryId,
                        "primaryTaxRatePct" to s.stateTaxRatePct,
                        "secondaryTaxRatePct" to 0.0,
                        "salesTaxRatePct" to 0.0,
                        "budgetInfrastructureCents" to 80_000_000_00L,
                        "publicDebtCents" to 0L,
                        "departments" to s.departments,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                    treasuryWrites += treasuryId to mapOf(
                        "ownerUid" to uid,
                        "ownerEntityId" to govId,
                        "accountType" to "STATE_TREASURY",
                        "accountCategory" to "TREASURY",
                        "currencyCode" to currency,
                        "balanceCents" to 150_000_000_00L,
                        "restricted" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                }
            }

            AfricanWorldCatalog.counties.forEach { ct ->
                val govId = "gov_county_${ct.countyId}"
                val treasuryId = "treasury_county_${ct.countyId}"
                val escrowId = "escrow_county_${ct.countyId}"
                val currency = AfricanWorldCatalog.countries.firstOrNull { it.countryId == ct.countryId }?.currencyCode ?: "NGN"
                if (govId !in existingGovs) {
                    govWrites += govId to mapOf(
                        "scope" to "GOVERNMENT",
                        "jurisdictionLevel" to "COUNTY",
                        "jurisdictionId" to ct.countyId,
                        "jurisdictionName" to ct.name,
                        "leaderUid" to "CARETAKER_NPC",
                        "leaderName" to "${ct.name} Administration (Caretaker)",
                        "treasuryAccountId" to treasuryId,
                        "primaryTaxRatePct" to 2.0,
                        "secondaryTaxRatePct" to 0.0,
                        "salesTaxRatePct" to 0.0,
                        "budgetInfrastructureCents" to 25_000_000_00L,
                        "publicDebtCents" to 0L,
                        "departments" to ct.localServices,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                    treasuryWrites += treasuryId to mapOf(
                        "ownerUid" to uid,
                        "ownerEntityId" to govId,
                        "accountType" to "COUNTY_TREASURY",
                        "accountCategory" to "TREASURY",
                        "currencyCode" to currency,
                        "balanceCents" to 50_000_000_00L,
                        "restricted" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                    treasuryWrites += escrowId to mapOf(
                        "ownerUid" to uid,
                        "ownerEntityId" to "market_${ct.countyId}",
                        "accountType" to "MARKET_ESCROW",
                        "accountCategory" to "CORPORATE",
                        "currencyCode" to currency,
                        "balanceCents" to 0L,
                        "restricted" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                }
            }

            if (govWrites.isNotEmpty()) {
                govWrites.chunked(40).forEach { chunk ->
                    val batch = db.batch()
                    chunk.forEach { (id, payload) ->
                        batch.set(db.collection("governments").document(id), payload)
                    }
                    batch.commit().await()
                }
            }
            if (treasuryWrites.isNotEmpty()) {
                currentPath = "bank_accounts"
                treasuryWrites.chunked(40).forEach { chunk ->
                    val batch = db.batch()
                    chunk.forEach { (id, payload) ->
                        batch.set(db.collection("bank_accounts").document(id), payload)
                    }
                    batch.commit().await()
                }
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, currentPath)
            Result.failure(e)
        }
    }

    suspend fun settleOrRelocateWithinCountry(
        userId: String,
        targetCountryId: Long,
        targetStateId: Long,
        targetCountyId: Long,
        countyLabel: String,
        stateLabel: String,
    ): Result<String> {
        val userPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Authenticated user mismatch" }

            val userRef = db.collection("users").document(uid)
            val userSnap = userRef.get().await()
            val account = userSnap.toObject(
                UserAccount::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            ) ?: return Result.failure(IllegalStateException("Citizen account record not initialized yet."))

            if (account.activeCountryId != 0L && account.activeCountryId != targetCountryId) {
                return Result.failure(
                    IllegalStateException(
                        "Cross-border migration requires Exit Clearance and an Entry Permit. Use the Immigration tab in Profile or request cross-border immigration below.",
                    ),
                )
            }

            if (account.activeStateId == targetStateId && account.activeCountyId == targetCountyId) {
                return Result.success("You are already settled in $countyLabel, $stateLabel.")
            }

            val isInitial = account.activeCountryId == 0L
            val relocationType = if (isInitial) "INITIAL_SETTLEMENT" else "INTERNAL_RELOCATION"
            val noteText = if (isInitial) {
                "Initial settlement in $countyLabel, $stateLabel"
            } else {
                "Internal relocation to $countyLabel, $stateLabel"
            }.take(200)

            val relocationRef = userRef.collection("relocations").document()
            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "activeCountryId" to targetCountryId,
                    "activeStateId" to targetStateId,
                    "activeCountyId" to targetCountyId,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
            batch.set(
                relocationRef,
                mapOf(
                    "countryId" to targetCountryId,
                    "fromStateId" to account.activeStateId,
                    "fromCountyId" to account.activeCountyId,
                    "toStateId" to targetStateId,
                    "toCountyId" to targetCountyId,
                    "relocationType" to relocationType,
                    "note" to noteText,
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            )
            batch.commit().await()

            Result.success(noteText)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userPath)
            Result.failure(e)
        }
    }
}
