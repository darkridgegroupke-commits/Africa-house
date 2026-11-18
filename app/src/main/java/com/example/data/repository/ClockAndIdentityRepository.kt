package com.example.data.repository

import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.AppNotification
import com.example.data.model.ClockTickLog
import com.example.data.model.ImmigrationRecord
import com.example.data.model.PlayerCharacter
import com.example.data.model.PrivacySecuritySettings
import com.example.data.model.WorldClock
import com.example.domain.GameClockEngine
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
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
 * Repository for Update 2 (Server-Controlled Game Clock + Idempotent Recovery),
 * Update 3 (Player Identity, Main + ALT Characters, Avatar, Public/Private Profiles, Immigration),
 * and Update 12 (Security/Privacy Settings & Audit Notifications).
 */
class ClockAndIdentityRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    private fun requireUid(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("Google Sign-In required.")

    fun observeWorldClock(): Flow<WorldClock?> = flow {
        val uid = requireUid()
        val ref = db.collection("world_clock").document("canonical")
        emitAll(
            ref.snapshots()
                .map { it.toObject(WorldClock::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.GET, ref.path)
                    throw e
                },
        )
    }

    fun observeClockTickLogs(): Flow<List<ClockTickLog>> = flow {
        val uid = requireUid()
        val path = "world_clock/canonical/tick_logs"
        emitAll(
            db.collection("world_clock").document("canonical").collection("tick_logs")
                .whereEqualTo("scope", "CLOCK_TICK")
                .snapshots()
                .map { snap ->
                    snap.toObjects(ClockTickLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.tickIndex }
                        .take(25)
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == uid) handleFirestoreError(fe ?: e, OperationType.LIST, path)
                    throw e
                },
        )
    }

    fun observeMyCharacters(uid: String): Flow<List<PlayerCharacter>> = flow {
        val authed = requireUid()
        check(authed == uid)
        emitAll(
            db.collection("players")
                .whereEqualTo("ownerUid", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(PlayerCharacter::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.playerId }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == authed) handleFirestoreError(fe ?: e, OperationType.LIST, "players")
                    throw e
                },
        )
    }

    fun observeImmigrationRecords(uid: String): Flow<List<ImmigrationRecord>> = flow {
        val authed = requireUid()
        check(authed == uid)
        emitAll(
            db.collection("immigration_records")
                .whereEqualTo("ownerUid", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(ImmigrationRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.issuedAtGameDay }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == authed) handleFirestoreError(fe ?: e, OperationType.LIST, "immigration_records")
                    throw e
                },
        )
    }

    fun observeNotifications(uid: String): Flow<List<AppNotification>> = flow {
        val authed = requireUid()
        check(authed == uid)
        val path = "users/$uid/notifications"
        emitAll(
            db.collection("users").document(uid).collection("notifications")
                .snapshots()
                .map { snap ->
                    snap.toObjects(AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == authed) handleFirestoreError(fe ?: e, OperationType.LIST, path)
                    throw e
                },
        )
    }

    fun observePrivacySecurity(uid: String): Flow<PrivacySecuritySettings?> = flow {
        val authed = requireUid()
        check(authed == uid)
        val ref = db.collection("users").document(uid).collection("privacy_security").document("settings")
        emitAll(
            ref.snapshots()
                .map { it.toObject(PrivacySecuritySettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { e ->
                    val fe = generateSequence(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe == null && e is CancellationException) throw e
                    if (auth.currentUser?.uid == authed) handleFirestoreError(fe ?: e, OperationType.GET, ref.path)
                    throw e
                },
        )
    }

    /**
     * Ensures canonical server clock exists, stamps server time via FieldValue.serverTimestamp(),
     * and processes any missed ticks idempotently (safe against duplicate runs).
     * Never uses player's device wall-clock as authoritative time.
     */
    suspend fun ensureAndSyncServerClock(): Result<WorldClock> {
        val clockRef = db.collection("world_clock").document("canonical")
        return try {
            val uid = requireUid()
            val snap = clockRef.get().await()
            if (!snap.exists()) {
                val initPayload = mapOf(
                    "scope" to "WORLD",
                    "eraNumber" to 1,
                    "eraStartTime" to FieldValue.serverTimestamp(),
                    "lastTickTime" to FieldValue.serverTimestamp(),
                    "lastProcessedTickIndex" to 0L,
                    "totalGameDaysElapsed" to 0.0,
                )
                clockRef.set(initPayload).await()
            } else {
                // Touch server timestamp cleanly so server updates lastTickTime without altering eraStartTime
                clockRef.update(mapOf("lastTickTime" to FieldValue.serverTimestamp())).await()
            }

            // Read back server-resolved timestamps (falling back to default if offline emulator caches)
            val freshSnap = try {
                clockRef.get(Source.SERVER).await()
            } catch (_: Exception) {
                clockRef.get().await()
            }
            val currentClock = freshSnap.toObject(
                WorldClock::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            ) ?: WorldClock()

            val eraStartMs = currentClock.eraStartTime?.toDate()?.time ?: 0L
            val serverNowMs = currentClock.lastTickTime?.toDate()?.time ?: eraStartMs
            val window = GameClockEngine.computeCatchUpWindow(
                lastProcessedTickIndex = currentClock.lastProcessedTickIndex,
                eraStartMillis = eraStartMs,
                serverNowMillis = serverNowMs,
            )

            if (window.hasPendingTicks || window.totalGameDaysAtTarget > currentClock.totalGameDaysElapsed) {
                val tickId = "tick_${window.targetTickIndex}"
                val tickLogRef = clockRef.collection("tick_logs").document(tickId)
                db.runTransaction { tx ->
                    val latestClock = tx.get(clockRef)
                    val latestTickIdx = latestClock.getLong("lastProcessedTickIndex") ?: 0L
                    val existingTick = tx.get(tickLogRef)
                    val targetIdx = maxOf(latestTickIdx, window.targetTickIndex)
                    tx.update(
                        clockRef,
                        mapOf(
                            "lastTickTime" to FieldValue.serverTimestamp(),
                            "lastProcessedTickIndex" to targetIdx,
                            "totalGameDaysElapsed" to maxOf(
                                latestClock.getDouble("totalGameDaysElapsed") ?: 0.0,
                                window.totalGameDaysAtTarget,
                            ),
                        ),
                    )
                    if (!existingTick.exists() && window.ticksToProcess > 0) {
                        tx.set(
                            tickLogRef,
                            mapOf(
                                "scope" to "CLOCK_TICK",
                                "tickIndex" to targetIdx,
                                "gameDaysAtTick" to window.totalGameDaysAtTarget,
                                "eventsProcessed" to "Synced ${window.ticksToProcess} missed game day(s) idempotently",
                                "processedByUid" to uid,
                                "processedAt" to FieldValue.serverTimestamp(),
                            ),
                        )
                    }
                }.await()
            }

            val updatedClock = clockRef.get().await().toObject(
                WorldClock::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            ) ?: currentClock
            Result.success(updatedClock)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, clockRef.path)
            Result.failure(e)
        }
    }

    /**
     * Advances one scheduled simulation cycle explicitly (authorized world tick log + server clock sync)
     * for testing and player-driven tick synchronization with strict tick-log idempotency.
     */
    suspend fun recordScheduledTick(note: String = "Scheduled Economic & Civic Tick"): Result<WorldClock> {
        val clockRef = db.collection("world_clock").document("canonical")
        return try {
            val uid = requireUid()
            val updated = db.runTransaction { tx ->
                val snap = tx.get(clockRef)
                val era = (snap.getLong("eraNumber") ?: 1L).toInt()
                val lastIdx = snap.getLong("lastProcessedTickIndex") ?: 0L
                val nextIdx = lastIdx + 1L
                val prevDays = snap.getDouble("totalGameDaysElapsed") ?: 0.0
                val nextDays = maxOf(prevDays + 1.0, nextIdx.toDouble())
                val tickLogRef = clockRef.collection("tick_logs").document("tick_$nextIdx")

                if (!snap.exists()) {
                    tx.set(
                        clockRef,
                        mapOf(
                            "scope" to "WORLD",
                            "eraNumber" to 1,
                            "eraStartTime" to FieldValue.serverTimestamp(),
                            "lastTickTime" to FieldValue.serverTimestamp(),
                            "lastProcessedTickIndex" to nextIdx,
                            "totalGameDaysElapsed" to nextDays,
                        ),
                    )
                } else {
                    tx.update(
                        clockRef,
                        mapOf(
                            "lastTickTime" to FieldValue.serverTimestamp(),
                            "lastProcessedTickIndex" to nextIdx,
                            "totalGameDaysElapsed" to nextDays,
                        ),
                    )
                }
                tx.set(
                    tickLogRef,
                    mapOf(
                        "scope" to "CLOCK_TICK",
                        "tickIndex" to nextIdx,
                        "gameDaysAtTick" to nextDays,
                        "eventsProcessed" to note.take(160),
                        "processedByUid" to uid,
                        "processedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                WorldClock(
                    docId = "canonical",
                    scope = "WORLD",
                    eraNumber = era,
                    lastProcessedTickIndex = nextIdx,
                    totalGameDaysElapsed = nextDays,
                )
            }.await()
            Result.success(updated)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, clockRef.path)
            Result.failure(e)
        }
    }

    suspend fun checkUsernameAvailability(rawUsername: String): Result<Boolean> {
        return try {
            requireUid()
            val normalized = com.example.domain.OnboardingEngine.normalizeUsername(rawUsername)
            if (com.example.domain.OnboardingEngine.validateUsername(rawUsername) is com.example.domain.OnboardingEngine.ValidationResult.Invalid) {
                return Result.success(false)
            }
            val snap = db.collection("usernames").document(normalized).get().await()
            Result.success(!snap.exists())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "usernames")
            Result.failure(e)
        }
    }

    suspend fun completeAccountRegistration(
        username: String,
        email: String,
        displayName: String,
        emailVerified: Boolean,
    ): Result<String> {
        return try {
            val uid = requireUid()
            val cleanUsername = username.trim()
            val usernameLower = com.example.domain.OnboardingEngine.normalizeUsername(cleanUsername)
            when (val uVal = com.example.domain.OnboardingEngine.validateUsername(cleanUsername)) {
                is com.example.domain.OnboardingEngine.ValidationResult.Invalid ->
                    return Result.failure(IllegalArgumentException(uVal.message))
                else -> {}
            }
            when (val eVal = com.example.domain.OnboardingEngine.validateEmail(email)) {
                is com.example.domain.OnboardingEngine.ValidationResult.Invalid ->
                    return Result.failure(IllegalArgumentException(eVal.message))
                else -> {}
            }

            val usernameRef = db.collection("usernames").document(usernameLower)
            val userRef = db.collection("users").document(uid)
            val securityRef = userRef.collection("privacy_security").document("settings")

            db.runTransaction { tx ->
                val unameSnap = tx.get(usernameRef)
                val userSnap = tx.get(userRef)
                val secSnap = tx.get(securityRef)

                if (unameSnap.exists() && unameSnap.getString("ownerUid") != uid) {
                    throw IllegalStateException("Username @$cleanUsername is already taken. Please choose another.")
                }

                if (!unameSnap.exists()) {
                    tx.set(
                        usernameRef,
                        mapOf(
                            "username" to cleanUsername,
                            "usernameLower" to usernameLower,
                            "ownerUid" to uid,
                            "createdAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                }

                val safeDisplayName = displayName.trim().ifBlank { cleanUsername }.take(80)
                if (!userSnap.exists()) {
                    tx.set(
                        userRef,
                        mapOf(
                            "userId" to uid,
                            "displayName" to safeDisplayName,
                            "username" to cleanUsername,
                            "email" to email.trim().take(120),
                            "emailVerified" to emailVerified,
                            "termsAcceptedAt" to FieldValue.serverTimestamp(),
                            "role" to "citizen",
                            "activeCountryId" to 0L,
                            "activeStateId" to 0L,
                            "activeCountyId" to 0L,
                            "mainPlayerId" to 0L,
                            "activePlayerId" to 0L,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                } else {
                    tx.update(
                        userRef,
                        mapOf(
                            "displayName" to safeDisplayName,
                            "username" to cleanUsername,
                            "email" to email.trim().take(120),
                            "emailVerified" to emailVerified,
                            "termsAcceptedAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                }

                if (!secSnap.exists()) {
                    tx.set(
                        securityRef,
                        mapOf(
                            "ownerUid" to uid,
                            "twoFactorEnabled" to true,
                            "emailVerified" to emailVerified,
                            "showNationality" to true,
                            "showResidence" to true,
                            "showOccupation" to true,
                            "showPublicActivity" to false,
                            "showNetWorth" to false,
                            "showPersonalBalance" to false,
                            "showCapitalBalance" to false,
                            "showEmployment" to true,
                            "showCompanies" to true,
                            "showPolitics" to true,
                            "showPersonalIdentity" to false,
                            "notifySalaries" to true,
                            "notifyTaxes" to true,
                            "notifyElections" to true,
                            "notifyCompanies" to true,
                            "notifyMarketplace" to true,
                            "allowFriendRequests" to true,
                            "allowDirectMessages" to true,
                            "publicProfileSearchable" to true,
                            "rateLimitWindowSec" to 60,
                            "antiAbuseSignalMode" to "CARRIER_NAT_SAFE_FINGERPRINT",
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                }
            }.await()

            pushNotification(
                uid = uid,
                category = "System",
                title = "Account Verified (@$cleanUsername)",
                detail = "Welcome to House of Africa. Proceed to create your Main Citizen character.",
                refId = usernameLower,
            )
            Result.success(cleanUsername)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "usernames")
            Result.failure(e)
        }
    }

    suspend fun syncEmailVerificationStatus(emailVerified: Boolean): Result<Unit> {
        return try {
            val uid = requireUid()
            val userRef = db.collection("users").document(uid)
            val secRef = userRef.collection("privacy_security").document("settings")
            db.runBatch { batch ->
                batch.update(
                    userRef,
                    mapOf(
                        "emailVerified" to emailVerified,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                batch.set(
                    secRef,
                    mapOf(
                        "ownerUid" to uid,
                        "emailVerified" to emailVerified,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                    com.google.firebase.firestore.SetOptions.merge(),
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users")
            Result.failure(e)
        }
    }

    suspend fun createCharacter(
        fullName: String,
        genderPresentation: String,
        dateOfBirth: String,
        nationalityCountryId: Long,
        placeOfBirth: String,
        currentCountryId: Long,
        currentStateId: Long,
        currentCountyId: Long,
        occupation: String,
        biography: String,
        avatarStyle: String,
        avatarAccentIndex: Int,
        asAltCharacter: Boolean,
        firstName: String = "",
        middleName: String = "",
        lastName: String = "",
        birthCountryId: Long = nationalityCountryId,
        birthStateId: Long = currentStateId,
        birthCountyId: Long = currentCountyId,
        settlementName: String = "",
        backgroundStory: String = "",
        personalInterests: List<String> = emptyList(),
        avatarSkinToneIdx: Int = 2,
        avatarHairStyle: String = "SHORT_FADE",
        avatarHairColorIdx: Int = 0,
        avatarFaceStyle: String = "CONFIDENT",
        avatarClothingStyle: String = "KENTE_ROBE",
        avatarAccessory: String = "NONE",
        currencyCode: String = "NGN",
    ): Result<PlayerCharacter> {
        return try {
            val uid = requireUid()
            val cleanName = fullName.trim()
            if (cleanName.length < 2) {
                return Result.failure(IllegalArgumentException("Citizen name must be at least 2 characters."))
            }

            // Check main character limit (only 1 MAIN per account)
            val existingChars = db.collection("players")
                .whereEqualTo("ownerUid", uid)
                .get()
                .await()
                .toObjects(PlayerCharacter::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val hasMain = existingChars.any { it.characterRole == "MAIN" && it.accountStatus != "DELETED" }
            val role = if (asAltCharacter && hasMain) "ALT" else if (!hasMain) "MAIN" else "ALT"

            val counterRef = db.collection("system_counters").document("player_seq")
            val userRef = db.collection("users").document(uid)
            val grantKeyRef = db.collection("idempotency_keys").document("onboarding_grant_$uid")
            val personalAcctRef = db.collection("bank_accounts").document("acct_${uid}_personal")
            val capitalAcctRef = db.collection("bank_accounts").document("acct_${uid}_capital")
            val relocRef = userRef.collection("relocations").document()
            val personalLedgerRef = db.collection("ledger_transactions").document()
            val capitalLedgerRef = db.collection("ledger_transactions").document()

            val created = db.runTransaction { tx ->
                // All Firestore transaction reads MUST precede all writes
                val counterSnap = tx.get(counterRef)
                val userSnap = tx.get(userRef)
                val grantSnap = tx.get(grantKeyRef)
                val personalSnap = tx.get(personalAcctRef)
                val capitalSnap = tx.get(capitalAcctRef)

                val allocatedId = if (!counterSnap.exists()) {
                    tx.set(counterRef, mapOf("nextId" to 12454L))
                    12453L
                } else {
                    val currentNext = counterSnap.getLong("nextId") ?: 12453L
                    tx.update(counterRef, mapOf("nextId" to currentNext + 1L))
                    currentNext
                }

                val playerDocRef = db.collection("players").document(allocatedId.toString())
                val safeBirthPlace = placeOfBirth.trim().ifBlank { "Capital District" }.take(80)
                val safeBio = biography.trim().ifBlank { "Citizen of House of Africa" }.take(400)
                val safeOccupation = occupation.trim().ifBlank { "Citizen" }.take(80)
                val safeCurrency = currencyCode.trim().ifBlank { "NGN" }.take(6)

                val payload = mapOf(
                    "playerId" to allocatedId,
                    "ownerUid" to uid,
                    "characterRole" to role,
                    "fullName" to cleanName.take(80),
                    "firstName" to firstName.trim().take(40),
                    "middleName" to middleName.trim().take(40),
                    "lastName" to lastName.trim().take(40),
                    "genderPresentation" to genderPresentation.trim().ifBlank { "Non-specified" }.take(40),
                    "dateOfBirth" to dateOfBirth.trim().ifBlank { "Yr 1, Jan 1" }.take(32),
                    "nationalityCountryId" to nationalityCountryId,
                    "placeOfBirth" to safeBirthPlace,
                    "birthCountryId" to birthCountryId,
                    "birthStateId" to birthStateId,
                    "birthCountyId" to birthCountyId,
                    "currentCountryId" to currentCountryId,
                    "currentStateId" to currentStateId,
                    "currentCountyId" to currentCountyId,
                    "settlementName" to settlementName.trim().take(80),
                    "occupation" to safeOccupation,
                    "biography" to safeBio,
                    "backgroundStory" to backgroundStory.trim().take(400),
                    "personalInterests" to personalInterests.take(6),
                    "avatarStyle" to avatarStyle.take(40),
                    "avatarAccentIndex" to avatarAccentIndex.coerceIn(0, 10),
                    "avatarSkinToneIdx" to avatarSkinToneIdx.coerceIn(0, 5),
                    "avatarHairStyle" to avatarHairStyle.take(32),
                    "avatarHairColorIdx" to avatarHairColorIdx.coerceIn(0, 4),
                    "avatarFaceStyle" to avatarFaceStyle.take(32),
                    "avatarClothingStyle" to avatarClothingStyle.take(32),
                    "avatarAccessory" to avatarAccessory.take(32),
                    "accountStatus" to "ACTIVE",
                    "publicVisible" to true,
                    "showNationality" to true,
                    "showResidence" to true,
                    "showOccupation" to true,
                    "showActivity" to false,
                    "showNetWorth" to false,
                    "showPersonalBalance" to false,
                    "showCapitalBalance" to false,
                    "showEmployment" to true,
                    "showCompanies" to true,
                    "showPolitics" to true,
                    "showPersonalIdentity" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                tx.set(playerDocRef, payload)

                if (userSnap.exists()) {
                    val userUpdates = mutableMapOf<String, Any>(
                        "activePlayerId" to allocatedId,
                        "activeCountryId" to currentCountryId,
                        "activeStateId" to currentStateId,
                        "activeCountyId" to currentCountyId,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                    if (role == "MAIN") {
                        userUpdates["mainPlayerId"] = allocatedId
                    }
                    tx.update(userRef, userUpdates)
                }

                val relocationNote = "Initial settlement in ${settlementName.ifBlank { safeBirthPlace }}, /county/$currentCountyId".take(200)
                if (role == "MAIN") {
                    tx.set(
                        relocRef,
                        mapOf(
                            "countryId" to currentCountryId,
                            "fromStateId" to 0L,
                            "fromCountyId" to 0L,
                            "toStateId" to currentStateId,
                            "toCountyId" to currentCountyId,
                            "relocationType" to "INITIAL_SETTLEMENT",
                            "note" to relocationNote,
                            "createdAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                }

                // Explicit bounded onboarding initial balance grant (awarded strictly once per UID on MAIN creation)
                if (role == "MAIN" && !grantSnap.exists()) {
                    val perGrant = com.example.domain.FinancialEngines.INITIAL_PERSONAL_BALANCE_CENTS
                    val capGrant = com.example.domain.FinancialEngines.INITIAL_CAPITAL_BALANCE_CENTS

                    tx.set(
                        grantKeyRef,
                        mapOf(
                            "ownerUid" to uid,
                            "playerId" to allocatedId,
                            "createdAt" to FieldValue.serverTimestamp(),
                        ),
                    )

                    if (!personalSnap.exists()) {
                        tx.set(
                            personalAcctRef,
                            mapOf(
                                "ownerUid" to uid,
                                "ownerEntityId" to uid,
                                "accountType" to "PERSONAL",
                                "accountCategory" to "CITIZEN",
                                "currencyCode" to safeCurrency,
                                "balanceCents" to perGrant,
                                "restricted" to false,
                                "createdAt" to FieldValue.serverTimestamp(),
                                "updatedAt" to FieldValue.serverTimestamp(),
                            ),
                        )
                    } else {
                        val existingPer = personalSnap.getLong("balanceCents") ?: 0L
                        tx.update(
                            personalAcctRef,
                            mapOf(
                                "currencyCode" to safeCurrency,
                                "balanceCents" to (existingPer + perGrant),
                                "updatedAt" to FieldValue.serverTimestamp(),
                            ),
                        )
                    }

                    if (!capitalSnap.exists()) {
                        tx.set(
                            capitalAcctRef,
                            mapOf(
                                "ownerUid" to uid,
                                "ownerEntityId" to uid,
                                "accountType" to "CAPITAL",
                                "accountCategory" to "CITIZEN",
                                "currencyCode" to safeCurrency,
                                "balanceCents" to capGrant,
                                "restricted" to false,
                                "createdAt" to FieldValue.serverTimestamp(),
                                "updatedAt" to FieldValue.serverTimestamp(),
                            ),
                        )
                    } else {
                        val existingCap = capitalSnap.getLong("balanceCents") ?: 0L
                        tx.update(
                            capitalAcctRef,
                            mapOf(
                                "currencyCode" to safeCurrency,
                                "balanceCents" to (existingCap + capGrant),
                                "updatedAt" to FieldValue.serverTimestamp(),
                            ),
                        )
                    }

                    tx.set(
                        personalLedgerRef,
                        mapOf(
                            "txRef" to "ONB-PER-${allocatedId}",
                            "initiatedByUid" to uid,
                            "senderAccountId" to "onboarding_config_reserve",
                            "receiverAccountId" to personalAcctRef.id,
                            "amountCents" to perGrant,
                            "taxAmountCents" to 0L,
                            "taxTreasuryAccountId" to "",
                            "txType" to "ONBOARDING_PERSONAL_GRANT",
                            "gameDay" to 0.0,
                            "reference" to "Configured initial Personal stipend for Citizen #$allocatedId",
                            "status" to "SETTLED",
                            "publicAudit" to false,
                            "createdAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                    tx.set(
                        capitalLedgerRef,
                        mapOf(
                            "txRef" to "ONB-CAP-${allocatedId}",
                            "initiatedByUid" to uid,
                            "senderAccountId" to "onboarding_config_reserve",
                            "receiverAccountId" to capitalAcctRef.id,
                            "amountCents" to capGrant,
                            "taxAmountCents" to 0L,
                            "taxTreasuryAccountId" to "",
                            "txType" to "ONBOARDING_CAPITAL_GRANT",
                            "gameDay" to 0.0,
                            "reference" to "Configured initial Capital endowment for Citizen #$allocatedId",
                            "status" to "SETTLED",
                            "publicAudit" to false,
                            "createdAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                }

                PlayerCharacter(
                    docId = allocatedId.toString(),
                    playerId = allocatedId,
                    ownerUid = uid,
                    characterRole = role,
                    fullName = cleanName.take(80),
                    firstName = firstName.trim().take(40),
                    middleName = middleName.trim().take(40),
                    lastName = lastName.trim().take(40),
                    genderPresentation = genderPresentation.trim().ifBlank { "Non-specified" },
                    dateOfBirth = dateOfBirth.trim().ifBlank { "Yr 1, Jan 1" },
                    nationalityCountryId = nationalityCountryId,
                    placeOfBirth = safeBirthPlace,
                    birthCountryId = birthCountryId,
                    birthStateId = birthStateId,
                    birthCountyId = birthCountyId,
                    currentCountryId = currentCountryId,
                    currentStateId = currentStateId,
                    currentCountyId = currentCountyId,
                    settlementName = settlementName.trim().take(80),
                    occupation = safeOccupation,
                    biography = safeBio,
                    backgroundStory = backgroundStory.trim().take(400),
                    personalInterests = personalInterests.take(6),
                    avatarStyle = avatarStyle,
                    avatarAccentIndex = avatarAccentIndex.coerceIn(0, 10),
                    avatarSkinToneIdx = avatarSkinToneIdx.coerceIn(0, 5),
                    avatarHairStyle = avatarHairStyle,
                    avatarHairColorIdx = avatarHairColorIdx.coerceIn(0, 4),
                    avatarFaceStyle = avatarFaceStyle,
                    avatarClothingStyle = avatarClothingStyle,
                    avatarAccessory = avatarAccessory,
                    accountStatus = "ACTIVE",
                )
            }.await()

            pushNotification(
                uid = uid,
                category = "System",
                title = "${created.characterRole} Citizen Registered (#${created.playerId})",
                detail = "${created.fullName} settled at ${created.canonicalUrl()}",
                refId = created.docId,
            )
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "players")
            Result.failure(e)
        }
    }

    suspend fun switchActiveCharacter(playerId: Long): Result<Unit> {
        val uid = requireUid()
        return try {
            val snap = db.collection("players").document(playerId.toString()).get().await()
            val character = snap.toObject(PlayerCharacter::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: return Result.failure(IllegalArgumentException("Player Not Found"))
            if (character.ownerUid != uid) {
                return Result.failure(IllegalAccessException("Cannot switch to another player's character."))
            }
            if (character.accountStatus != "ACTIVE") {
                return Result.failure(IllegalStateException(character.resolvePublicDisplayName()))
            }
            db.collection("users").document(uid).update(
                mapOf(
                    "activePlayerId" to character.playerId,
                    "activeCountryId" to character.currentCountryId,
                    "activeStateId" to character.currentStateId,
                    "activeCountyId" to character.currentCountyId,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid")
            Result.failure(e)
        }
    }

    suspend fun updateAvatarAndBio(
        playerId: Long,
        avatarStyle: String,
        avatarAccentIndex: Int,
        occupation: String,
        biography: String,
        publicVisible: Boolean,
        avatarSkinToneIdx: Int? = null,
        avatarHairStyle: String? = null,
        avatarHairColorIdx: Int? = null,
        avatarFaceStyle: String? = null,
        avatarClothingStyle: String? = null,
        avatarAccessory: String? = null,
        personalInterests: List<String>? = null,
    ): Result<Unit> {
        return try {
            requireUid()
            val updates = mutableMapOf<String, Any>(
                "avatarStyle" to avatarStyle.take(40),
                "avatarAccentIndex" to avatarAccentIndex.coerceIn(0, 10),
                "occupation" to occupation.trim().ifBlank { "Citizen" }.take(80),
                "biography" to biography.trim().take(400),
                "publicVisible" to publicVisible,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            avatarSkinToneIdx?.let { updates["avatarSkinToneIdx"] = it.coerceIn(0, 5) }
            avatarHairStyle?.let { updates["avatarHairStyle"] = it.take(32) }
            avatarHairColorIdx?.let { updates["avatarHairColorIdx"] = it.coerceIn(0, 4) }
            avatarFaceStyle?.let { updates["avatarFaceStyle"] = it.take(32) }
            avatarClothingStyle?.let { updates["avatarClothingStyle"] = it.take(32) }
            avatarAccessory?.let { updates["avatarAccessory"] = it.take(32) }
            personalInterests?.let { updates["personalInterests"] = it.take(6) }

            db.collection("players").document(playerId.toString()).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "players/$playerId")
            Result.failure(e)
        }
    }

    suspend fun softDeleteCharacter(playerId: Long): Result<Unit> {
        return try {
            val uid = requireUid()
            db.collection("players").document(playerId.toString()).update(
                mapOf(
                    "accountStatus" to "DELETED",
                    "publicVisible" to false,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            pushNotification(
                uid = uid,
                category = "System",
                title = "Character #$playerId Marked Deleted",
                detail = "Historical ledgers now render 'User Deleted' without deleting audit history.",
                refId = playerId.toString(),
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "players/$playerId")
            Result.failure(e)
        }
    }

    suspend fun lookupPublicPlayerById(playerId: Long): Result<Pair<String, PlayerCharacter?>> {
        return try {
            requireUid()
            if (playerId <= 0L) {
                return Result.success("Player Not Found" to null)
            }
            val snap = db.collection("players").document(playerId.toString()).get().await()
            if (!snap.exists()) {
                return Result.success("Player Not Found" to null)
            }
            val player = snap.toObject(PlayerCharacter::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: return Result.success("Player Not Found" to null)
            Result.success(player.resolvePublicDisplayName() to player)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "players/$playerId")
            Result.failure(e)
        }
    }

    /**
     * Update 3: International Immigration (Exit Clearance + Entry/Residence Permit valid 10 in-game years / 20 real days).
     */
    suspend fun processCrossBorderImmigration(
        player: PlayerCharacter,
        targetCountryId: Long,
        targetStateId: Long,
        targetCountyId: Long,
        currentGameDay: Double,
        permitFeeCents: Long = 5_000L,
    ): Result<ImmigrationRecord> {
        return try {
            val uid = requireUid()
            check(player.ownerUid == uid)
            if (player.currentCountryId == targetCountryId) {
                return Result.failure(IllegalArgumentException("Use internal county relocation within the same country."))
            }

            val expiryDay = GameClockEngine.permitExpiryGameDay(currentGameDay)
            val exitCode = "EXIT-${player.currentCountryId}-${UUID.randomUUID().toString().take(6).uppercase()}"
            val entryCode = "PERMIT-$targetCountryId-${UUID.randomUUID().toString().take(6).uppercase()}"

            val personalAcctRef = db.collection("bank_accounts").document("acct_${uid}_personal")
            val destTreasuryRef = db.collection("bank_accounts").document("treasury_country_$targetCountryId")
            val immigRef = db.collection("immigration_records").document()
            val playerRef = db.collection("players").document(player.playerId.toString())
            val userRef = db.collection("users").document(uid)
            val ledgerRef = db.collection("ledger_transactions").document()

            val record = db.runTransaction { tx ->
                val personalSnap = tx.get(personalAcctRef)
                val destTreasurySnap = tx.get(destTreasuryRef)
                val personalBal = personalSnap.getLong("balanceCents") ?: 0L
                val actualFee = if (personalSnap.exists() && destTreasurySnap.exists() && personalBal >= permitFeeCents && permitFeeCents > 0L) {
                    val treasuryBal = destTreasurySnap.getLong("balanceCents") ?: 0L
                    tx.update(
                        personalAcctRef,
                        mapOf("balanceCents" to (personalBal - permitFeeCents), "updatedAt" to FieldValue.serverTimestamp()),
                    )
                    tx.update(
                        destTreasuryRef,
                        mapOf("balanceCents" to (treasuryBal + permitFeeCents), "updatedAt" to FieldValue.serverTimestamp()),
                    )
                    tx.set(
                        ledgerRef,
                        mapOf(
                            "txRef" to "IMM-${UUID.randomUUID().toString().take(8).uppercase()}",
                            "initiatedByUid" to uid,
                            "senderAccountId" to personalAcctRef.id,
                            "receiverAccountId" to destTreasuryRef.id,
                            "amountCents" to permitFeeCents,
                            "taxAmountCents" to 0L,
                            "taxTreasuryAccountId" to "",
                            "txType" to "IMMIGRATION_PERMIT_FEE",
                            "gameDay" to currentGameDay,
                            "reference" to "Entry Permit $entryCode ($targetCountryId)",
                            "status" to "SETTLED",
                            "publicAudit" to true,
                            "createdAt" to FieldValue.serverTimestamp(),
                        ),
                    )
                    permitFeeCents
                } else {
                    0L
                }

                tx.set(
                    immigRef,
                    mapOf(
                        "ownerUid" to uid,
                        "playerId" to player.playerId,
                        "fromCountryId" to player.currentCountryId,
                        "toCountryId" to targetCountryId,
                        "toStateId" to targetStateId,
                        "toCountyId" to targetCountyId,
                        "exitClearanceCode" to exitCode,
                        "entryPermitCode" to entryCode,
                        "issuedAtGameDay" to currentGameDay,
                        "expiresAtGameDay" to expiryDay,
                        "permitStatus" to "APPROVED",
                        "feePaidCents" to actualFee,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.update(
                    playerRef,
                    mapOf(
                        "currentCountryId" to targetCountryId,
                        "currentStateId" to targetStateId,
                        "currentCountyId" to targetCountyId,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.update(
                    userRef,
                    mapOf(
                        "activeCountryId" to targetCountryId,
                        "activeStateId" to targetStateId,
                        "activeCountyId" to targetCountyId,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )

                ImmigrationRecord(
                    docId = immigRef.id,
                    ownerUid = uid,
                    playerId = player.playerId,
                    fromCountryId = player.currentCountryId,
                    toCountryId = targetCountryId,
                    toStateId = targetStateId,
                    toCountyId = targetCountyId,
                    exitClearanceCode = exitCode,
                    entryPermitCode = entryCode,
                    issuedAtGameDay = currentGameDay,
                    expiresAtGameDay = expiryDay,
                    permitStatus = "APPROVED",
                    feePaidCents = actualFee,
                )
            }.await()

            pushNotification(
                uid = uid,
                category = "Immigration",
                title = "Entry Permit Approved ($entryCode)",
                detail = "Valid 10 in-game years (20 real days) until Game Day ${expiryDay.toLong()}",
                refId = immigRef.id,
            )
            Result.success(record)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "immigration_records")
            Result.failure(e)
        }
    }

    suspend fun renewImmigrationPermit(record: ImmigrationRecord, currentGameDay: Double): Result<Unit> {
        return try {
            val uid = requireUid()
            check(record.ownerUid == uid)
            val newExpiry = GameClockEngine.permitExpiryGameDay(currentGameDay)
            db.collection("immigration_records").document(record.docId).update(
                mapOf(
                    "expiresAtGameDay" to newExpiry,
                    "permitStatus" to "RENEWED",
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            pushNotification(
                uid = uid,
                category = "Immigration",
                title = "Permit Renewed (${record.entryPermitCode})",
                detail = "Extended 10 in-game years to Game Day ${newExpiry.toLong()}",
                refId = record.docId,
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "immigration_records/${record.docId}")
            Result.failure(e)
        }
    }

    suspend fun savePrivacySecuritySettings(
        settings: PrivacySecuritySettings,
        activePlayerId: Long = 0L,
    ): Result<Unit> {
        val uid = requireUid()
        val ref = db.collection("users").document(uid).collection("privacy_security").document("settings")
        return try {
            ref.set(
                mapOf(
                    "ownerUid" to uid,
                    "twoFactorEnabled" to settings.twoFactorEnabled,
                    "emailVerified" to settings.emailVerified,
                    "showNationality" to settings.showNationality,
                    "showResidence" to settings.showResidence,
                    "showOccupation" to settings.showOccupation,
                    "showPublicActivity" to settings.showPublicActivity,
                    "showNetWorth" to settings.showNetWorth,
                    "showPersonalBalance" to settings.showPersonalBalance,
                    "showCapitalBalance" to settings.showCapitalBalance,
                    "showEmployment" to settings.showEmployment,
                    "showCompanies" to settings.showCompanies,
                    "showPolitics" to settings.showPolitics,
                    "showPersonalIdentity" to settings.showPersonalIdentity,
                    "notifySalaries" to settings.notifySalaries,
                    "notifyTaxes" to settings.notifyTaxes,
                    "notifyElections" to settings.notifyElections,
                    "notifyCompanies" to settings.notifyCompanies,
                    "notifyMarketplace" to settings.notifyMarketplace,
                    "allowFriendRequests" to settings.allowFriendRequests,
                    "allowDirectMessages" to settings.allowDirectMessages,
                    "publicProfileSearchable" to settings.publicProfileSearchable,
                    "rateLimitWindowSec" to settings.rateLimitWindowSec,
                    "antiAbuseSignalMode" to settings.antiAbuseSignalMode,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            if (activePlayerId > 0L) {
                runCatching {
                    db.collection("players").document(activePlayerId.toString()).update(
                        mapOf(
                            "publicVisible" to settings.publicProfileSearchable,
                            "showNationality" to settings.showNationality,
                            "showResidence" to settings.showResidence,
                            "showOccupation" to settings.showOccupation,
                            "showActivity" to settings.showPublicActivity,
                            "showNetWorth" to settings.showNetWorth,
                            "showPersonalBalance" to settings.showPersonalBalance,
                            "showCapitalBalance" to settings.showCapitalBalance,
                            "showEmployment" to settings.showEmployment,
                            "showCompanies" to settings.showCompanies,
                            "showPolitics" to settings.showPolitics,
                            "showPersonalIdentity" to settings.showPersonalIdentity,
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun pushNotification(
        uid: String,
        category: String,
        title: String,
        detail: String,
        refId: String = "",
    ) {
        runCatching {
            db.collection("users").document(uid).collection("notifications").document().set(
                mapOf(
                    "ownerUid" to uid,
                    "category" to category.take(40),
                    "title" to title.take(120),
                    "detail" to detail.take(240),
                    "referenceId" to refId.take(80),
                    "readStatus" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
        }
    }
}
