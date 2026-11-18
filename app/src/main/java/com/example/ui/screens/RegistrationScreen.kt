package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.domain.OnboardingEngine
import com.example.ui.auth.onGoogleSignInClicked
import com.example.ui.components.ActionFeedbackBanner
import com.example.ui.components.KenteFiligreeDivider
import com.example.ui.state.ActionFeedback
import com.example.ui.state.PendingRegistrationDraft
import com.example.ui.theme.HoaMonoStyle
import com.example.ui.theme.LocalHoaColors

@Composable
fun RegistrationScreen(
    isSignedIn: Boolean,
    initialEmail: String = "",
    initialDisplayName: String = "",
    emailVerified: Boolean = false,
    pendingDraft: PendingRegistrationDraft = PendingRegistrationDraft(),
    usernameAvailability: Pair<String, Boolean?>? = null,
    feedback: ActionFeedback = ActionFeedback(),
    onDraftChange: ((PendingRegistrationDraft) -> Unit)? = null,
    onCheckUsernameAvailability: ((String) -> Unit)? = null,
    onSendVerificationEmail: (() -> Unit)? = null,
    onRefreshEmailVerification: (() -> Unit)? = null,
    onCompleteRegistration: ((username: String, email: String, displayName: String, emailVerified: Boolean) -> Unit)? = null,
    onBackToLanding: (() -> Unit)? = null,
    onGoToLogin: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val hoaColors = LocalHoaColors.current

    var username by remember(pendingDraft.username) { mutableStateOf(pendingDraft.username) }
    var email by remember(initialEmail, pendingDraft.email) {
        mutableStateOf(initialEmail.ifBlank { pendingDraft.email })
    }
    var displayName by remember(initialDisplayName, pendingDraft.displayName) {
        mutableStateOf(pendingDraft.displayName.ifBlank { initialDisplayName })
    }
    var acceptedTerms by remember(pendingDraft.acceptedTerms) { mutableStateOf(pendingDraft.acceptedTerms) }
    var localError by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var lastSubmitMillis by remember { mutableLongStateOf(0L) }

    val usernameCheck = OnboardingEngine.validateUsername(username)
    val emailCheck = OnboardingEngine.validateEmail(email)
    val availabilityMatchesInput = usernameAvailability?.first?.equals(username.trim(), ignoreCase = true) == true
    val isUsernameAvailable = if (availabilityMatchesInput) usernameAvailability?.second else null

    fun syncDraft(
        nextUsername: String = username,
        nextEmail: String = email,
        nextDisplayName: String = displayName,
        nextAcceptedTerms: Boolean = acceptedTerms,
        nextAutoSubmit: Boolean = pendingDraft.autoSubmitAfterSignIn,
    ) {
        onDraftChange?.invoke(
            PendingRegistrationDraft(
                username = nextUsername,
                email = nextEmail,
                displayName = nextDisplayName,
                acceptedTerms = nextAcceptedTerms,
                autoSubmitAfterSignIn = nextAutoSubmit,
            ),
        )
    }

    // Check username availability automatically once signed in with a valid carried-over draft username,
    // and auto-claim if the user initiated submission directly before Google Sign-In.
    LaunchedEffect(isSignedIn, pendingDraft.username, pendingDraft.autoSubmitAfterSignIn, emailVerified) {
        if (isSignedIn) {
            val cleanUser = username.trim()
            if (OnboardingEngine.validateUsername(cleanUser) is OnboardingEngine.ValidationResult.Valid) {
                onCheckUsernameAvailability?.invoke(cleanUser)
            }
            if (
                pendingDraft.autoSubmitAfterSignIn &&
                acceptedTerms &&
                emailVerified &&
                OnboardingEngine.validateUsername(cleanUser) is OnboardingEngine.ValidationResult.Valid &&
                OnboardingEngine.validateEmail(email) is OnboardingEngine.ValidationResult.Valid &&
                onCompleteRegistration != null
            ) {
                syncDraft(nextAutoSubmit = false)
                val resolvedEmail = initialEmail.ifBlank { email.trim() }
                val resolvedName = displayName.ifBlank { initialDisplayName.ifBlank { cleanUser } }
                onCompleteRegistration(cleanUser, resolvedEmail, resolvedName, emailVerified)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .testTag("registration_screen_root"),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (onBackToLanding != null) {
                        IconButton(onClick = onBackToLanding, modifier = Modifier.testTag("register_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to landing page")
                        }
                    }
                    Text("/register", style = HoaMonoStyle, color = MaterialTheme.colorScheme.primary)
                }

                Text(
                    text = if (isSignedIn) "Complete Citizen Account" else "Create Your Account",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (isSignedIn) {
                        "Your identity is authenticated. Reserve your unique @username and confirm email verification to enter character creation."
                    } else {
                        "Reserve a unique @username, agree to House of Africa rules, and authenticate via Google Credential Manager to create your character."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                ActionFeedbackBanner(feedback = feedback)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)),
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                localError = null
                                syncDraft(nextEmail = it)
                            },
                            label = { Text("Email Address") },
                            readOnly = isSignedIn && initialEmail.isNotBlank(),
                            singleLine = true,
                            isError = email.isNotBlank() && emailCheck is OnboardingEngine.ValidationResult.Invalid,
                            supportingText = {
                                if (isSignedIn && emailVerified) {
                                    Text("Verified via Firebase Authentication")
                                } else if (email.isNotBlank() && emailCheck is OnboardingEngine.ValidationResult.Invalid) {
                                    Text(emailCheck.message)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("register_email_input"),
                        )

                        if (isSignedIn) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = if (emailVerified) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                                border = BorderStroke(
                                    1.dp,
                                    if (emailVerified) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                    else MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("register_email_verification_card"),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = if (emailVerified) Icons.Outlined.MarkEmailRead else Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = if (emailVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Text(
                                            text = if (emailVerified) {
                                                "Email Verified • Ready for Citizen Onboarding"
                                            } else {
                                                "Email Verification Required"
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                    if (!emailVerified) {
                                        Text(
                                            text = "Verify your email link via Firebase Authentication before reserving your citizen handle.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            if (onSendVerificationEmail != null) {
                                                OutlinedButton(
                                                    onClick = onSendVerificationEmail,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                        .testTag("register_send_verification_button"),
                                                ) {
                                                    Text("Send Link")
                                                }
                                            }
                                            if (onRefreshEmailVerification != null) {
                                                OutlinedButton(
                                                    onClick = onRefreshEmailVerification,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                        .testTag("register_refresh_verification_button"),
                                                ) {
                                                    Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Refresh")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = username,
                            onValueChange = { raw ->
                                val filtered = raw.filter { it.isLetterOrDigit() || it == '_' }.take(24)
                                username = filtered
                                localError = null
                                syncDraft(nextUsername = filtered)
                                if (isSignedIn && OnboardingEngine.validateUsername(filtered) is OnboardingEngine.ValidationResult.Valid) {
                                    onCheckUsernameAvailability?.invoke(filtered)
                                }
                            },
                            label = { Text("Unique Username (@handle)") },
                            singleLine = true,
                            isError = (username.isNotBlank() && usernameCheck is OnboardingEngine.ValidationResult.Invalid) ||
                                isUsernameAvailable == false,
                            supportingText = {
                                when {
                                    username.isNotBlank() && usernameCheck is OnboardingEngine.ValidationResult.Invalid ->
                                        Text(usernameCheck.message)
                                    isUsernameAvailable == true ->
                                        Text("@${username.lowercase()} is available!", color = MaterialTheme.colorScheme.primary)
                                    isUsernameAvailable == false ->
                                        Text("@${username.lowercase()} is already claimed.", color = MaterialTheme.colorScheme.error)
                                    else ->
                                        Text("3–24 chars: letters, numbers, single internal underscores.")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("register_username_input"),
                        )

                        if (isSignedIn && onCheckUsernameAvailability != null) {
                            OutlinedButton(
                                onClick = { onCheckUsernameAvailability(username) },
                                enabled = usernameCheck is OnboardingEngine.ValidationResult.Valid,
                                modifier = Modifier.height(48.dp).testTag("register_check_username_button"),
                            ) {
                                Text("Check Availability")
                            }
                        }

                        OutlinedTextField(
                            value = displayName,
                            onValueChange = {
                                displayName = it
                                syncDraft(nextDisplayName = it)
                            },
                            label = { Text("Preferred Display Name (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("register_display_name_input"),
                        )

                        KenteFiligreeDivider()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Checkbox(
                                checked = acceptedTerms,
                                onCheckedChange = {
                                    acceptedTerms = it
                                    localError = null
                                    syncDraft(nextAcceptedTerms = it)
                                },
                                modifier = Modifier.testTag("register_terms_checkbox"),
                            )
                            Text(
                                text = "I agree to the House of Africa Terms of Service, Privacy Policy, and anti-multi-account fair play rules.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, hoaColors.goldAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().testTag("register_security_policy_card"),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Auth & Hashing Policy: Sign-in runs via Firebase Authentication (Google Credential Manager) with atomic /usernames uniqueness locks in Firestore and zero client password storage. To enable standalone email/password login with Firebase server-side scrypt hashing (${OnboardingEngine.MIN_PASSWORD_LENGTH}+ chars, upper/lower/digit/symbol), turn on Email/Password in Firebase Console.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        if (!localError.isNullOrBlank()) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth().testTag("register_error_banner"),
                            ) {
                                Text(
                                    text = localError.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val now = System.currentTimeMillis()
                                if (now - lastSubmitMillis < OnboardingEngine.REGISTRATION_RATE_LIMIT_MS) {
                                    localError = "Please wait a moment before retrying registration."
                                    return@Button
                                }
                                if (emailCheck is OnboardingEngine.ValidationResult.Invalid) {
                                    localError = emailCheck.message
                                    return@Button
                                }
                                if (usernameCheck is OnboardingEngine.ValidationResult.Invalid) {
                                    localError = usernameCheck.message
                                    return@Button
                                }
                                if (isUsernameAvailable == false) {
                                    localError = "Username @$username is already taken."
                                    return@Button
                                }
                                if (!acceptedTerms) {
                                    localError = "Accept Terms of Service & Fair Play Rules to continue."
                                    return@Button
                                }
                                if (isSignedIn && !emailVerified) {
                                    localError = "Verify your email address first using the Verification card above."
                                    return@Button
                                }
                                lastSubmitMillis = now
                                if (isSignedIn) {
                                    onCompleteRegistration?.invoke(
                                        username.trim(),
                                        email.trim(),
                                        displayName.ifBlank { username.trim() },
                                        emailVerified,
                                    )
                                } else {
                                    isAuthenticating = true
                                    syncDraft(nextAutoSubmit = true)
                                    onGoogleSignInClicked(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onAuthSuccess = {
                                            isAuthenticating = false
                                        },
                                        onAuthError = { err ->
                                            isAuthenticating = false
                                            syncDraft(nextAutoSubmit = false)
                                            localError = err
                                        },
                                        scope = scope,
                                        onAuthCancelled = {
                                            isAuthenticating = false
                                            syncDraft(nextAutoSubmit = false)
                                        },
                                    )
                                }
                            },
                            enabled = !feedback.isBusy && !isAuthenticating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("register_submit_button"),
                        ) {
                            if (feedback.isBusy || isAuthenticating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reserving Citizen Account...")
                            } else {
                                Icon(
                                    imageVector = if (isSignedIn) Icons.Outlined.CheckCircle else Icons.Outlined.VerifiedUser,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isSignedIn) "Reserve @${username.ifBlank { "username" }} & Create Character" else "Verify with Google & Create Account")
                            }
                        }

                        if (onGoToLogin != null && !isSignedIn) {
                            OutlinedButton(
                                onClick = onGoToLogin,
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("register_go_login_button"),
                            ) {
                                Text("Already registered? Login")
                            }
                        }
                    }
                }
            }
        }
    }
}
