package com.example

import com.example.domain.FinancialEngines
import com.example.domain.OnboardingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingEngineTest {

    @Test
    fun username_validation_enforces_format_and_reserved_handles() {
        assertTrue(OnboardingEngine.validateUsername("amina_okonkwo") is OnboardingEngine.ValidationResult.Valid)
        assertTrue(OnboardingEngine.validateUsername("ab") is OnboardingEngine.ValidationResult.Invalid)
        assertTrue(OnboardingEngine.validateUsername("_leading") is OnboardingEngine.ValidationResult.Invalid)
        assertTrue(OnboardingEngine.validateUsername("trailing_") is OnboardingEngine.ValidationResult.Invalid)
        assertTrue(OnboardingEngine.validateUsername("double__score") is OnboardingEngine.ValidationResult.Invalid)
        assertTrue(OnboardingEngine.validateUsername("bad-handle!") is OnboardingEngine.ValidationResult.Invalid)
        assertTrue(OnboardingEngine.validateUsername("admin") is OnboardingEngine.ValidationResult.Invalid)
        assertTrue(OnboardingEngine.validateUsername("houseofafrica") is OnboardingEngine.ValidationResult.Invalid)
        assertTrue(OnboardingEngine.validateUsername("deleted") is OnboardingEngine.ValidationResult.Invalid)
    }

    @Test
    fun password_strength_enforces_length_complexity_and_anti_identifier_checks() {
        val short = OnboardingEngine.validatePasswordStrength("Ab1!", username = "amina", email = "amina@africa.org")
        assertEquals(OnboardingEngine.PasswordStrength.WEAK, short.strength)
        assertTrue(!short.isAcceptable)

        val common = OnboardingEngine.validatePasswordStrength("Password123", username = "amina", email = "amina@africa.org")
        assertTrue(!common.isAcceptable)

        val containsUser = OnboardingEngine.validatePasswordStrength("Amina#2026Strong!", username = "amina", email = "kofi@africa.org")
        assertTrue(!containsUser.isAcceptable)

        val good = OnboardingEngine.validatePasswordStrength("Kente#2026Go", username = "amina", email = "amina@africa.org")
        assertEquals(OnboardingEngine.PasswordStrength.GOOD, good.strength)
        assertTrue(good.isAcceptable)

        val strong = OnboardingEngine.validatePasswordStrength("Savanna!Gold#9421", username = "amina", email = "amina@africa.org")
        assertEquals(OnboardingEngine.PasswordStrength.STRONG, strong.strength)
        assertTrue(strong.isAcceptable)
        assertTrue(strong.issues.isEmpty())
    }

    @Test
    fun email_validation_checks_format() {
        assertTrue(OnboardingEngine.validateEmail("citizen@africa.org") is OnboardingEngine.ValidationResult.Valid)
        assertTrue(OnboardingEngine.validateEmail("") is OnboardingEngine.ValidationResult.Invalid)
        assertTrue(OnboardingEngine.validateEmail("not-an-email") is OnboardingEngine.ValidationResult.Invalid)
    }

    @Test
    fun character_identity_requires_minimum_18_in_game_years() {
        // Year 1 current -> birthYear -16 gives age 17 (rejected)
        val tooYoung = OnboardingEngine.validateCharacterIdentity(
            firstName = "Kofi",
            lastName = "Mensah",
            birthGameYear = -16,
            birthMonth = 4,
            birthDay = 10,
            currentGameYear = 1,
        )
        assertTrue(tooYoung is OnboardingEngine.ValidationResult.Invalid)

        // Year 1 current -> birthYear -17 gives age 18 (accepted)
        val adult = OnboardingEngine.validateCharacterIdentity(
            firstName = "Kofi",
            lastName = "Mensah",
            birthGameYear = -17,
            birthMonth = 4,
            birthDay = 10,
            currentGameYear = 1,
        )
        assertTrue(adult is OnboardingEngine.ValidationResult.Valid)
        assertEquals(18, OnboardingEngine.computeCharacterAgeYears(-17, 1))
    }

    @Test
    fun full_name_and_initial_grants_are_bounded() {
        assertEquals("Amina Zuri Okonkwo", OnboardingEngine.buildFullName(" Amina ", "Zuri", " Okonkwo "))
        assertEquals("Amina Okonkwo", OnboardingEngine.buildFullName("Amina", "", "Okonkwo"))
        assertEquals(25_000L, FinancialEngines.INITIAL_PERSONAL_BALANCE_CENTS)
        assertEquals(100_000L, FinancialEngines.INITIAL_CAPITAL_BALANCE_CENTS)
    }
}
