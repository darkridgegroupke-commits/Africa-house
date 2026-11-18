package com.example.ui.state

import androidx.lifecycle.ViewModel
import com.example.ui.theme.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class PhaseProgress {
    COMPLETED,
    ACTIVE,
    PREPARED_NEXT,
    PLANNED,
}

data class RoadmapPhase(
    val number: Int,
    val title: String,
    val summary: String,
    val status: PhaseProgress,
)

enum class CampaignSystemTab(val title: String, val scopeLabel: String) {
    STATE("State Campaign", "Governor & Regional Offices"),
    NATIONAL("National Campaign", "Parliament & Presidential Offices"),
    GENERAL("General Campaign", "General Election Cycles"),
}

enum class BankAccountCategory(val title: String, val code: String, val ruleSummary: String) {
    PERSONAL(
        title = "Personal Funds",
        code = "ACCT-PER",
        ruleSummary = "Salaries, approved dividends, and everyday personal expenses.",
    ),
    CAPITAL(
        title = "Capital Funds",
        code = "ACCT-CAP",
        ruleSummary = "Investments, business formation, and approved capital contributions.",
    ),
    CAMPAIGN(
        title = "Campaign Funds",
        code = "ACCT-CMP",
        ruleSummary = "Ring-fenced political campaign funds. Cannot convert to personal cash.",
    ),
}

enum class ProfileSectionTab(val index: Int, val label: String) {
    OVERVIEW(1, "Overview"),
    IDENTITY(2, "Identity"),
    IMMIGRATION(3, "Immigration"),
    BANK_ACCOUNTS(4, "Bank Accounts"),
    INCOME(5, "Income"),
    EMPLOYMENT(6, "Employment"),
    COMPANIES(7, "Companies"),
    INVESTMENTS(8, "Investments"),
    PROPERTIES(9, "Properties"),
    CERTIFICATES(10, "Certificates"),
    POLITICS(11, "Politics"),
    CAMPAIGNS(12, "Campaigns"),
    ELECTIONS(13, "Elections"),
    ACHIEVEMENTS(14, "Achievements"),
    HISTORY(15, "History"),
    SETTINGS(16, "Settings"),
}

data class PublicVisibilitySettings(
    val showNationality: Boolean = true,
    val showCurrentResidence: Boolean = true,
    val showOccupation: Boolean = true,
    val showPublicActivity: Boolean = false,
    val showNetWorth: Boolean = false,
    val showPersonalBalance: Boolean = false,
    val showCapitalBalance: Boolean = false,
    val showEmployment: Boolean = true,
    val showCompanies: Boolean = true,
    val showPolitics: Boolean = true,
    val showPersonalIdentity: Boolean = false,
    val notifySalaries: Boolean = true,
    val notifyTaxes: Boolean = true,
    val notifyElections: Boolean = true,
    val notifyCompanies: Boolean = true,
    val notifyMarketplace: Boolean = true,
    val allowFriendRequests: Boolean = true,
    val allowDirectMessages: Boolean = true,
    val publicProfileSearchable: Boolean = true,
)

data class HoaShellUiState(
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val selectedProfileSection: ProfileSectionTab = ProfileSectionTab.OVERVIEW,
    val selectedCampaignTab: CampaignSystemTab = CampaignSystemTab.STATE,
    val selectedBankCategory: BankAccountCategory = BankAccountCategory.PERSONAL,
    val selectedNotificationFilter: String = "All",
    val visibilitySettings: PublicVisibilitySettings = PublicVisibilitySettings(),
    val roadmapPhases: List<RoadmapPhase> = defaultRoadmapPhases(),
)

class HoaShellViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HoaShellUiState())
    val uiState: StateFlow<HoaShellUiState> = _uiState.asStateFlow()

    fun setThemePreference(preference: ThemePreference) {
        _uiState.update { it.copy(themePreference = preference) }
    }

    fun selectProfileSection(section: ProfileSectionTab) {
        _uiState.update { it.copy(selectedProfileSection = section) }
    }

    fun selectCampaignTab(tab: CampaignSystemTab) {
        _uiState.update { it.copy(selectedCampaignTab = tab) }
    }

    fun selectBankCategory(category: BankAccountCategory) {
        _uiState.update { it.copy(selectedBankCategory = category) }
    }

    fun selectNotificationFilter(filter: String) {
        _uiState.update { it.copy(selectedNotificationFilter = filter) }
    }

    fun toggleVisibilityNationality(enabled: Boolean) {
        _uiState.update {
            it.copy(visibilitySettings = it.visibilitySettings.copy(showNationality = enabled))
        }
    }

    fun toggleVisibilityResidence(enabled: Boolean) {
        _uiState.update {
            it.copy(visibilitySettings = it.visibilitySettings.copy(showCurrentResidence = enabled))
        }
    }

    fun toggleVisibilityOccupation(enabled: Boolean) {
        _uiState.update {
            it.copy(visibilitySettings = it.visibilitySettings.copy(showOccupation = enabled))
        }
    }

    fun toggleVisibilityActivity(enabled: Boolean) {
        _uiState.update {
            it.copy(visibilitySettings = it.visibilitySettings.copy(showPublicActivity = enabled))
        }
    }

    fun updateVisibilitySettings(transform: (PublicVisibilitySettings) -> PublicVisibilitySettings) {
        _uiState.update { it.copy(visibilitySettings = transform(it.visibilitySettings)) }
    }
}

private fun defaultRoadmapPhases(): List<RoadmapPhase> = listOf(
    RoadmapPhase(1, "Update 1 • Complete World Geography", "12 African countries, 24 states, 28 counties & clean /world/{id} routes", PhaseProgress.ACTIVE),
    RoadmapPhase(2, "Update 2 • Server Game Clock", "48h = 1 in-game year, idempotent catch-up recovery & tick logs", PhaseProgress.ACTIVE),
    RoadmapPhase(3, "Update 3 • Identity & Immigration", "Main + ALT characters, 16-tab profile, 'User Deleted' & 10-yr permits", PhaseProgress.ACTIVE),
    RoadmapPhase(4, "Update 4 • Central Banking Engine", "Double-entry Personal, Capital & Campaign accounts with audit logs", PhaseProgress.ACTIVE),
    RoadmapPhase(5, "Update 5 • Employment & Tax Routing", "Hourly salaries, unpaid wage liabilities & National/State/County taxes", PhaseProgress.ACTIVE),
    RoadmapPhase(6, "Update 6 • Companies & Equity", "1M shares @ 10 units, 2-for-1 splits, CEO >=5% rule & IPO issuance", PhaseProgress.ACTIVE),
    RoadmapPhase(7, "Update 7 • Facilities & Production", "Sector buildings, NPC capacity caps & inventory-first output", PhaseProgress.ACTIVE),
    RoadmapPhase(8, "Update 8 • TSN County Marketplace", "Supply/demand pricing, escrow orders, anti-wash guards & inventories", PhaseProgress.ACTIVE),
    RoadmapPhase(9, "Update 9 • 14-Dept Government", "National, State & County budgets, tax policies & audited spend", PhaseProgress.ACTIVE),
    RoadmapPhase(10, "Update 10 • Parties, Elections & 3 Campaigns", "State, National & General campaign budgets + single-ballot voting", PhaseProgress.ACTIVE),
    RoadmapPhase(11, "Update 11 • Property, Loans & Bonds", "Land/real estate, liability-tracked loans & sovereign/corp bonds", PhaseProgress.ACTIVE),
    RoadmapPhase(12, "Update 12 • Security & 200-Day Referendum", "2FA/rate-limit signals, audit logs & 60% Era Referendum retention", PhaseProgress.ACTIVE),
)
