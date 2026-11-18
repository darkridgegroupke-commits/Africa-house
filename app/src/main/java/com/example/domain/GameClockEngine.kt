package com.example.domain

import kotlin.math.floor

/**
 * Authoritative server-controlled game clock math for House of Africa.
 *
 * Canonical conversion rules:
 * - 48 real-life hours  = 1 in-game year (12 months * 30 days = 360 in-game days)
 * - 24 real-life hours  = 6 in-game months (180 in-game days)
 * - 12 real-life hours  = 3 in-game months (90 in-game days)
 * - 1 real-life hour    = 7.5 in-game days (1 in-game day = 8 real minutes = 480,000 ms)
 * - 20 real-life days   = 10 in-game years (3,600 in-game days — immigration permit renewal)
 * - 200 real-life days  = 100 in-game years (36,000 in-game days — Era Referendum milestone)
 */
object GameClockEngine {

    const val REAL_HOURS_PER_GAME_YEAR: Double = 48.0
    const val REAL_HOURS_PER_SIX_MONTHS: Double = 24.0
    const val REAL_HOURS_PER_THREE_MONTHS: Double = 12.0
    const val GAME_DAYS_PER_REAL_HOUR: Double = 7.5
    const val GAME_DAYS_PER_MONTH: Int = 30
    const val GAME_MONTHS_PER_YEAR: Int = 12
    const val GAME_DAYS_PER_YEAR: Int = GAME_DAYS_PER_MONTH * GAME_MONTHS_PER_YEAR // 360

    const val REAL_MILLIS_PER_GAME_DAY: Long = 480_000L // 8 real minutes = 1 in-game day
    const val TEN_YEAR_PERMIT_GAME_DAYS: Long = 10L * GAME_DAYS_PER_YEAR // 3,600 game days = 20 real days
    const val ERA_REFERENDUM_GAME_DAYS: Long = 100L * GAME_DAYS_PER_YEAR // 36,000 game days = 200 real days
    const val MAX_CATCHUP_TICKS_PER_BATCH: Int = 360 // Up to 1 in-game year per batch to keep transactions responsive

    data class GameCalendarDate(
        val eraNumber: Int,
        val totalGameDays: Double,
        val year: Int,
        val month: Int,
        val day: Int,
        val hourOfDay: Int,
    ) {
        val monthName: String
            get() = MONTH_NAMES.getOrElse(month - 1) { "M$month" }

        fun formattedShort(): String = "Era $eraNumber • Yr $year, $monthName $day"

        fun formattedFull(): String =
            "Era $eraNumber • Year $year, $monthName $day (${hourOfDay.toString().padStart(2, '0')}:00)"

        fun isReferendumMilestoneReached(): Boolean = totalGameDays >= ERA_REFERENDUM_GAME_DAYS
    }

    data class ClockCatchUpWindow(
        val previousTickIndex: Long,
        val targetTickIndex: Long,
        val ticksToProcess: Int,
        val totalGameDaysAtTarget: Double,
    ) {
        val hasPendingTicks: Boolean get() = ticksToProcess > 0
    }

    private val MONTH_NAMES = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )

    fun elapsedGameDaysFromRealMillis(elapsedMillis: Long): Double {
        if (elapsedMillis <= 0L) return 0.0
        return elapsedMillis.toDouble() / REAL_MILLIS_PER_GAME_DAY.toDouble()
    }

    fun realHoursToGameDays(realHours: Double): Double =
        if (realHours <= 0.0) 0.0 else realHours * GAME_DAYS_PER_REAL_HOUR

    fun gameYearsToRealDays(gameYears: Double): Double =
        if (gameYears <= 0.0) 0.0 else gameYears * (REAL_HOURS_PER_GAME_YEAR / 24.0)

    fun toGameCalendarDate(eraNumber: Int, totalGameDays: Double): GameCalendarDate {
        val safeDays = totalGameDays.coerceAtLeast(0.0)
        val wholeDays = floor(safeDays).toLong()
        val fractionalDay = safeDays - wholeDays
        val hourOfDay = floor(fractionalDay * 24.0).toInt().coerceIn(0, 23)

        val year = (wholeDays / GAME_DAYS_PER_YEAR).toInt() + 1
        val dayOfYear = (wholeDays % GAME_DAYS_PER_YEAR).toInt()
        val month = (dayOfYear / GAME_DAYS_PER_MONTH) + 1
        val day = (dayOfYear % GAME_DAYS_PER_MONTH) + 1

        return GameCalendarDate(
            eraNumber = eraNumber.coerceAtLeast(1),
            totalGameDays = safeDays,
            year = year,
            month = month,
            day = day,
            hourOfDay = hourOfDay,
        )
    }

    /**
     * Computes deterministic catch-up ticks between [lastProcessedTickIndex] and the authoritative
     * server timestamp [serverNowMillis] relative to [eraStartMillis]. Never uses device wall clock
     * as truth—both timestamps originate from Firestore @ServerTimestamp documents.
     */
    fun computeCatchUpWindow(
        lastProcessedTickIndex: Long,
        eraStartMillis: Long,
        serverNowMillis: Long,
        maxTicksPerBatch: Int = MAX_CATCHUP_TICKS_PER_BATCH,
    ): ClockCatchUpWindow {
        val elapsedMs = (serverNowMillis - eraStartMillis).coerceAtLeast(0L)
        val currentTickIndex = elapsedMs / REAL_MILLIS_PER_GAME_DAY
        val rawPending = (currentTickIndex - lastProcessedTickIndex).coerceAtLeast(0L)
        val ticksToProcess = rawPending.coerceAtMost(maxTicksPerBatch.toLong()).toInt()
        val targetTickIndex = lastProcessedTickIndex + ticksToProcess
        val totalGameDays = elapsedGameDaysFromRealMillis(elapsedMs).coerceAtLeast(targetTickIndex.toDouble())
        return ClockCatchUpWindow(
            previousTickIndex = lastProcessedTickIndex,
            targetTickIndex = targetTickIndex,
            ticksToProcess = ticksToProcess,
            totalGameDaysAtTarget = totalGameDays,
        )
    }

    /**
     * Converts game days into immigration permit expiration day.
     */
    fun permitExpiryGameDay(issuedAtGameDay: Double): Double =
        issuedAtGameDay.coerceAtLeast(0.0) + TEN_YEAR_PERMIT_GAME_DAYS.toDouble()
}
