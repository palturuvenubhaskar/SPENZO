package com.spenzo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
                    containerColor = Background,
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
                        composable("add_edit?txId={txId}") { back ->
                            AddEditTransactionScreen(
                                nav = nav,
                                txId = back.arguments?.getString("txId")?.toLongOrNull()
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
    Surface(color = Surface, tonalElevation = 8.dp) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val backStack by nav.currentBackStackEntryAsState()
            val current = backStack?.destination?.route
            items.forEach { item ->
                val selected = current == item.route
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) DarkCard else Color_Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            nav.navigate(item.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(item.icon, item.label,
                            tint = if (selected) Surface else TextSecondary,
                            modifier = Modifier.size(22.dp))
                        if (selected) {
                            Spacer(Modifier.width(8.dp))
                            Text(item.label, color = Surface, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

private val Color_Transparent = androidx.compose.ui.graphics.Color.Transparent
