package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.navigation.HoaDestination
import com.example.ui.navigation.NavGroup
import com.example.ui.screens.AfricaWorldScreen
import com.example.ui.screens.BankingScreen
import com.example.ui.screens.CompaniesScreen
import com.example.ui.screens.EconomyScreen
import com.example.ui.screens.GovernmentScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.MarketplaceScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.PoliticsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.state.HoaShellUiState
import com.example.ui.state.HoaShellViewModel
import com.example.ui.theme.HoaMonoStyle
import com.example.ui.theme.LocalHoaColors
import kotlinx.coroutines.launch

@Composable
fun HoaAppShell(
    uiState: HoaShellUiState,
    viewModel: HoaShellViewModel,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = HoaDestination.fromRoute(backStackEntry?.destination?.route)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navigateTo: (HoaDestination) -> Unit = { dest ->
        if (dest != currentDestination) {
            navController.navigate(dest.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
        scope.launch {
            if (drawerState.isOpen) drawerState.close()
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpandedDesktop = maxWidth >= 960.dp
        val isMediumTablet = maxWidth in 600.dp..<960.dp

        when {
            isExpandedDesktop -> {
                PermanentNavigationDrawer(
                    drawerContent = {
                        PermanentDrawerSheet(
                            modifier = Modifier.width(276.dp),
                            drawerContainerColor = MaterialTheme.colorScheme.surface,
                        ) {
                            HoaDrawerMenuContent(
                                currentDestination = currentDestination,
                                onNavigate = navigateTo,
                            )
                        }
                    },
                ) {
                    HoaScaffoldContent(
                        currentDestination = currentDestination,
                        navController = navController,
                        uiState = uiState,
                        viewModel = viewModel,
                        onNavigate = navigateTo,
                        showMenuButton = false,
                        showBottomBar = false,
                        onOpenDrawer = {},
                    )
                }
            }

            isMediumTablet -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    HoaNavigationRail(
                        currentDestination = currentDestination,
                        onNavigate = navigateTo,
                    )
                    HoaScaffoldContent(
                        currentDestination = currentDestination,
                        navController = navController,
                        uiState = uiState,
                        viewModel = viewModel,
                        onNavigate = navigateTo,
                        showMenuButton = false,
                        showBottomBar = false,
                        onOpenDrawer = {},
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            else -> {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.width(296.dp),
                            drawerContainerColor = MaterialTheme.colorScheme.surface,
                        ) {
                            HoaDrawerMenuContent(
                                currentDestination = currentDestination,
                                onNavigate = navigateTo,
                            )
                        }
                    },
                ) {
                    HoaScaffoldContent(
                        currentDestination = currentDestination,
                        navController = navController,
                        uiState = uiState,
                        viewModel = viewModel,
                        onNavigate = navigateTo,
                        showMenuButton = true,
                        showBottomBar = true,
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HoaScaffoldContent(
    currentDestination: HoaDestination,
    navController: NavHostController,
    uiState: HoaShellUiState,
    viewModel: HoaShellViewModel,
    onNavigate: (HoaDestination) -> Unit,
    showMenuButton: Boolean,
    showBottomBar: Boolean,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hoaColors = LocalHoaColors.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = hoaColors.forestHeader,
                    titleContentColor = hoaColors.onForestHeader,
                    navigationIconContentColor = hoaColors.onForestHeader,
                    actionIconContentColor = hoaColors.onForestHeader,
                ),
                navigationIcon = {
                    if (showMenuButton) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("nav_drawer_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = stringResource(id = R.string.cd_open_menu),
                            )
                        }
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (!showMenuButton) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_hoa_emblem_1795036582659),
                                contentDescription = stringResource(id = R.string.cd_app_emblem),
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        Column {
                            Text(
                                text = currentDestination.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = currentDestination.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = hoaColors.onForestHeader.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigate(HoaDestination.NOTIFICATIONS) },
                        modifier = Modifier.testTag("topbar_notifications_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = stringResource(id = R.string.cd_notifications),
                        )
                    }
                    IconButton(
                        onClick = { onNavigate(HoaDestination.PROFILE) },
                        modifier = Modifier.testTag("topbar_profile_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = stringResource(id = R.string.cd_profile),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    windowInsets = WindowInsets.navigationBars,
                ) {
                    HoaDestination.mobileBottomDestinations.forEach { dest ->
                        val selected = currentDestination == dest
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onNavigate(dest) },
                            modifier = Modifier.testTag("bottom_nav_${dest.route}"),
                            icon = {
                                Icon(
                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = dest.title,
                                )
                            },
                            label = {
                                Text(
                                    text = dest.shortLabel,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = hoaColors.goldContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HoaDestination.HOME.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(HoaDestination.HOME.route) {
                HomeDashboardScreen(
                    uiState = uiState,
                    onNavigate = onNavigate,
                )
            }
            composable(HoaDestination.PROFILE.route) {
                ProfileScreen(
                    uiState = uiState,
                    onSelectSection = viewModel::selectProfileSection,
                )
            }
            composable(HoaDestination.AFRICA_WORLD.route) {
                AfricaWorldScreen()
            }
            composable(HoaDestination.GOVERNMENT.route) {
                GovernmentScreen()
            }
            composable(HoaDestination.POLITICS.route) {
                PoliticsScreen(
                    uiState = uiState,
                    onSelectCampaignTab = viewModel::selectCampaignTab,
                )
            }
            composable(HoaDestination.ECONOMY.route) {
                EconomyScreen()
            }
            composable(HoaDestination.COMPANIES.route) {
                CompaniesScreen()
            }
            composable(HoaDestination.MARKETPLACE.route) {
                MarketplaceScreen()
            }
            composable(HoaDestination.BANKING.route) {
                BankingScreen(
                    uiState = uiState,
                    onSelectAccountCategory = viewModel::selectBankCategory,
                )
            }
            composable(HoaDestination.NOTIFICATIONS.route) {
                NotificationsScreen(
                    uiState = uiState,
                    onSelectFilter = viewModel::selectNotificationFilter,
                )
            }
            composable(HoaDestination.SETTINGS.route) {
                SettingsScreen(
                    uiState = uiState,
                    onSelectTheme = viewModel::setThemePreference,
                    onToggleNationality = viewModel::toggleVisibilityNationality,
                    onToggleResidence = viewModel::toggleVisibilityResidence,
                    onToggleOccupation = viewModel::toggleVisibilityOccupation,
                    onToggleActivity = viewModel::toggleVisibilityActivity,
                )
            }
        }
    }
}

@Composable
private fun HoaDrawerMenuContent(
    currentDestination: HoaDestination,
    onNavigate: (HoaDestination) -> Unit,
) {
    val hoaColors = LocalHoaColors.current

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(bottom = 24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(hoaColors.forestHeader)
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_hoa_emblem_1795036582659),
                    contentDescription = stringResource(id = R.string.cd_app_emblem),
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Column {
                    Text(
                        text = "House of Africa",
                        style = MaterialTheme.typography.headlineSmall,
                        color = hoaColors.onForestHeader,
                    )
                    Surface(
                        shape = CircleShape,
                        color = hoaColors.goldAccent.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, hoaColors.goldAccent.copy(alpha = 0.7f)),
                    ) {
                        Text(
                            text = "PHASE 1 • UI SHELL",
                            style = HoaMonoStyle,
                            color = hoaColors.onForestHeader,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        NavGroup.entries.forEach { group ->
            val destinations = HoaDestination.entries.filter { it.group == group }
            Text(
                text = group.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            destinations.forEach { dest ->
                val selected = dest == currentDestination
                NavigationDrawerItem(
                    selected = selected,
                    onClick = { onNavigate(dest) },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .testTag("drawer_nav_${dest.route}"),
                    icon = {
                        Icon(
                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = {
                        Text(
                            text = dest.title,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = hoaColors.goldContainer,
                        selectedIconColor = hoaColors.onGoldContainer,
                        selectedTextColor = hoaColors.onGoldContainer,
                    ),
                )
            }
            if (group != NavGroup.entries.last()) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                )
            }
        }
    }
}

@Composable
private fun HoaNavigationRail(
    currentDestination: HoaDestination,
    onNavigate: (HoaDestination) -> Unit,
) {
    val hoaColors = LocalHoaColors.current
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Image(
                painter = painterResource(id = R.drawable.ic_hoa_emblem_1795036582659),
                contentDescription = stringResource(id = R.string.cd_app_emblem),
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .size(38.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        },
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HoaDestination.entries.forEach { dest ->
                val selected = dest == currentDestination
                NavigationRailItem(
                    selected = selected,
                    onClick = { onNavigate(dest) },
                    modifier = Modifier.testTag("rail_nav_${dest.route}"),
                    icon = {
                        Icon(
                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                            contentDescription = dest.title,
                        )
                    },
                    label = {
                        Text(
                            text = dest.shortLabel,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = hoaColors.onGoldContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = hoaColors.goldContainer,
                    ),
                )
            }
        }
    }
}
