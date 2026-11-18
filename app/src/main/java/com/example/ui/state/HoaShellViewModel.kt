package com.example.ui.state

import androidx.lifecycle.ViewModel
import com.example.ui.theme.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class PhaseProgress {
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
}

private fun defaultRoadmapPhases(): List<RoadmapPhase> = listOf(
    RoadmapPhase(1, "Phase 1 • UI Shell & Navigation", "Dark green, gold & off-white design system, adaptive 11-module navigation", PhaseProgress.ACTIVE),
    RoadmapPhase(2, "Phase 2 • World & Geography", "African countries, states, counties & non-recycled /world/{id} hierarchy", PhaseProgress.PREPARED_NEXT),
    RoadmapPhase(3, "Phase 3 • Server Game Clock", "48h = 1 in-game year, idempotent catch-up recovery & scheduling", PhaseProgress.PLANNED),
    RoadmapPhase(4, "Phase 4 • Identity & Immigration", "Main + ALT characters, 16-tab profile, 10-year permits & clearances", PhaseProgress.PLANNED),
    RoadmapPhase(5, "Phase 5 • Central Ledger & Taxes", "Double-entry banking, Personal/Capital/Campaign funds & salary liabilities", PhaseProgress.PLANNED),
    RoadmapPhase(6, "Phase 6 • Companies & Shares", "1M shares @ 10 units, 2-for-1 splits, CEO rules & certificates", PhaseProgress.PLANNED),
    RoadmapPhase(7, "Phase 7 • Facilities & Production", "Sector buildings, NPC workforce limits & real-input production", PhaseProgress.PLANNED),
    RoadmapPhase(8, "Phase 8 • TSN Marketplace", "County-specific supply/demand pricing, warehouses & anti-wash controls", PhaseProgress.PLANNED),
    RoadmapPhase(9, "Phase 9 • Government & Treasuries", "14 departments across National, State & County budgets", PhaseProgress.PLANNED),
    RoadmapPhase(10, "Phase 10 • Parties & 3 Campaigns", "State, National & General campaign funds and secure elections", PhaseProgress.PLANNED),
    RoadmapPhase(11, "Phase 11 • Property, Loans & NPCs", "Land, bonds, collateralized loans & grouped NPC simulation", PhaseProgress.PLANNED),
    RoadmapPhase(12, "Phase 12 • Security & Referendum", "2FA, audit logs, anti-exploit safeguards & 200-day Era Referendum", PhaseProgress.PLANNED),
)
