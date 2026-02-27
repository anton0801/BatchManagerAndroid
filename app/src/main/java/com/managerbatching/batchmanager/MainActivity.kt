package com.managerbatching.batchmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.managerbatching.batchmanager.data.BatchRepository
import com.managerbatching.batchmanager.data.PreferencesManager
import com.managerbatching.batchmanager.ui.navigation.AppNavGraph
import com.managerbatching.batchmanager.ui.navigation.Route
import com.managerbatching.batchmanager.ui.onboarding.OnboardingScreen
import com.managerbatching.batchmanager.ui.splash.SplashContent
import com.managerbatching.batchmanager.ui.theme.*
import com.managerbatching.batchmanager.viewmodel.BatchViewModel

class MainActivity : ComponentActivity() {

    private val prefsManager by lazy { PreferencesManager(this) }
    private val repository by lazy { BatchRepository(prefsManager) }
    private val viewModel: BatchViewModel by viewModels {
        BatchViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            BatchManagerTheme {
                AppRoot(viewModel, prefsManager)
            }
        }
    }
}

enum class AppState { SPLASH, ONBOARDING, MAIN }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(viewModel: BatchViewModel, prefs: PreferencesManager) {
    var appState by remember {
        mutableStateOf(if (prefs.isOnboardingDone()) AppState.SPLASH else AppState.SPLASH)
    }
    var onboardingDone by remember { mutableStateOf(prefs.isOnboardingDone()) }

    Crossfade(targetState = appState, label = "appState") { state ->
        when (state) {
            AppState.SPLASH -> SplashContent(onFinished = {
                appState = if (onboardingDone) AppState.MAIN else AppState.ONBOARDING
            })
            AppState.ONBOARDING -> OnboardingScreen(onFinish = {
                prefs.setOnboardingDone()
                onboardingDone = true
                appState = AppState.MAIN
            })
            AppState.MAIN -> MainApp(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: BatchViewModel) {
    val navController = rememberNavController()
    val currentRoute by navController.currentBackStackEntryFlow.collectAsState(initial = navController.currentBackStackEntry)

    val showBottomBar = currentRoute?.destination?.route in listOf(
        Route.Dashboard.path, Route.BatchHistory.path
    )

    Scaffold(
        containerColor = BackgroundYellow,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(containerColor = CreamPanel, tonalElevation = 0.dp) {
                    NavigationBarItem(
                        selected = currentRoute?.destination?.route == Route.Dashboard.path,
                        onClick = { navController.navigate(Route.Dashboard.path) { launchSingleTop = true } },
                        icon = { Text("🏠", fontSize = 22.sp) },
                        label = { Text("Dashboard", fontFamily = NunitoFont, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentOrange,
                            selectedTextColor = AccentOrange,
                            indicatorColor = PrimaryYellow.copy(0.3f)
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(Route.CreateBatch.path) },
                        icon = { Text("➕", fontSize = 22.sp) },
                        label = { Text("New", fontFamily = NunitoFont, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentOrange,
                            indicatorColor = PrimaryYellow.copy(0.3f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute?.destination?.route == Route.BatchHistory.path,
                        onClick = { navController.navigate(Route.BatchHistory.path) { launchSingleTop = true } },
                        icon = { Text("📜", fontSize = 22.sp) },
                        label = { Text("History", fontFamily = NunitoFont, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentOrange,
                            selectedTextColor = AccentOrange,
                            indicatorColor = PrimaryYellow.copy(0.3f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AppNavGraph(navController = navController, viewModel = viewModel)
        }
    }
}