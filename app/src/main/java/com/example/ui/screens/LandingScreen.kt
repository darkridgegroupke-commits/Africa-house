package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.HoaAdaptivePageContainer
import com.example.ui.components.HoaSectionHeader
import com.example.ui.components.KenteFiligreeDivider
import com.example.ui.theme.HoaMonoStyle
import com.example.ui.theme.LocalHoaColors
import kotlinx.coroutines.launch

private data class LandingFeatureSpec(
    val title: String,
    val summary: String,
    val icon: ImageVector,
)

private val landingFeatures = listOf(
    LandingFeatureSpec("Player Identity", "Create a permanent Citizen ID (#12453+), customize your portrait avatar, and build history without XP or level grinds.", Icons.Outlined.Badge),
    LandingFeatureSpec("Business & Companies", "Register private, public, or state enterprises issuing 1,000,000 initial shares and hiring citizens or NPC staff.", Icons.Outlined.Business),
    LandingFeatureSpec("Banking & Finance", "Operate Personal, Capital, and segregated Campaign accounts backed by a double-entry ledger.", Icons.Outlined.AccountBalanceWallet),
    LandingFeatureSpec("African Marketplace", "Trade commodities across county exchanges with supply-demand order books and logistics warehouses.", Icons.Outlined.Storefront),
    LandingFeatureSpec("Government & Taxation", "Serve in 14 ministries, set National/State/County tax rates, and audit public treasuries.", Icons.Outlined.AccountBalance),
    LandingFeatureSpec("Politics & Elections", "Found political parties, run State/National/General campaigns, and cast verified ballots.", Icons.Outlined.HowToVote),
    LandingFeatureSpec("Property & Investment", "Acquire real estate deeds, lease commercial hubs, subscribe treasury bonds, and earn dividends.", Icons.Outlined.Apartment),
    LandingFeatureSpec("Dynamic NPC Economy", "NPC workers, consumers, and civil institutions keep counties operating around the server clock.", Icons.Outlined.Groups),
)

private val howToPlaySteps = listOf(
    "Create your account.",
    "Generate your character.",
    "Choose your starting country and location.",
    "Enter the world.",
    "Work, build, invest and participate in society.",
    "Create your own legacy.",
)

