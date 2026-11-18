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
        assertTrue(OnboardingEngine.validateUsername("bad-handle!") is OnboardingEngine.ValidationResult.Invalid)
        assertTrue(OnboardingEngine.validateUsername("admin") is OnboardingEngine.ValidationResult.Invalid)
        assertTrue(OnboardingEngine.validateUsername("deleted") is OnboardingEngine.ValidationResult.Invalid)
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
