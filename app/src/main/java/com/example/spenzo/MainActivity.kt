package com.example.spenzo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.spenzo.presentation.addexpense.AddExpenseSheet
import com.example.spenzo.presentation.analysis.AnalysisScreen
import com.example.spenzo.presentation.expenses.ExpensesScreen
import com.example.spenzo.presentation.graphs.GraphsScreen
import com.example.spenzo.presentation.theme.CardDark
import com.example.spenzo.presentation.theme.SpenzoTheme
import com.example.spenzo.presentation.theme.TextOnDark
import com.example.spenzo.presentation.theme.TextPrimary
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Expenses : Screen("expenses", "Expenses", Icons.Outlined.Receipt)
    object Graphs : Screen("graphs", "Graphs", Icons.Outlined.BarChart)
    object Analysis : Screen("analysis", "Analysis", Icons.Outlined.PieChart)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpenzoTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                var isSheetOpen by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Expenses.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Screen.Expenses.route) {
                            ExpensesScreen(
                                onAddExpenseClick = { isSheetOpen = true },
                                isSheetOpen = isSheetOpen
                            )
                        }
                        composable(Screen.Graphs.route) {
                            GraphsScreen()
                        }
                        composable(Screen.Analysis.route) {
                            AnalysisScreen()
                        }
                    }

                    // Floating Glass Navigation Bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.White.copy(alpha = 0.85f))
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val items = listOf(Screen.Expenses, Screen.Graphs, Screen.Analysis)
                            items.forEach { screen ->
                                val selected = currentRoute == screen.route
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(if (selected) CardDark else Color.Transparent)
                                        .clickable {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        tint = if (selected) TextOnDark else TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    if (selected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = screen.title,
                                            color = TextOnDark,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Sheet
                    if (isSheetOpen) {
                        // Frosted Backdrop
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.15f))
                                .clickable { isSheetOpen = false }
                        )
                        
                        ModalBottomSheet(
                            onDismissRequest = { isSheetOpen = false },
                            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                            containerColor = Color.White,
                            dragHandle = { BottomSheetDefaults.DragHandle() },
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        ) {
                            AddExpenseSheet(onDismiss = { isSheetOpen = false })
                        }
                    }
                }
            }
        }
    }
}