private val aboutBullets = listOf(
    "Build companies.",
    "Work and earn salaries.",
    "Invest in businesses.",
    "Trade in marketplaces.",
    "Participate in politics.",
    "Manage governments.",
    "Compete in elections.",
    "Build wealth and influence.",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LandingScreen(
    onNavigateLogin: () -> Unit,
    onNavigateRegister: () -> Unit,
    onExploreCountry: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hoaColors = LocalHoaColors.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var mobileMenuOpen by remember { mutableStateOf(false) }
    var countrySearch by remember { mutableStateOf("") }
    var activeFooterDialog by remember { mutableStateOf<String?>(null) }

    var aboutOffset by remember { mutableStateOf(0f) }
    var featuresOffset by remember { mutableStateOf(0f) }
    var exploreOffset by remember { mutableStateOf(0f) }
    var howToPlayOffset by remember { mutableStateOf(0f) }

    val scrollTo: (Float) -> Unit = { targetY ->
        scope.launch { scrollState.animateScrollTo(targetY.toInt().coerceAtLeast(0)) }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .testTag("landing_screen_root"),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState),
        ) {
            // A. Responsive Public Header
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(hoaColors.forestHeader)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                val isDesktopOrTablet = maxWidth >= 760.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1140.dp)
                        .align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_hoa_emblem_1795036582659),
                            contentDescription = stringResource(id = R.string.cd_app_emblem),
                            modifier = Modifier.size(38.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                        Text(
                            text = "House of Africa",
                            style = MaterialTheme.typography.titleLarge,
                            color = hoaColors.onForestHeader,
                        )
                    }

                    if (isDesktopOrTablet) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            HeaderNavLink("Home") { scrollTo(0f) }
                            HeaderNavLink("About the Game") { scrollTo(aboutOffset) }
                            HeaderNavLink("Explore Africa") { scrollTo(exploreOffset) }
                            HeaderNavLink("Features") { scrollTo(featuresOffset) }
                            HeaderNavLink("How to Play") { scrollTo(howToPlayOffset) }
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = onNavigateLogin,
                                modifier = Modifier.testTag("landing_header_login_button"),
                                border = BorderStroke(1.dp, hoaColors.goldAccent),
                            ) {
                                Text("Login", color = hoaColors.onForestHeader)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = onNavigateRegister,
                                modifier = Modifier.testTag("landing_header_register_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = hoaColors.goldAccent,
                                    contentColor = hoaColors.onGoldContainer,
                                ),
                            ) {
                                Text("Create Account")
                            }
                        }
                    } else {
                        Box {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = onNavigateRegister,
                                    modifier = Modifier.testTag("landing_mobile_register_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = hoaColors.goldAccent,
                                        contentColor = hoaColors.onGoldContainer,
                                    ),
                                ) {
                                    Text("Create Account")
                                }
                                IconButton(
                                    onClick = { mobileMenuOpen = !mobileMenuOpen },
                                    modifier = Modifier.testTag("landing_mobile_menu_button"),
                                ) {
                                    Icon(
                                        imageVector = if (mobileMenuOpen) Icons.Outlined.Close else Icons.Outlined.Menu,
                                        contentDescription = stringResource(id = R.string.cd_open_menu),
                                        tint = hoaColors.onForestHeader,
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = mobileMenuOpen,
                                onDismissRequest = { mobileMenuOpen = false },
                            ) {
                                DropdownMenuItem(text = { Text("Home") }, onClick = { mobileMenuOpen = false; scrollTo(0f) })
                                DropdownMenuItem(text = { Text("About the Game") }, onClick = { mobileMenuOpen = false; scrollTo(aboutOffset) })
                                DropdownMenuItem(text = { Text("Explore Africa") }, onClick = { mobileMenuOpen = false; scrollTo(exploreOffset) })
                                DropdownMenuItem(text = { Text("Features") }, onClick = { mobileMenuOpen = false; scrollTo(featuresOffset) })
                                DropdownMenuItem(text = { Text("How to Play") }, onClick = { mobileMenuOpen = false; scrollTo(howToPlayOffset) })
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Login") },
                                    onClick = { mobileMenuOpen = false; onNavigateLogin() },
                                )
                                DropdownMenuItem(
                                    text = { Text("Create Account") },
                                    onClick = { mobileMenuOpen = false; onNavigateRegister() },
                                )
                            }
                        }
                    }
                }
            }

            // B. Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(hoaColors.charcoalDeep)
                    .padding(horizontal = 16.dp, vertical = 28.dp),
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1140.dp)
                        .align(Alignment.Center),
                ) {
                    val wideHero = maxWidth >= 820.dp
                    val heroCopy: @Composable (Modifier) -> Unit = { copyMod ->
                        Column(modifier = copyMod, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = hoaColors.goldAccent.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, hoaColors.goldAccent),
                            ) {
                                Text(
                                    text = "PERSISTENT PAN-AFRICAN WORLD • 48H = 1 IN-GAME YEAR",
                                    style = HoaMonoStyle,
                                    color = hoaColors.goldAccent,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                )
                            }
                            Text(
                                text = "YOUR LIFE. YOUR POWER. YOUR AFRICA.",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = hoaColors.onCharcoalDeep,
                            )
                            Text(
                                text = "Build businesses, shape economies, influence governments and create your legacy in a living African world.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = hoaColors.onCharcoalDeep.copy(alpha = 0.85f),
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Button(
                                    onClick = onNavigateRegister,
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("hero_enter_world_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = hoaColors.goldAccent,
                                        contentColor = hoaColors.onGoldContainer,
                                    ),
                                ) {
                                    Text("Enter the World", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                                OutlinedButton(
                                    onClick = { scrollTo(aboutOffset) },
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("hero_discover_game_button"),
                                    border = BorderStroke(1.dp, hoaColors.onCharcoalDeep.copy(alpha = 0.7f)),
                                ) {
                                    Text("Discover the Game", color = hoaColors.onCharcoalDeep)
                                }
                            }
                        }
                    }
                    val heroMapCard: @Composable (Modifier) -> Unit = { mapMod ->
                        Card(
                            modifier = mapMod,
                            shape = MaterialTheme.shapes.large,
                            border = BorderStroke(1.5.dp, hoaColors.goldAccent.copy(alpha = 0.75f)),
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_africa_world_map_1795039446356),
                                    contentDescription = stringResource(id = R.string.cd_world_map),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    hoaColors.charcoalDeep.copy(alpha = 0.1f),
                                                    hoaColors.charcoalDeep.copy(alpha = 0.82f),
                                                ),
                                            ),
                                        ),
                                )
                                Column(
                                    modifier = Modifier.align(Alignment.BottomStart).padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text("12 Sovereign Nations • 28 Counties", style = MaterialTheme.typography.titleMedium, color = hoaColors.onCharcoalDeep)
                                    Text("Server-synchronized world • Real double-entry currencies", style = HoaMonoStyle, color = hoaColors.goldAccent)
                                }
                            }
                        }
                    }

                    if (wideHero) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            heroCopy(Modifier.weight(1.1f))
                            heroMapCard(Modifier.weight(0.9f))
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            heroCopy(Modifier.fillMaxWidth())
                            heroMapCard(Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            HoaAdaptivePageContainer {
                // C. About House of Africa
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { aboutOffset = it.positionInParent().y },
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HoaSectionHeader(
                        title = "About House of Africa",
                        subtitle = "A persistent economic, political and social simulation across Africa",
                        badgeLabel = "PERSISTENT WORLD",
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("landing_about_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Every country, state, county, company, treasury ledger, and election ballot advances on a unified server-controlled game clock — even when players are offline.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                aboutBullets.forEach { item ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                    ) {
                                        Text(
                                            text = "• $item",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                KenteFiligreeDivider()

                // D. Game Features (8 Feature Cards)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { featuresOffset = it.positionInParent().y },
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HoaSectionHeader(
                        title = "Game Features",
                        subtitle = "Eight interconnected simulation pillars without fake currencies or pay-to-win cash-outs",
                        badgeLabel = "8 SYSTEMS",
                    )
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val cols = if (maxWidth >= 780.dp) 2 else 1
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            landingFeatures.chunked(cols).forEach { rowSpecs ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    rowSpecs.forEach { feat ->
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)),
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.Top,
                                            ) {
                                                Surface(
                                                    shape = MaterialTheme.shapes.small,
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                ) {
                                                    Icon(
                                                        imageVector = feat.icon,
                                                        contentDescription = feat.title,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(10.dp).size(22.dp),
                                                    )
                                                }
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(feat.title, style = MaterialTheme.typography.titleSmall)
                                                    Text(feat.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                    if (rowSpecs.size < cols) Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                KenteFiligreeDivider()

                // E. Explore Africa (Zero fake player counts)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { exploreOffset = it.positionInParent().y },
                ) {
                    LandingExploreAfricaSection(
                        countrySearch = countrySearch,
                        onCountrySearchChange = { countrySearch = it },
                        onExploreCountry = onExploreCountry,
                    )
                }

                KenteFiligreeDivider()

                // F. How to Play
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { howToPlayOffset = it.positionInParent().y },
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HoaSectionHeader(
                        title = "How to Play",
                        subtitle = "From account creation to national influence in six structured steps",
                        badgeLabel = "6 STEPS",
                    )
                    howToPlaySteps.forEachIndexed { index, stepText ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = hoaColors.forestHeader,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("${index + 1}", style = MaterialTheme.typography.labelLarge, color = hoaColors.goldAccent)
                                    }
                                }
                                Text(stepText, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                // G. Call to Action
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("landing_cta_card"),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = hoaColors.forestHeader),
                    border = BorderStroke(1.5.dp, hoaColors.goldAccent),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "YOUR STORY IN AFRICA STARTS HERE.",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = hoaColors.onForestHeader,
                        )
                        Text(
                            text = "Claim your unique Citizen ID, settle in any African county, and participate in a persistent economy.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = hoaColors.onForestHeader.copy(alpha = 0.85f),
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                onClick = onNavigateRegister,
                                modifier = Modifier.height(48.dp).testTag("cta_create_character_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = hoaColors.goldAccent,
                                    contentColor = hoaColors.onGoldContainer,
                                ),
                            ) {
                                Text("CREATE YOUR CHARACTER", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onNavigateRegister,
                                modifier = Modifier.height(48.dp).testTag("cta_register_button"),
                                border = BorderStroke(1.dp, hoaColors.goldAccent),
                            ) {
                                Text("Register", color = hoaColors.onForestHeader)
                            }
                            OutlinedButton(
                                onClick = onNavigateLogin,
                                modifier = Modifier.height(48.dp).testTag("cta_login_button"),
                                border = BorderStroke(1.dp, hoaColors.onForestHeader.copy(alpha = 0.7f)),
                            ) {
                                Text("Login", color = hoaColors.onForestHeader)
                            }
                        }
                    }
                }
            }

            // H. Public Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(hoaColors.charcoalDeep)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOf("About", "Rules", "Privacy Policy", "Terms of Service", "Contact", "Community").forEach { item ->
                        TextButton(
                            onClick = { activeFooterDialog = item },
                            modifier = Modifier.testTag("footer_link_${item.lowercase().replace(" ", "_")}"),
                        ) {
                            Text(item, color = hoaColors.goldAccent)
                        }
                    }
                }
                Text(
                    text = "House of Africa • Persistent Pan-African Economic & Political Simulation",
                    style = MaterialTheme.typography.bodySmall,
                    color = hoaColors.onCharcoalDeep.copy(alpha = 0.72f),
                )
            }
        }
    }

    if (activeFooterDialog != null) {
        val title = activeFooterDialog.orEmpty()
        val message = when (title) {
            "Rules" -> "Fair play applies strictly: 1 Main account per person, no multi-account circular wash trading, and campaign funds may never convert to personal balances."
            "Privacy Policy" -> "Citizens choose whether nationality, residence, occupation, net worth, balances, employment, companies, politics, and activity appear on /player/{id}. Deleted accounts resolve as 'User Deleted'."
            "Terms of Service" -> "All House of Africa currencies, bonds, shares, and assets remain strictly inside the game simulation with zero ordinary real-money cash-out."
            "Contact" -> "Official support and civic moderation appeals are handled through verified in-world governance notices and project administrators."
            "Community" -> "Join country parliaments, county chambers of commerce, and political party halls once settled inside Africa."
            else -> "House of Africa is a persistent simulation where 48 real hours equal 1 in-game year across 12 seeded African nations."
        }
        AlertDialog(
            onDismissRequest = { activeFooterDialog = null },
            title = { Text(title) },
            text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { activeFooterDialog = null }) { Text("Close") }
            },
        )
    }
}

@Composable
private fun HeaderNavLink(label: String, onClick: () -> Unit) {
    val hoaColors = LocalHoaColors.current
    TextButton(onClick = onClick) {
        Text(text = label, color = hoaColors.onForestHeader)
    }
}
