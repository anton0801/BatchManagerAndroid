package com.managerbatching.batchmanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.managerbatching.batchmanager.ui.batch.*
import com.managerbatching.batchmanager.ui.dashboard.DashboardScreen
import com.managerbatching.batchmanager.ui.history.BatchHistoryScreen
import com.managerbatching.batchmanager.ui.incubation.IncubationMonitorScreen
import com.managerbatching.batchmanager.ui.reports.BatchReportsScreen
import com.managerbatching.batchmanager.viewmodel.BatchViewModel

sealed class Route(val path: String) {
    object Dashboard : Route("dashboard")
    object CreateBatch : Route("create_batch")
    object BatchDetails : Route("batch_details/{batchId}") {
        fun go(id: String) = "batch_details/$id"
    }
    object BatchEvent : Route("batch_event/{batchId}") {
        fun go(id: String) = "batch_event/$id"
    }
    object IncubationMonitor : Route("incubation_monitor/{batchId}") {
        fun go(id: String) = "incubation_monitor/$id"
    }
    object BatchReports : Route("batch_reports/{batchId}") {
        fun go(id: String) = "batch_reports/$id"
    }
    object BatchHistory : Route("batch_history")
}

@Composable
fun AppNavGraph(navController: NavHostController, viewModel: BatchViewModel) {
    NavHost(navController, startDestination = Route.Dashboard.path) {
        composable(Route.Dashboard.path) {
            DashboardScreen(
                viewModel = viewModel,
                onCreateBatch = { navController.navigate(Route.CreateBatch.path) },
                onBatchClick = { navController.navigate(Route.BatchDetails.go(it)) }
            )
        }
        composable(Route.CreateBatch.path) {
            BatchCreatorScreen(
                viewModel = viewModel,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            Route.BatchDetails.path,
            arguments = listOf(navArgument("batchId") { type = NavType.StringType })
        ) {
            val id = it.arguments?.getString("batchId") ?: return@composable
            BatchDetailsScreen(
                batchId = id,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddEvent = { bId -> navController.navigate(Route.BatchEvent.go(bId)) },
                onIncubationMonitor = { bId -> navController.navigate(Route.IncubationMonitor.go(bId)) }
            )
        }
        composable(
            Route.BatchEvent.path,
            arguments = listOf(navArgument("batchId") { type = NavType.StringType })
        ) {
            val id = it.arguments?.getString("batchId") ?: return@composable
            BatchEventScreen(batchId = id, viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(
            Route.IncubationMonitor.path,
            arguments = listOf(navArgument("batchId") { type = NavType.StringType })
        ) {
            val id = it.arguments?.getString("batchId") ?: return@composable
            IncubationMonitorScreen(batchId = id, viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(
            Route.BatchReports.path,
            arguments = listOf(navArgument("batchId") { type = NavType.StringType })
        ) {
            val id = it.arguments?.getString("batchId") ?: return@composable
            BatchReportsScreen(batchId = id, viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Route.BatchHistory.path) {
            BatchHistoryScreen(
                viewModel = viewModel,
                onCreateBatch = { navController.navigate(Route.CreateBatch.path) },
                onBatchClick = { navController.navigate(Route.BatchDetails.go(it)) }
            )
        }
    }
}