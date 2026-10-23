package com.pascm.inspseg.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pascm.inspseg.ui.screens.HomeScreen
import com.pascm.inspseg.ui.screens.IncidencesScreen
import com.pascm.inspseg.ui.screens.InspectionScreen
import com.pascm.inspseg.ui.screens.LoginScreen
import com.pascm.inspseg.ui.screens.NormativesScreen
import com.pascm.inspseg.ui.theme.OutlineSubtle
import com.pascm.inspseg.ui.theme.SurfaceBase
import com.pascm.inspseg.ui.theme.TextPrimary
import com.pascm.inspseg.ui.theme.TextSecondary
import com.pascm.inspseg.ui.theme.Void
import com.pascm.inspseg.ui.viewmodel.AppViewModelProvider
import com.pascm.inspseg.ui.viewmodel.AuthViewModel

/**
 * Destinos de primer nivel. Se usa barra inferior, que es el patron esperado de
 * navegacion primaria en movil, en lugar de un menu lateral oculto.
 */
enum class TopLevel(val route: String, val label: String, val icon: ImageVector) {
    RECORRIDOS("recorridos", "Recorridos", Icons.AutoMirrored.Filled.Assignment),
    HALLAZGOS("hallazgos", "Hallazgos", Icons.Filled.ReportProblem),
    NORMAS("normas", "Normas", Icons.Filled.Gavel)
}

private const val INSPECTION_ROUTE = "inspeccion/{zoneId}/{inspector}"
private const val LOGIN_ROUTE = "acceso"

@Composable
fun InspSegNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val inspector by authViewModel.inspector.collectAsState()
    var offline by rememberSaveable { mutableStateOf(false) }
    // Con sesion de Firebase (o modo sin conexion elegido) se entra al tablero;
    // al cerrar sesion se regresa a la pantalla de acceso.
    val signedIn = inspector != null || offline
    LaunchedEffect(signedIn) {
        val target = if (signedIn) TopLevel.RECORRIDOS.route else LOGIN_ROUTE
        if (navController.currentDestination?.route != target) {
            navController.navigate(target) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    LaunchedEffect(inspector) { if (inspector != null) offline = false }
    // El destino inicial se fija una sola vez; los cambios de sesion navegan arriba.
    val startRoute = remember { if (signedIn) TopLevel.RECORRIDOS.route else LOGIN_ROUTE }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    // La captura ocupa la pantalla completa: sin barra inferior que invite a salir a media inspeccion.
    val showBottomBar = TopLevel.entries.any { level ->
        currentDestination?.hierarchy?.any { it.route == level.route } == true
    }

    Scaffold(
        containerColor = Void,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = SurfaceBase, tonalElevation = 0.dp) {
                    TopLevel.entries.forEach { level ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == level.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(level.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(level.icon, contentDescription = null) },
                            label = { Text(level.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextPrimary,
                                selectedTextColor = TextPrimary,
                                indicatorColor = OutlineSubtle,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(padding)
        ) {
            composable(LOGIN_ROUTE) {
                LoginScreen(viewModel = authViewModel, onContinueOffline = { offline = true })
            }
            composable(TopLevel.RECORRIDOS.route) {
                HomeScreen(
                    onStartInspection = { zoneId, inspector ->
                        navController.navigate("inspeccion/$zoneId/${Uri.encode(inspector)}")
                    },
                    onOpenIncidences = {
                        navController.navigate(TopLevel.HALLAZGOS.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(TopLevel.HALLAZGOS.route) { IncidencesScreen() }
            composable(TopLevel.NORMAS.route) { NormativesScreen() }
            composable(
                route = INSPECTION_ROUTE,
                arguments = listOf(
                    navArgument("zoneId") { type = NavType.LongType },
                    navArgument("inspector") { type = NavType.StringType }
                )
            ) { entry ->
                InspectionScreen(
                    zoneId = entry.arguments?.getLong("zoneId") ?: 0L,
                    inspector = Uri.decode(entry.arguments?.getString("inspector").orEmpty()),
                    onFinished = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
