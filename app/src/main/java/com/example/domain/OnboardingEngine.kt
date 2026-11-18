package com.example.domain

/**
 * Pure deterministic validation & privacy resolution for account registration,
 * 6-step character creation, and public/private citizen profile views.
 */
object OnboardingEngine {

    const val MIN_CITIZEN_AGE_YEARS: Int = 18
    const val MAX_CITIZEN_AGE_YEARS: Int = 95
    const val MIN_USERNAME_LENGTH: Int = 3
    const val MAX_USERNAME_LENGTH: Int = 24
    const val REGISTRATION_RATE_LIMIT_MS: Long = 3_000L

    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_]{3,24}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
    }

    fun normalizeUsername(raw: String): String = raw.trim().lowercase()

    fun validateUsername(raw: String): ValidationResult {
        val clean = raw.trim()
        if (clean.length < MIN_USERNAME_LENGTH) {
            return ValidationResult.Invalid("Username must be at least $MIN_USERNAME_LENGTH characters.")
        }
        if (clean.length > MAX_USERNAME_LENGTH) {
            return ValidationResult.Invalid("Username cannot exceed $MAX_USERNAME_LENGTH characters.")
        }
        if (!USERNAME_REGEX.matches(clean)) {
            return ValidationResult.Invalid("Username can only contain letters, numbers, and underscores.")
        }
        val reserved = setOf("admin", "system", "moderator", "support", "treasury", "deleted", "caretaker")
        if (clean.lowercase() in reserved) {
            return ValidationResult.Invalid("This username is reserved.")
        }
        return ValidationResult.Valid
    }

    fun validateEmail(raw: String): ValidationResult {
        val clean = raw.trim()
        if (clean.isEmpty()) {
            return ValidationResult.Invalid("Email address is required.")
        }
        if (!EMAIL_REGEX.matches(clean)) {
            return ValidationResult.Invalid("Enter a valid email address.")
        }
        return ValidationResult.Valid
    }

    fun validateCharacterIdentity(
        firstName: String,
        lastName: String,
        birthGameYear: Int,
        birthMonth: Int,
        birthDay: Int,
        currentGameYear: Int,
    ): ValidationResult {
        if (firstName.trim().length < 2) {
            return ValidationResult.Invalid("First name must be at least 2 characters.")
        }
        if (lastName.trim().length < 2) {
            return ValidationResult.Invalid("Last name must be at least 2 characters.")
        }
        if (birthMonth !in 1..12 || birthDay !in 1..30) {
            return ValidationResult.Invalid("Select a valid in-game birth month (1–12) and day (1–30).")
        }
        val age = computeCharacterAgeYears(birthGameYear, currentGameYear)
        if (age < MIN_CITIZEN_AGE_YEARS) {
            return ValidationResult.Invalid("Character must be at least $MIN_CITIZEN_AGE_YEARS in-game years old (currently calculated age: $age).")
        }
        if (age > MAX_CITIZEN_AGE_YEARS) {
            return ValidationResult.Invalid("Character age cannot exceed $MAX_CITIZEN_AGE_YEARS in-game years.")
        }
        return ValidationResult.Valid
    }

    /**
     * Supports pre-Era-1 historical birth years (e.g. -25 -> born 25 years before Era 1 Year 1)
     * as well as later Era years so new players can start at age 18+ right on Era 1 Year 1.
     */
    fun computeCharacterAgeYears(birthYear: Int, currentGameYear: Int): Int {
        return currentGameYear - birthYear
    }

    fun formatGameDateOfBirth(birthYear: Int, birthMonth: Int, birthDay: Int): String {
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val mLabel = monthNames.getOrElse(birthMonth - 1) { "M$birthMonth" }
        val yearLabel = if (birthYear <= 0) "B.E. ${kotlin.math.abs(birthYear) + 1}" else "Yr $birthYear"
        return "$yearLabel, $mLabel $birthDay"
    }

    fun buildFullName(firstName: String, middleName: String, lastName: String): String =
        listOf(firstName.trim(), middleName.trim(), lastName.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" ")
            .take(80)
}
