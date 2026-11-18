package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.ui.navigation.HoaDestination
import com.example.ui.navigation.HoaEntityRoutes
import com.example.ui.state.BankAccountCategory
import com.example.ui.state.CampaignSystemTab
import com.example.ui.state.HoaShellViewModel
import com.example.ui.state.ProfileSectionTab
import com.example.ui.theme.ThemePreference
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun read_app_name_from_context() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("House of Africa", appName)
    }

    @Test
    fun verify_eleven_main_destinations_and_clean_routes() {
        assertEquals(11, HoaDestination.entries.size)
        assertEquals(11, HoaDestination.entries.map { it.route }.distinct().size)
        assertEquals("/world/101", HoaEntityRoutes.world(101))
        assertEquals("/state/201", HoaEntityRoutes.state(201))
        assertEquals("/county/301", HoaEntityRoutes.county(301))
        assertEquals("/player/12453", HoaEntityRoutes.player(12453))
        assertEquals("/company/501", HoaEntityRoutes.company(501))
        assertEquals("/party/601", HoaEntityRoutes.party(601))
        assertEquals("/election/801", HoaEntityRoutes.election(801))
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
        assertEquals(16, ProfileSectionTab.entries.size)
    }

    @Test
    fun home_dashboard_displays_and_navigates_across_core_modules() {
        composeRule.onNodeWithTag("hero_banner_card").assertIsDisplayed()
        composeRule.onNodeWithTag("server_clock_banner").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_nav_world").performClick()
        composeRule.onNodeWithText("Africa / World Geography").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_nav_politics").performClick()
        composeRule.onNodeWithTag("campaign_tab_national").performClick()
        composeRule.onNodeWithText("National Campaign").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_nav_banking").performClick()
        composeRule.onNodeWithTag("bank_tab_campaign").performClick()
        composeRule.onNodeWithText("Campaign Funds").assertIsDisplayed()

        composeRule.onNodeWithTag("topbar_profile_button").performClick()
        composeRule.onNodeWithText("Citizen Profile").assertIsDisplayed()
    }
}
