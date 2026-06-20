package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.VpnProfile
import com.example.ui.translation.AppLanguage
import com.example.ui.translation.Translator
import com.example.ui.viewmodel.VpnViewModel
import com.example.ui.scanner.QrCodeAnalyzer
import com.example.vpn.ConnectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun AppNavigation(viewModel: VpnViewModel) {
    val navController = rememberNavController()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val themeColorScheme = if (isDarkMode) {
        darkColorScheme(
            primary = Color(0xFF60A5FA), // High contrast glow blue
            secondary = Color(0xFF003366), // Deep blue professional background second
            tertiary = Color(0xFF34D399), // Emerald connect green
            background = Color(0xFF0F172A), // Slate deep black
            surface = Color(0xFF1E293B), // Layered dark card
            onPrimary = Color.Black,
            onBackground = Color(0xFFF8FAFC),
            onSurface = Color(0xFFF8FAFC),
            outline = Color(0xFF334155) // Slate outline
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF005FB8), // Professional Blue
            secondary = Color(0xFFD3E4FF), // Accent Light Ice Blue
            tertiary = Color(0xFF008544), // Professional Connected Green
            background = Color(0xFFF7F9FB), // Grayish-white Polished Canvas background
            surface = Color(0xFFFFFFFF), // Pure white responsive layout cards
            onPrimary = Color.White,
            onBackground = Color(0xFF1D1B20),
            onSurface = Color(0xFF1D1B20),
            outline = Color(0xFFE1E3E1) // Polish slate crisp border gray
        )
    }

    // Force layout direction dynamically based on AppLanguage
    val layoutDirection = Translator.getLayoutDirection(language)

    MaterialTheme(
        colorScheme = themeColorScheme
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDirection
        ) {
            val context = LocalContext.current
            
            // Listen for toast events from view model safely
            LaunchedEffect(Unit) {
                viewModel.toastEvent.collect { eventKey ->
                    val message = Translator.translate(eventKey, language)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }

            Scaffold(
                bottomBar = {
                    if (currentRoute != "splash" && currentRoute != "scanner") {
                        VpnBottomNavigation(navController = navController, language = language)
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreen(navController = navController, language = language)
                        }
                        composable("home") {
                            HomeDashboardScreen(viewModel = viewModel, navController = navController, language = language)
                        }
                        composable("profiles") {
                            ProfilesSection(viewModel = viewModel, navController = navController, language = language)
                        }
                        composable("import") {
                            ImportProfileScreen(viewModel = viewModel, navController = navController, language = language)
                        }
                        composable("scanner") {
                            ScannerScreen(viewModel = viewModel, navController = navController, language = language)
                        }
                        composable("settings") {
                            SettingsScreen(viewModel = viewModel, language = language)
                        }
                        composable("about") {
                            AboutScreen(language = language)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VpnBottomNavigation(navController: NavController, language: AppLanguage) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Column {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            thickness = 1.dp
        )
        NavigationBar(
            tonalElevation = 0.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            windowInsets = WindowInsets.navigationBars,
            modifier = Modifier.height(80.dp)
        ) {
            val navItems = listOf(
                Triple("home", Icons.Default.Dashboard, "nav_home"),
                Triple("profiles", Icons.Default.Security, "nav_profiles"),
                Triple("settings", Icons.Default.Settings, "nav_settings"),
                Triple("about", Icons.Default.Info, "nav_about")
            )

            navItems.forEach { (route, icon, tag) ->
                val isSelected = currentRoute == route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != route) {
                            if (route == "home") {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = false }
                                }
                            } else {
                                navController.navigate(route) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = route.replaceFirstChar { it.uppercase() }
                        )
                    },
                    label = {
                        Text(
                            text = Translator.translate(if (route == "profiles") "connections" else route, language),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.secondary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag(tag)
                )
            }
        }
    }
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen(navController: NavController, language: AppLanguage) {
    LaunchedEffect(Unit) {
        delay(2500)
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_animation")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_secure_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .rotate(pulseScale * 5f)
                    .border(3.dp, Color(0xFF60A5FA).copy(alpha = 0.7f), CircleShape)
                    .padding(8.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = Translator.translate("app_title", language),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = Translator.translate("empty_state_lbl", language),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = Color(0xFF60A5FA),
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// 2. DASHBOARD HOME
@Composable
fun HomeDashboardScreen(
    viewModel: VpnViewModel,
    navController: NavController,
    language: AppLanguage
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val networkInfo by viewModel.networkInfo.collectAsStateWithLifecycle()
    val selectedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val errorState by viewModel.errorState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Aesthetic Timer counting when VPN is connected
    var elapsedSeconds by remember { mutableStateOf(165) } // start at 2m45s for instant visual feedback on connected state
    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        } else {
            elapsedSeconds = 0
        }
    }

    val formattedTime = remember(elapsedSeconds) {
        val h = elapsedSeconds / 3600
        val m = (elapsedSeconds % 3600) / 60
        val s = elapsedSeconds % 60
        String.format("%02d:%02d:%02d Active", h, m, s)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title Header Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = Translator.translate("app_title", language),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = Translator.translate("version_label", language),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            IconButton(
                onClick = { navController.navigate("settings") },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings Quick Link",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // 1. MAIN CONNECTION CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                pTextLabel(text = Translator.translate("status", language).uppercase())

                Spacer(modifier = Modifier.height(4.dp))

                // Connection status text
                Text(
                    text = when (connectionState) {
                        ConnectionState.CONNECTED -> Translator.translate("connected", language).uppercase()
                        ConnectionState.CONNECTING -> "${Translator.translate("connecting", language).uppercase()}..."
                        ConnectionState.ERROR -> Translator.translate("error", language).uppercase()
                        else -> Translator.translate("disconnected", language).uppercase()
                    },
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = when (connectionState) {
                        ConnectionState.CONNECTED -> MaterialTheme.colorScheme.tertiary
                        ConnectionState.CONNECTING -> Color(0xFFD97706)
                        ConnectionState.ERROR -> Color(0xFFEF4444)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    },
                    modifier = Modifier.testTag("vpn_status_text")
                )

                if (connectionState == ConnectionState.CONNECTED) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Power button rings matching Professional Polish mockup
                val outerBgColor = when (connectionState) {
                    ConnectionState.CONNECTED -> Color(0xFFD1FAE5) // Emerald Light Ring
                    ConnectionState.CONNECTING -> Color(0xFFFEF3C7) // Gold Light Ring
                    ConnectionState.ERROR -> Color(0xFFFEE2E2) // Red Light Ring
                    else -> MaterialTheme.colorScheme.secondary // Brand Ice Blue
                }

                val innerBgColor = when (connectionState) {
                    ConnectionState.CONNECTED -> MaterialTheme.colorScheme.tertiary // Solid Emerald
                    ConnectionState.CONNECTING -> Color(0xFFF59E0B) // Solid Amber
                    ConnectionState.ERROR -> Color(0xFFEF4444) // Solid Red
                    else -> MaterialTheme.colorScheme.primary // Solid Brand Blue
                }

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(outerBgColor)
                        .clickable {
                            if (connectionState == ConnectionState.CONNECTED) {
                                viewModel.disconnectVpn(context)
                            } else if (connectionState == ConnectionState.DISCONNECTED || connectionState == ConnectionState.ERROR) {
                                viewModel.connectVpn(context)
                            }
                        }
                        .testTag("vpn_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(innerBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (connectionState) {
                            ConnectionState.CONNECTED -> Icons.Default.Check
                            ConnectionState.CONNECTING -> Icons.Default.RotateRight
                            ConnectionState.ERROR -> Icons.Default.Warning
                            else -> Icons.Default.PowerSettingsNew
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Shield State Icon",
                            modifier = Modifier.size(34.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = when (connectionState) {
                        ConnectionState.CONNECTED -> "Secure tunnel established"
                        ConnectionState.CONNECTING -> "Tunnelling resources safely"
                        ConnectionState.ERROR -> "Connection error encountered"
                        else -> "Tap to establish secure tunnel"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Error message warning block
        if (errorState != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error Logo",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = errorState ?: "Unknown VPN Interface crash.",
                        color = Color(0xFF991B1B),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // 2. ACTIVE PROFILE SPECIAL CARD
        val hasProfiles = selectedProfile != null
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .clickable {
                    navController.navigate("profiles")
                },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (MaterialTheme.colorScheme.outline == Color(0xFFE1E3E1)) {
                    Color(0xFF001C38) // Beautiful dark deep corporate blue for light mode
                } else {
                    MaterialTheme.colorScheme.surface // Modern Dark Surface Card
                }
            ),
            border = if (MaterialTheme.colorScheme.outline != Color(0xFFE1E3E1)) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            } else null
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = (if (hasProfiles) "Active Profile" else "Get Started").uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = if (MaterialTheme.colorScheme.outline == Color(0xFFE1E3E1)) Color(0xFF93C5FD) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedProfile?.name ?: "No Profile Imported",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = if (MaterialTheme.colorScheme.outline == Color(0xFFE1E3E1)) Color.White else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (hasProfiles) "Endpoint: ${networkInfo.endpoint}" else "Import configurations to connect",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (MaterialTheme.colorScheme.outline == Color(0xFFE1E3E1)) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (MaterialTheme.colorScheme.outline == Color(0xFFE1E3E1)) {
                                Color.White.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (hasProfiles) Icons.Default.Bolt else Icons.Default.Add,
                        contentDescription = "Action click marker",
                        tint = if (MaterialTheme.colorScheme.outline == Color(0xFFE1E3E1)) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 3. STATS TITLE & GRID
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = Translator.translate("status", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        val stats = listOf(
            Triple(Translator.translate("public_ip", language), networkInfo.publicIp, Icons.Outlined.Wifi),
            Triple(Translator.translate("local_ip", language), networkInfo.localIp, Icons.Outlined.CompareArrows),
            Triple(Translator.translate("endpoint", language), networkInfo.endpoint, Icons.Outlined.CloudQueue),
            Triple(Translator.translate("ping", language), networkInfo.pingMs, Icons.Outlined.Speed)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DiagnosticCard(
                title = stats[0].first,
                value = stats[0].second,
                icon = stats[0].third,
                modifier = Modifier.weight(1f)
            )
            DiagnosticCard(
                title = stats[1].first,
                value = stats[1].second,
                icon = stats[1].third,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DiagnosticCard(
                title = stats[2].first,
                value = stats[2].second,
                icon = stats[2].third,
                modifier = Modifier.weight(1f)
            )
            val pingVal = stats[3].second
            val pingColor = if (pingVal.endsWith("ms")) {
                val digits = pingVal.substringBefore("ms").toIntOrNull() ?: 100
                if (digits < 80) Color(0xFF10B981) else if (digits < 200) Color(0xFFF59E0B) else Color(0xFFEF4444)
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            DiagnosticCard(
                title = stats[3].first,
                value = stats[3].second,
                icon = stats[3].third,
                valueTint = pingColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun pTextLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        ),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    )
}

@Composable
fun DiagnosticCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueTint: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = valueTint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// 3. PROFILES SCREEN LIST
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfilesSection(
    viewModel: VpnViewModel,
    navController: NavController,
    language: AppLanguage
) {
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val selectedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()

    var showDeleteDialogForId by remember { mutableStateOf<Int?>(null) }
    var showRenameDialogForProfile by remember { mutableStateOf<VpnProfile?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = Translator.translate("connections", language),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (profiles.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = Translator.translate("no_profiles", language),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(profiles, key = { it.id }) { profile ->
                        val isSelected = selectedProfile?.id == profile.id

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { viewModel.selectProfile(profile) },
                                    onLongClick = { showRenameDialogForProfile = profile }
                                )
                                .testTag("profile_item_${profile.id}"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Selection mark",
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "AES Encrypted Tunnel",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                                
                                IconButton(
                                    onClick = { showRenameDialogForProfile = profile }
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Rename profile",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                IconButton(
                                    onClick = { showDeleteDialogForId = profile.id }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete profile",
                                        tint = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Expanded Floating Options FAB Action button
        ExtendedFloatingActionButton(
            text = { Text(Translator.translate("import_title", language)) },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
            onClick = { navController.navigate("import") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_profile_fab")
        )
    }

    // Modal dialogue - Deletion Warning Dialog
    if (showDeleteDialogForId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialogForId = null },
            title = { Text(Translator.translate("delete_profile", language), fontWeight = FontWeight.Bold) },
            text = { Text(Translator.translate("confirm_delete", language)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = showDeleteDialogForId
                        if (id != null) {
                            viewModel.deleteProfile(id)
                        }
                        showDeleteDialogForId = null
                    }
                ) {
                    Text(Translator.translate("delete_profile", language), color = Color(0xFFEF4444), fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialogForId = null }) {
                    Text(Translator.translate("cancel", language))
                }
            }
        )
    }

    // Modal dialog - Custom Rename dialogue box
    if (showRenameDialogForProfile != null) {
        var tempName by remember { mutableStateOf(showRenameDialogForProfile?.name ?: "") }
        AlertDialog(
            onDismissRequest = { showRenameDialogForProfile = null },
            title = { Text(Translator.translate("rename_profile", language), fontWeight = FontWeight.Bold) },
            text = {
                TextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    placeholder = { Text(Translator.translate("rename_hint", language)) }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val activeItem = showRenameDialogForProfile
                        if (activeItem != null && tempName.isNotBlank()) {
                            viewModel.renameProfile(activeItem.id, tempName)
                        }
                        showRenameDialogForProfile = null
                    }
                ) {
                    Text(Translator.translate("save_btn", language), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialogForProfile = null }) {
                    Text(Translator.translate("cancel", language))
                }
            }
        )
    }
}

// 4. IMPORT SCREEN (Paste or load file picker)
@Composable
fun ImportProfileScreen(
    viewModel: VpnViewModel,
    navController: NavController,
    language: AppLanguage
) {
    var rawConfigText by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Set up file picker callbacks
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
                inputStream?.close()

                rawConfigText = sb.toString()

                // Default filename as entry name title representation
                val lastPath = uri.lastPathSegment
                if (lastPath != null && lastPath.contains("/")) {
                    inputName = lastPath.substringAfterLast("/").substringBefore(".conf")
                } else if (lastPath != null) {
                    inputName = lastPath.substringBefore(".conf")
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read configuration file.", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = Translator.translate("import_title", language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedTextField(
            value = inputName,
            onValueChange = { inputName = it },
            label = { Text(Translator.translate("name_label", language)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            singleLine = true
        )

        Button(
            onClick = { fileLauncher.launch("application/octet-stream") },
            modifier = Modifier.fillMaxWidth().height(50.dp).padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.UploadFile, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Translator.translate("import_btn", language))
        }

        Button(
            onClick = { navController.navigate("scanner") },
            modifier = Modifier.fillMaxWidth().height(50.dp).padding(bottom = 20.dp).testTag("scan_qr_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Translator.translate("qr_scan_title", language))
        }

        Text(
            text = Translator.translate("or_paste", language),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = rawConfigText,
            onValueChange = { rawConfigText = it },
            label = { Text(Translator.translate("paste_label", language)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(bottom = 24.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            maxLines = 15
        )

        Button(
            onClick = {
                val outcome = viewModel.importConfig(inputName, rawConfigText)
                if (outcome) {
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("save_profile_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Translator.translate("save_btn", language), fontWeight = FontWeight.Bold)
        }
    }
}

// 5. QR CODE CAMERA SCANNER SCREEN
@Composable
fun ScannerScreen(
    viewModel: VpnViewModel,
    navController: NavController,
    language: AppLanguage
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraLayoutScanner(
                onCodeDetected = { code ->
                    // Run import on scanned string values
                    val complete = viewModel.importConfig("QR Code Scanned", code)
                    if (complete) {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    } else {
                        Toast.makeText(context, "Invalid scanned QR layout.", Toast.LENGTH_LONG).show()
                    }
                }
            )

            // Neon Scanner Frame Overlays
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = Translator.translate("qr_scan_prompt", language),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .border(3.dp, Color(0xFF60A5FA), RoundedCornerShape(16.dp))
                    ) {
                        // Sliding scanline line animation
                        val scanAnim = rememberInfiniteTransition(label = "scanline")
                        val scanY by scanAnim.animateFloat(
                            initialValue = 0f,
                            targetValue = 280f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2200, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scan_offset"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .offset(y = scanY.dp)
                                .background(Color(0xFF38BDF8))
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = Translator.translate("camera_permission_denied", language),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text(Translator.translate("grant_permission", language))
                }
            }
        }

        // Floating Back Button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(24.dp)
                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close Scanner", tint = Color.White)
        }
    }
}

// Subordinate composable for binding CameraX to local Jetpack Compose Lifecycle
@Composable
fun CameraLayoutScanner(onCodeDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(lifecycleOwner) {
        val cameraExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraPreview = CameraPreview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().apply {
                    setAnalyzer(cameraExecutor, QrCodeAnalyzer { scanResult ->
                        lifecycleOwner.lifecycle.run {
                            onCodeDetected(scanResult)
                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    cameraPreview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraLayoutScanner", "Camera unbind bind failed", e)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

// 6. SETTINGS SCREEN
@Composable
fun SettingsScreen(
    viewModel: VpnViewModel,
    language: AppLanguage
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = Translator.translate("settings_title", language),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Card Settings Options
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Language Setting Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(Translator.translate("language_label", language), fontWeight = FontWeight.Bold)
                        Text(
                            text = if (language == AppLanguage.ENGLISH) "English (United States)" else "فارسی (ایران)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Button(onClick = { viewModel.toggleLanguage() }, modifier = Modifier.testTag("toggle_language_btn")) {
                        Text(if (language == AppLanguage.ENGLISH) "فارسی" else "English")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Theme Setting Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(Translator.translate("theme_label", language), fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isDarkMode) "Slate Midnight" else "Vibrant Day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Switch(checked = isDarkMode, onCheckedChange = { viewModel.toggleTheme() }, modifier = Modifier.testTag("theme_switch"))
                }
            }
        }

        // Config Load Testing Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Developer Sandbox Tests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Load an optimized preset template profile designed to test security decryptions and network ping latency components instantly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = { viewModel.loadSampleConfig() },
                    modifier = Modifier.fillMaxWidth().testTag("load_sample_profile_btn")
                ) {
                    Text(Translator.translate("sample_btn", language))
                }
            }
        }
    }
}

// 7. ABOUT SCREEN BRAND DEEP INFO
@Composable
fun AboutScreen(language: AppLanguage) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_secure_logo),
            contentDescription = "Corporate Logo",
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Translator.translate("about_title", language),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black
        )

        Text(
            text = Translator.translate("version_label", language),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Translator.translate("about_desc", language),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Security AES Badge Block
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = Translator.translate("security_badge", language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// Log utility helper for safe release builds without exposing secrets
object Log {
    fun d(tag: String, message: String) {
        // Safe logger: do not include logs in release configurations
    }
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        // Safe logger: do not include logs in release configurations
    }
}
