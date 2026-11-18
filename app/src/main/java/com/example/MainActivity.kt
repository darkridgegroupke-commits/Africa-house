package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.BankingAndEmploymentRepository
import com.example.data.repository.ClockAndIdentityRepository
import com.example.data.repository.CompanyAndMarketRepository
import com.example.data.repository.GovernancePoliticsAssetsRepository
import com.example.data.repository.WorldGeographyRepository
import com.example.ui.HoaAppShell
import com.example.ui.WorldShellBindings
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.reloadCurrentUserEmailVerified
import com.example.ui.auth.sendCurrentUserEmailVerification
import com.example.ui.auth.signOutFromHouseOfAfrica
import com.example.ui.screens.CharacterCreationWizardScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.PublicCountryPreviewScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.state.HoaShellViewModel
import com.example.ui.state.PendingRegistrationDraft
import com.example.ui.state.SimulationShellBindings
import com.example.ui.state.SimulationViewModel
import com.example.ui.state.UiState
import com.example.ui.state.WorldGeographyViewModel
import com.example.ui.state.asActions
import com.example.ui.theme.HouseOfAfricaTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val shellViewModel: HoaShellViewModel = viewModel()
            val shellUiState by shellViewModel.uiState.collectAsStateWithLifecycle()

            HouseOfAfricaTheme(themePreference = shellUiState.themePreference) {
                AppNavigation(shellViewModel = shellViewModel)
            }
        }
    }
}

