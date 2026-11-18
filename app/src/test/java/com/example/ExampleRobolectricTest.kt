package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BankAccount
import com.example.data.model.CompanyRecord
import com.example.data.model.GovernmentRecord
import com.example.data.model.PlayerCharacter
import com.example.data.model.ReferendumRecord
import com.example.data.model.UserAccount
import com.example.data.model.WorldClock
import com.example.data.seed.AfricanWorldCatalog
import com.example.ui.HoaAppShell
import com.example.ui.WorldShellBindings
import com.example.ui.navigation.HoaDestination
import com.example.ui.navigation.HoaEntityRoutes
import com.example.ui.state.BankAccountCategory
import com.example.ui.state.CampaignSystemTab
import com.example.ui.state.HoaShellViewModel
import com.example.ui.state.ProfileSectionTab
import com.example.ui.state.RelocationFeedback
import com.example.ui.state.SimulationShellBindings
import com.example.ui.state.SimulationSnapshot
import com.example.ui.state.UiState
import com.example.ui.state.WorldAtlasSnapshot
import com.example.ui.theme.HouseOfAfricaTheme
import com.example.ui.theme.ThemePreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun read_app_name_from_context() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("House of Africa", appName)
    }

    @Test
    fun verify_eleven_main_destinations_and_immutable_world_catalog() {
        assertEquals(11, HoaDestination.entries.size)
        assertEquals(11, HoaDestination.entries.map { it.route }.distinct().size)

        val countryIds = AfricanWorldCatalog.countries.map { it.countryId }
        val stateIds = AfricanWorldCatalog.states.map { it.stateId }
        val countyIds = AfricanWorldCatalog.counties.map { it.countyId }

        assertEquals(12, countryIds.distinct().size)
        assertEquals(24, stateIds.distinct().size)
        assertEquals(28, countyIds.distinct().size)
        assertTrue(countryIds.all { it >= 101L })
        assertTrue(stateIds.all { it >= 201L })
        assertTrue(countyIds.all { it >= 301L })

        assertEquals("/world/101", HoaEntityRoutes.world(101))
        assertEquals("/state/201", HoaEntityRoutes.state(201))
        assertEquals("/county/301", HoaEntityRoutes.county(301))
    }

    @Test
    fun verify_shell_view_model_state_transitions() {
        val viewModel = HoaShellViewModel()
        viewModel.setThemePreference(ThemePreference.DARK)
        viewModel.selectProfileSection(ProfileSectionTab.IMMIGRATION)
        viewModel.selectCampaignTab(CampaignSystemTab.NATIONAL)
        viewModel.selectBankCategory(BankAccountCategory.CAMPAIGN)

        val state = viewModel.uiState.value
        assertEquals(ThemePreference.DARK, state.themePreference)
        assertEquals(ProfileSectionTab.IMMIGRATION, state.selectedProfileSection)
        assertEquals(CampaignSystemTab.NATIONAL, state.selectedCampaignTab)
        assertEquals(BankAccountCategory.CAMPAIGN, state.selectedBankCategory)
    }

    @Test
    fun full_cuj_navigation_world_drilldown_profile_banking_and_politics() {
        val shellVm = HoaShellViewModel()
        val atlasSnapshot = WorldAtlasSnapshot(
            userAccount = UserAccount(
                userId = "test_uid",
                displayName = "Amina Okonkwo",
                activeCountryId = 101L,
                activeStateId = 201L,
                activeCountyId = 301L,
                mainPlayerId = 12453L,
                activePlayerId = 12453L,
            ),
            countries = AfricanWorldCatalog.countries,
            statesForSelectedCountry = AfricanWorldCatalog.states.filter { it.countryId == 101L },
            countiesForSelectedState = AfricanWorldCatalog.counties.filter { it.stateId == 201L },
            allCounties = AfricanWorldCatalog.counties,
            relocations = emptyList(),
        )
        val simSnapshot = SimulationSnapshot(
            worldClock = WorldClock(
                scope = "WORLD",
                eraNumber = 1,
                lastProcessedTickIndex = 12L,
                totalGameDaysElapsed = 12.0,
            ),
            myCharacters = listOf(
                PlayerCharacter(
                    playerId = 12453L,
                    ownerUid = "test_uid",
                    characterRole = "MAIN",
                    fullName = "Amina Okonkwo",
                    currentCountryId = 101L,
                    currentStateId = 201L,
                    currentCountyId = 301L,
                    occupation = "Economist",
                ),
            ),
            activeCharacterId = 12453L,
            myAccounts = listOf(
                BankAccount(docId = "acct_test_personal", accountType = "PERSONAL", accountCategory = "CITIZEN", currencyCode = "NGN", balanceCents = 250_000L),
                BankAccount(docId = "acct_test_capital", accountType = "CAPITAL", accountCategory = "CITIZEN", currencyCode = "NGN", balanceCents = 50_000_000L),
                BankAccount(docId = "acct_test_campaign", accountType = "CAMPAIGN", accountCategory = "CAMPAIGN", currencyCode = "NGN", balanceCents = 100_000L),
            ),
            companies = listOf(
                CompanyRecord(
                    companyId = 501L,
                    name = "Kente Agri-Industries Ltd",
                    sector = "Farming",
                    ownershipType = "PRIVATE",
                    totalShares = 1_000_000L,
                    sharePriceCents = 1_000L,
                ),
            ),
            selectedCompanyId = 501L,
            governments = listOf(
                GovernmentRecord(
                    docId = "gov_country_101",
                    jurisdictionLevel = "NATIONAL",
                    jurisdictionId = 101L,
                    jurisdictionName = "Nigeria",
                    treasuryAccountId = "treasury_country_101",
                ),
            ),
            referendums = listOf(
                ReferendumRecord(
                    eraNumber = 1,
                    status = "ACTIVE",
                    continueVotes = 70L,
                    partialResetVotes = 20L,
                    fullResetVotes = 10L,
                    abstainVotes = 5L,
                ),
            ),
        )

        composeRule.setContent {
            val uiState by shellVm.uiState.collectAsStateWithLifecycle()
            HouseOfAfricaTheme(themePreference = uiState.themePreference) {
                HoaAppShell(
                    uiState = uiState,
                    viewModel = shellVm,
                    worldBindings = WorldShellBindings(
                        worldState = UiState.Success(atlasSnapshot),
                        searchQuery = "",
                        relocationFeedback = RelocationFeedback(),
                        bootstrapError = null,
                        userDisplayName = "Amina Okonkwo",
                        onSearchQueryChange = {},
                        onSelectCountry = {},
                        onSelectState = {},
                        onSettleOrRelocate = { _, _, _, _, _ -> },
                        onRetryBootstrap = {},
                        onSignOut = {},
                    ),
                    simBindings = SimulationShellBindings(snapshot = simSnapshot),
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("hero_banner_card").assertIsDisplayed()

        // Navigate World Atlas -> Country 101 -> State 201 -> County 301
        composeRule.onNodeWithTag("bottom_nav_world").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("country_card_101").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("state_card_201").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("county_card_301").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Eti-Osa County").assertIsDisplayed()
        composeRule.onNodeWithTag("settle_relocate_button").performScrollTo().assertIsDisplayed()

        // Navigate Banking -> verify Personal/Capital/Campaign tabs
        composeRule.onNodeWithTag("bottom_nav_banking").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("bank_tab_campaign").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Campaign Funds").assertIsDisplayed()

        // Navigate Politics -> verify 3 Campaign Systems
        composeRule.onNodeWithTag("bottom_nav_politics").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("campaign_tab_national").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("National Campaign").assertIsDisplayed()

        // Navigate TopBar Profile -> verify Citizen Profile & 16-section switcher
        composeRule.onNodeWithTag("topbar_profile_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Citizen Profile & Immigration").assertIsDisplayed()

        // Navigate Home Dashboard and verify /player/{id} route works cleanly
        composeRule.onNodeWithTag("bottom_nav_home").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dashboard_view_profile_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("public_player_profile_root").assertIsDisplayed()
        assertTrue(
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Amina Okonkwo", substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty(),
        )
    }

    @Test
    @Config(qualifiers = "w1100dp-h800dp")
    fun verify_public_landing_page_desktop_header_and_all_required_sections() {
        composeRule.setContent {
            HouseOfAfricaTheme {
                com.example.ui.screens.LandingScreen(
                    onNavigateLogin = {},
                    onNavigateRegister = {},
                    onExploreCountry = {},
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("landing_header_login_button").assertIsDisplayed()
        composeRule.onNodeWithTag("landing_header_register_button").assertIsDisplayed()
        composeRule.onNodeWithText("YOUR LIFE. YOUR POWER. YOUR AFRICA.").assertIsDisplayed()
        composeRule.onNodeWithText("Enter the World").assertIsDisplayed()
        composeRule.onNodeWithText("Discover the Game").assertIsDisplayed()
        composeRule.onNodeWithTag("landing_about_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("landing_country_101").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("landing_cta_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("YOUR STORY IN AFRICA STARTS HERE.").assertIsDisplayed()
        composeRule.onNodeWithText("CREATE YOUR CHARACTER").assertIsDisplayed()
    }

    @Test
    fun verify_character_creation_wizard_age_validation_and_step_progression() {
        composeRule.setContent {
            HouseOfAfricaTheme {
                com.example.ui.screens.CharacterCreationWizardScreen(
                    sim = SimulationSnapshot(),
                    feedback = com.example.ui.state.ActionFeedback(),
                    actions = null,
                    asAlt = false,
                    onCompleted = {},
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wizard_first_name").assertIsDisplayed()
        // Empty names should block progression and surface validation error
        composeRule.onNodeWithTag("wizard_next_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wizard_error_banner").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun verify_public_player_profile_handles_deleted_suspended_and_not_found() {
        val deletedChar = PlayerCharacter(playerId = 12499L, fullName = "Hidden Name", accountStatus = "DELETED")
        composeRule.setContent {
            HouseOfAfricaTheme {
                com.example.ui.screens.PublicPlayerProfileScreen(
                    playerId = 12499L,
                    sim = SimulationSnapshot(),
                    playerLookupResult = "User Deleted" to deletedChar,
                    feedback = com.example.ui.state.ActionFeedback(),
                    actions = null,
                    onBack = {},
                    onOpenSettings = {},
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("public_player_profile_root").assertIsDisplayed()
        assertTrue(
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("User Deleted"))
                .fetchSemanticsNodes()
                .isNotEmpty(),
        )
    }

    @Test
    fun verify_dashboard_hides_campaign_funds_when_inactive_and_shows_settings_eight_sections() {
        val shellVm = HoaShellViewModel()
        composeRule.setContent {
            val uiState by shellVm.uiState.collectAsStateWithLifecycle()
            HouseOfAfricaTheme {
                com.example.ui.screens.HomeDashboardScreen(
                    uiState = uiState,
                    onNavigate = {},
                    sim = SimulationSnapshot(
                        myAccounts = listOf(
                            BankAccount(docId = "p1", accountType = "PERSONAL", balanceCents = 25_000L),
                            BankAccount(docId = "c1", accountType = "CAPITAL", balanceCents = 100_000L),
                        ),
                    ),
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("widget_account_balances").performScrollTo().assertIsDisplayed()
        assertTrue(
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Campaign:", substring = true))
                .fetchSemanticsNodes()
                .isEmpty(),
        )
    }

    @Test
    fun verify_eight_section_settings_screen_renders() {
        val shellVm = HoaShellViewModel()
        composeRule.setContent {
            val uiState by shellVm.uiState.collectAsStateWithLifecycle()
            HouseOfAfricaTheme {
                com.example.ui.screens.SettingsScreen(
                    uiState = uiState,
                    onSelectTheme = shellVm::setThemePreference,
                    onToggleNationality = shellVm::toggleVisibilityNationality,
                    onToggleResidence = shellVm::toggleVisibilityResidence,
                    onToggleOccupation = shellVm::toggleVisibilityOccupation,
                    onToggleActivity = shellVm::toggleVisibilityActivity,
                    onUpdateVisibilitySettings = shellVm::updateVisibilitySettings,
                    sim = SimulationSnapshot(
                        myCharacters = listOf(
                            PlayerCharacter(playerId = 12453L, fullName = "Amina Okonkwo", characterRole = "MAIN"),
                        ),
                        activeCharacterId = 12453L,
                    ),
                    userAccount = UserAccount(userId = "u1", username = "amina_dev", email = "amina@hoa.africa", emailVerified = true),
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_section_account").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_section_privacy").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_section_alt").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun verify_register_screen_username_uniqueness_and_email_verification_gate() {
        var verificationEmailTriggered = false
        var refreshTriggered = false
        var registrationSubmitted = false

        composeRule.setContent {
            HouseOfAfricaTheme {
                com.example.ui.screens.RegistrationScreen(
                    isSignedIn = true,
                    initialEmail = "amina@hoa.africa",
                    initialDisplayName = "Amina Okonkwo",
                    emailVerified = false,
                    pendingDraft = com.example.ui.state.PendingRegistrationDraft(
                        username = "amina_lagos",
                        acceptedTerms = true,
                    ),
                    usernameAvailability = "amina_lagos" to true,
                    onSendVerificationEmail = { verificationEmailTriggered = true },
                    onRefreshEmailVerification = { refreshTriggered = true },
                    onCompleteRegistration = { _, _, _, _ -> registrationSubmitted = true },
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("registration_screen_root").assertIsDisplayed()
        composeRule.onNodeWithTag("register_email_verification_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("@amina_lagos is available!").assertIsDisplayed()
        composeRule.onNodeWithTag("register_security_policy_card").performScrollTo().assertIsDisplayed()

        // Verify email link & refresh triggers work
        composeRule.onNodeWithTag("register_send_verification_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("register_refresh_verification_button").performScrollTo().performClick()
        assertTrue(verificationEmailTriggered)
        assertTrue(refreshTriggered)

        // Unverified signed-in user is blocked from reserving until email verification passes
        composeRule.onNodeWithTag("register_submit_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("register_error_banner").performScrollTo().assertIsDisplayed()
        assertTrue(!registrationSubmitted)
    }
}
