package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavGroup(val label: String) {
    CORE("Core Hub"),
    WORLD_AND_STATE("World & Governance"),
    COMMERCE_AND_FINANCE("Economy & Finance"),
    CITIZEN("Citizen & System"),
}

enum class HoaDestination(
    val route: String,
    val title: String,
    val shortLabel: String,
    val subtitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val group: NavGroup,
    val showInMobileBottomBar: Boolean = false,
) {
    HOME(
        route = "home",
        title = "Home",
        shortLabel = "Home",
        subtitle = "Sovereign Command Dashboard",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        group = NavGroup.CORE,
        showInMobileBottomBar = true,
    ),
    PROFILE(
        route = "profile",
        title = "Profile",
        shortLabel = "Profile",
        subtitle = "Citizen Identity & 16-Section Dossier",
        selectedIcon = Icons.Filled.Badge,
        unselectedIcon = Icons.Outlined.Badge,
        group = NavGroup.CITIZEN,
    ),
    AFRICA_WORLD(
        route = "world",
        title = "Africa / World",
        shortLabel = "World",
        subtitle = "Countries, States, Counties & Clean IDs",
        selectedIcon = Icons.Filled.Public,
        unselectedIcon = Icons.Outlined.Public,
        group = NavGroup.WORLD_AND_STATE,
        showInMobileBottomBar = true,
    ),
    GOVERNMENT(
        route = "government",
        title = "Government",
        shortLabel = "Gov",
        subtitle = "National, State & County Administration",
        selectedIcon = Icons.Filled.AccountBalance,
        unselectedIcon = Icons.Outlined.AccountBalance,
        group = NavGroup.WORLD_AND_STATE,
    ),
    POLITICS(
        route = "politics",
        title = "Politics",
        shortLabel = "Politics",
        subtitle = "Parties, Campaigns & Elections",
        selectedIcon = Icons.Filled.HowToVote,
        unselectedIcon = Icons.Outlined.HowToVote,
        group = NavGroup.WORLD_AND_STATE,
        showInMobileBottomBar = true,
    ),
    ECONOMY(
        route = "economy",
        title = "Economy",
        shortLabel = "Economy",
        subtitle = "Game Clock, Macro Stats & Era Referendum",
        selectedIcon = Icons.Filled.TrendingUp,
        unselectedIcon = Icons.Outlined.TrendingUp,
        group = NavGroup.COMMERCE_AND_FINANCE,
    ),
    COMPANIES(
        route = "companies",
        title = "Companies",
        shortLabel = "Companies",
        subtitle = "Registrations, Shares, Facilities & NPCs",
        selectedIcon = Icons.Filled.Business,
        unselectedIcon = Icons.Outlined.Business,
        group = NavGroup.COMMERCE_AND_FINANCE,
    ),
    MARKETPLACE(
        route = "marketplace",
        title = "Marketplace",
        shortLabel = "Market",
        subtitle = "TSN County Exchange & Logistics",
        selectedIcon = Icons.Filled.Storefront,
        unselectedIcon = Icons.Outlined.Storefront,
        group = NavGroup.COMMERCE_AND_FINANCE,
        showInMobileBottomBar = true,
    ),
    BANKING(
        route = "banking",
        title = "Banking",
        shortLabel = "Banking",
        subtitle = "Double-Entry Central Ledger & Taxes",
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet,
        group = NavGroup.COMMERCE_AND_FINANCE,
        showInMobileBottomBar = true,
    ),
    NOTIFICATIONS(
        route = "notifications",
        title = "Notifications",
        shortLabel = "Alerts",
        subtitle = "Audit Events, Salaries & Official Notices",
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications,
        group = NavGroup.CITIZEN,
    ),
    SETTINGS(
        route = "settings",
        title = "Settings",
        shortLabel = "Settings",
        subtitle = "Display, Privacy & Security Readiness",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        group = NavGroup.CITIZEN,
    );

    companion object {
        fun fromRoute(route: String?): HoaDestination = when {
            route == null || route == "dashboard" -> HOME
            route.startsWith("player/") || route.startsWith("character/create") -> PROFILE
            route.startsWith("world/") || route.startsWith("state/") || route.startsWith("county/") -> AFRICA_WORLD
            else -> entries.firstOrNull { it.route == route } ?: HOME
        }

        val mobileBottomDestinations: List<HoaDestination> =
            entries.filter { it.showInMobileBottomBar }
    }
}

/**
 * Canonical non-recycled entity route builders prepared for deep-linking.
 */
object HoaEntityRoutes {
    fun landing() = "/"
    fun register() = "/register"
    fun login() = "/login"
    fun characterCreate() = "/character/create"
    fun dashboard() = "/dashboard"
    fun settings() = "/settings"
    fun world(countryId: Long) = "/world/$countryId"
    fun state(stateId: Long) = "/state/$stateId"
    fun county(countyId: Long) = "/county/$countyId"
    fun player(playerId: Long) = "/player/$playerId"
    fun company(companyId: Long) = "/company/$companyId"
    fun party(partyId: Long) = "/party/$partyId"
    fun election(electionId: Long) = "/election/$electionId"
}
