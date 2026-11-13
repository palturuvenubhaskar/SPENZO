package com.spenzo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.spenzo.ui.*
import com.spenzo.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpenzoTheme {
                val nav = rememberNavController()
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = { SpenzoBottomBar(nav) }
                ) { padding ->
                    NavHost(
                        navController = nav,
                        startDestination = "home",
                        modifier = Modifier.padding(padding)
                    ) {
                        composable("home") { HomeScreen(nav) }
                        composable("graphs") { GraphsScreen() }
                        composable("analysis") { AnalysisScreen() }
                        composable("history") { HistoryScreen(nav) }
                        composable(
                            route = "add_edit?txId={txId}",
                            arguments = listOf(navArgument("txId") { type = NavType.LongType })
                        ) { back ->
                            AddEditTransactionScreen(
                                nav = nav,
                                txId = back.getLong("txId")
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class NavItem(val route: String, val icon: ImageVector, val label: String)

@Composable
private fun SpenzoBottomBar(nav: androidx.navigation.NavHostController) {
    val items = listOf(
        NavItem("home", Icons.Outlined.Home, "Home"),
        NavItem("graphs", Icons.Outlined.PieChart, "Graphs"),
        NavItem("analysis", Icons.Outlined.Insights, "Analysis"),
        NavItem("history", Icons.Outlined.ReceiptLong, "History")
    )

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = NavigationBarDefaults.TonalElevation
    ) {
        val backStack by nav.currentBackStackEntryAsState()
        val current = backStack?.destination?.route
        items.forEach { item ->
            val selected = current == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (selected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    nav.navigate(item.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                alwaysShowLabel = false,
                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                colors = NavigationBarItemDefaults.colors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                )
            )
        }
    }
}