@Composable
private fun AppNavigation(shellViewModel: HoaShellViewModel) {
    val auth = remember { Firebase.auth }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val shellUiState by shellViewModel.uiState.collectAsStateWithLifecycle()
    val user = currentUser

    if (user == null) {
        PublicUnauthenticatedFlow(
            pendingDraft = shellUiState.pendingRegistration,
            onDraftChange = { draft -> shellViewModel.updatePendingRegistration { draft } },
        )
    } else {
        val uid = user.uid
        val displayName = user.displayName ?: "African Citizen"
        val userEmail = user.email.orEmpty()
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val credentialManager = remember(context) { CredentialManager.create(context) }
        var verifiedEmailState by remember(uid) { mutableStateOf(user.isEmailVerified) }
        var authFeedbackMessage by remember(uid) { mutableStateOf<com.example.ui.state.ActionFeedback?>(null) }

        key(uid) {
            val worldViewModel: WorldGeographyViewModel = viewModel(
                key = "world_$uid",
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY]) {
                            "APPLICATION_KEY missing from CreationExtras"
                        }
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        WorldGeographyViewModel(
                            repository = WorldGeographyRepository(db = db),
                            currentUserId = uid,
                            initialDisplayName = displayName,
                        )
                    }
                },
            )
            val simViewModel: SimulationViewModel = viewModel(
                key = "sim_$uid",
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY]) {
                            "APPLICATION_KEY missing from CreationExtras"
                        }
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        SimulationViewModel(
                            uid = uid,
                            clockIdentityRepo = ClockAndIdentityRepository(db = db),
                            bankingRepo = BankingAndEmploymentRepository(db = db),
                            companyMarketRepo = CompanyAndMarketRepository(db = db),
                            govPoliticsRepo = GovernancePoliticsAssetsRepository(db = db),
                        )
                    }
                },
            )
            val worldState by worldViewModel.worldState.collectAsStateWithLifecycle()
            val searchQuery by worldViewModel.searchQuery.collectAsStateWithLifecycle()
            val relocationFeedback by worldViewModel.relocationFeedback.collectAsStateWithLifecycle()
            val bootstrapError by worldViewModel.bootstrapError.collectAsStateWithLifecycle()
            val simSnapshot by simViewModel.snapshot.collectAsStateWithLifecycle()
            val simFeedback by simViewModel.feedback.collectAsStateWithLifecycle()
            val playerLookupResult by simViewModel.playerLookupResult.collectAsStateWithLifecycle()
            val usernameAvailability by simViewModel.usernameAvailability.collectAsStateWithLifecycle()
            val simActions = remember(simViewModel) { simViewModel.asActions() }

            when (val ws = worldState) {
                is UiState.Loading -> {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_hoa_emblem_1795036582659),
                                    contentDescription = stringResource(id = R.string.cd_app_emblem),
                                    modifier = Modifier.size(68.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                                CircularProgressIndicator()
                                Text("Synchronizing House of Africa...", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }

                else -> {
                    val userAccount = (ws as? UiState.Success)?.data?.userAccount
                    val needsUsername = ws is UiState.Success && userAccount != null && userAccount.username.isBlank()
                    val hasMainCharacter = (userAccount?.mainPlayerId ?: 0L) > 0L ||
                        simSnapshot.myCharacters.any { it.characterRole == "MAIN" && it.accountStatus != "DELETED" }
                    val needsFirstCharacter = ws is UiState.Success && userAccount != null && !needsUsername && !hasMainCharacter

                    when {
                        needsUsername -> {
                            RegistrationScreen(
                                isSignedIn = true,
                                initialEmail = userEmail,
                                initialDisplayName = displayName,
                                emailVerified = verifiedEmailState,
                                pendingDraft = shellUiState.pendingRegistration,
                                usernameAvailability = usernameAvailability,
                                feedback = authFeedbackMessage ?: simFeedback,
                                onDraftChange = { draft -> shellViewModel.updatePendingRegistration { draft } },
                                onCheckUsernameAvailability = simActions::checkUsernameAvailability,
                                onSendVerificationEmail = {
                                    scope.launch {
                                        authFeedbackMessage = com.example.ui.state.ActionFeedback(isBusy = true, message = "Sending verification email...")
                                        sendCurrentUserEmailVerification().fold(
                                            onSuccess = {
                                                authFeedbackMessage = com.example.ui.state.ActionFeedback(
                                                    isBusy = false,
                                                    message = "Verification email sent to $userEmail. Check your inbox then tap Refresh.",
                                                )
                                            },
                                            onFailure = { err ->
                                                authFeedbackMessage = com.example.ui.state.ActionFeedback(
                                                    isBusy = false,
                                                    message = err.localizedMessage ?: "Unable to send verification email.",
                                                    isError = true,
                                                )
                                            },
                                        )
                                    }
                                },
                                onRefreshEmailVerification = {
                                    scope.launch {
                                        authFeedbackMessage = com.example.ui.state.ActionFeedback(isBusy = true, message = "Refreshing verification status...")
                                        reloadCurrentUserEmailVerified().fold(
                                            onSuccess = { verified ->
                                                verifiedEmailState = verified
                                                simActions.syncEmailVerification(verified)
                                                authFeedbackMessage = com.example.ui.state.ActionFeedback(
                                                    isBusy = false,
                                                    message = if (verified) "Email verified via Firebase Authentication!" else "Still pending verification. Open the link in your email first.",
                                                    isError = !verified,
                                                )
                                            },
                                            onFailure = { err ->
                                                authFeedbackMessage = com.example.ui.state.ActionFeedback(
                                                    isBusy = false,
                                                    message = err.localizedMessage ?: "Refresh failed.",
                                                    isError = true,
                                                )
                                            },
                                        )
                                    }
                                },
                                onCompleteRegistration = { username, email, name, verified ->
                                    authFeedbackMessage = null
                                    simActions.completeAccountRegistration(
                                        username = username,
                                        email = email,
                                        displayName = name,
                                        emailVerified = verified,
                                        onSuccess = { shellViewModel.clearPendingRegistration() },
                                    )
                                },
                                onBackToLanding = {
                                    shellViewModel.clearPendingRegistration()
                                    signOutFromHouseOfAfrica(credentialManager = credentialManager, scope = scope)
                                },
                            )
                        }

                        needsFirstCharacter -> {
                            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                                CharacterCreationWizardScreen(
                                    sim = simSnapshot,
                                    feedback = simFeedback,
                                    actions = simActions,
                                    asAlt = false,
                                    onCompleted = {},
                                )
                            }
                        }

                        else -> {
                            HoaAppShell(
                                uiState = shellUiState,
                                viewModel = shellViewModel,
                                worldBindings = WorldShellBindings(
                                    worldState = worldState,
                                    searchQuery = searchQuery,
                                    relocationFeedback = relocationFeedback,
                                    bootstrapError = bootstrapError,
                                    userDisplayName = displayName,
                                    userEmail = userEmail,
                                    onSearchQueryChange = worldViewModel::updateSearchQuery,
                                    onSelectCountry = worldViewModel::selectCountry,
                                    onSelectState = worldViewModel::selectState,
                                    onSettleOrRelocate = worldViewModel::settleOrRelocate,
                                    onRetryBootstrap = { worldViewModel.retryBootstrap(displayName) },
                                    onSignOut = {
                                        shellViewModel.clearPendingRegistration()
                                        signOutFromHouseOfAfrica(
                                            credentialManager = credentialManager,
                                            scope = scope,
                                        )
                                    },
                                ),
                                simBindings = SimulationShellBindings(
                                    snapshot = simSnapshot,
                                    feedback = simFeedback,
                                    playerLookupResult = playerLookupResult,
                                    usernameAvailability = usernameAvailability,
                                    actions = simActions,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicUnauthenticatedFlow(
    pendingDraft: PendingRegistrationDraft,
    onDraftChange: (PendingRegistrationDraft) -> Unit,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "landing",
    ) {
        composable("landing") {
            LandingScreen(
                onNavigateLogin = { navController.navigate("login") },
                onNavigateRegister = { navController.navigate("register") },
                onExploreCountry = { countryId -> navController.navigate("world/$countryId") },
            )
        }
        composable("login") {
            AuthScreen(
                onAuthSuccess = {},
                onBackToLanding = { navController.popBackStack() },
                onGoToRegister = { navController.navigate("register") },
            )
        }
        composable("register") {
            RegistrationScreen(
                isSignedIn = false,
                pendingDraft = pendingDraft,
                onDraftChange = onDraftChange,
                onBackToLanding = { navController.popBackStack() },
                onGoToLogin = { navController.navigate("login") },
            )
        }
        composable(
            route = "world/{countryId}",
            arguments = listOf(navArgument("countryId") { type = NavType.LongType }),
        ) { entry ->
            val countryId = entry.arguments?.getLong("countryId") ?: 101L
            PublicCountryPreviewScreen(
                countryId = countryId,
                onBack = { navController.popBackStack() },
                onCreateAccount = { navController.navigate("register") },
            )
        }
    }
}
