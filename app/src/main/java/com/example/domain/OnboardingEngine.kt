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
    const val MIN_PASSWORD_LENGTH: Int = 10
    const val STRONG_PASSWORD_LENGTH: Int = 14
    const val REGISTRATION_RATE_LIMIT_MS: Long = 3_000L

    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_]{3,24}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val RESERVED_USERNAMES = setOf(
        "admin",
        "administrator",
        "caretaker",
        "deleted",
        "houseofafrica",
        "moderator",
        "null",
        "official",
        "root",
        "security",
        "staff",
        "support",
        "system",
        "treasury",
        "undefined",
    )
    private val COMMON_PASSWORDS = setOf(
        "password",
        "password123",
        "12345678",
        "1234567890",
        "qwerty123",
        "letmein123",
        "welcome123",
        "admin123",
        "houseofafrica",
        "africa2025",
        "africa2026",
    )

    enum class PasswordStrength(val label: String) {
        WEAK("Weak"),
        FAIR("Fair"),
        GOOD("Good"),
        STRONG("Strong"),
    }

    data class PasswordEvaluation(
        val score: Int,
        val strength: PasswordStrength,
        val issues: List<String>,
        val isAcceptable: Boolean,
    )

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
        if (clean.startsWith("_") || clean.endsWith("_")) {
            return ValidationResult.Invalid("Username cannot start or end with an underscore.")
        }
        if ("__" in clean) {
            return ValidationResult.Invalid("Username cannot contain consecutive underscores.")
        }
        if (clean.lowercase() in RESERVED_USERNAMES) {
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

    /**
     * Pure password policy evaluator enforcing minimum length, mixed character classes,
     * whitespace safety, common-password rejection, and anti-identifier checks.
     * Actual credential hashing uses Firebase Authentication server-side scrypt when enabled.
     */
    fun validatePasswordStrength(
        password: String,
        username: String = "",
        email: String = "",
    ): PasswordEvaluation {
        val issues = mutableListOf<String>()
        if (password.length < MIN_PASSWORD_LENGTH) {
            issues += "Use at least $MIN_PASSWORD_LENGTH characters."
        }
        if (password.any { it.isWhitespace() }) {
            issues += "Password cannot contain whitespace."
        }
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() && !it.isWhitespace() }

        if (!hasUpper) issues += "Include at least one uppercase letter."
        if (!hasLower) issues += "Include at least one lowercase letter."
        if (!hasDigit) issues += "Include at least one number."
        if (!hasSpecial) issues += "Include at least one symbol (e.g. !@#\$%)."

        val lowered = password.lowercase()
        if (lowered in COMMON_PASSWORDS) {
            issues += "Choose a less predictable password."
        }
        val cleanUser = normalizeUsername(username)
        if (cleanUser.length >= 3 && cleanUser in lowered) {
            issues += "Password cannot contain your username."
        }
        val emailLocal = email.trim().substringBefore("@").lowercase()
        if (emailLocal.length >= 3 && emailLocal in lowered) {
            issues += "Password cannot contain your email name."
        }

        val classCount = listOf(hasUpper, hasLower, hasDigit, hasSpecial).count { it }
        val lengthBonus = when {
            password.length >= STRONG_PASSWORD_LENGTH -> 2
            password.length >= MIN_PASSWORD_LENGTH -> 1
            else -> 0
        }
        val rawScore = (classCount + lengthBonus - (if (issues.isEmpty()) 0 else 1)).coerceIn(0, 4)
        val strength = when {
            issues.isEmpty() && password.length >= STRONG_PASSWORD_LENGTH -> PasswordStrength.STRONG
            issues.isEmpty() -> PasswordStrength.GOOD
            rawScore >= 2 && password.length >= 8 -> PasswordStrength.FAIR
            else -> PasswordStrength.WEAK
        }
        return PasswordEvaluation(
            score = rawScore,
            strength = strength,
            issues = issues,
            isAcceptable = issues.isEmpty(),
        )
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
