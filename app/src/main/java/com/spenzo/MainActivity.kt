package com.spenzo

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.spenzo.data.BudgetStore
import com.spenzo.data.SettingsStore
import com.spenzo.ui.*
import com.spenzo.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SettingsStore.init(this)
        BudgetStore.init(this)
        enableEdgeToEdge()
        setContent {
            val themeMode by SettingsStore.themeMode.collectAsState()
            val currencySymbol by SettingsStore.currencySymbol.collectAsState()
            val darkTheme = when (themeMode) {
                SettingsStore.THEME_LIGHT -> false
                SettingsStore.THEME_DARK -> true
                else -> isSystemInDarkTheme()
            }

            // System bar icon colors must follow the in-app theme override, not
            // just the system setting — otherwise light-mode users on a dark
            // system get invisible (light) status bar icons.
            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT) { darkTheme },
                )
            }

            SpenzoTheme(darkTheme = darkTheme) {
                // One ViewModel shared by every destination: month selection,
                // DB flows and budget state stay in sync between tabs.
                val vm: TransactionViewModel = viewModel()
                val nav = rememberNavController()

                CompositionLocalProvider(LocalCurrencySymbol provides currencySymbol) {
                    Surface(modifier = Modifier.fillMaxSize(), color = SpTheme.colors.background) {
                        val backStack by nav.currentBackStackEntryAsState()
                        val currentRoute = backStack?.destination?.route
                        val isMainTab = currentRoute in mainTabs

                        Scaffold(
                            containerColor = SpTheme.colors.background,
                            bottomBar = {
                                AnimatedVisibility(
                                    visible = isMainTab,
                                    enter = slideInVertically(
                                        initialOffsetY = { it },
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    ) + fadeIn(),
                                    exit = slideOutVertically(
                                        targetOffsetY = { it },
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    ) + fadeOut()
                                ) {
                                    SpenzoBottomBar(nav)
                                }
                            },
                            floatingActionButton = {
                                AnimatedVisibility(
                                    visible = isMainTab,
                                    enter = scaleIn() + fadeIn(),
                                    exit = scaleOut() + fadeOut()
                                ) {
                                    FloatingActionButton(
                                        onClick = { nav.navigate("add_edit?type=EXPENSE") },
                                        containerColor = SpTheme.colors.primary,
                                        contentColor = SpTheme.colors.onPrimary,
                                        shape = CircleShape,
                                    ) {
                                        Icon(Icons.Rounded.Add, "Add transaction")
                                    }
                                }
                            }
                        ) { padding ->
                            NavHost(
                                navController = nav,
                                startDestination = "home",
                                modifier = Modifier.padding(padding),
                                // Screens own their insets: tabs pad the status bar,
                                // screens with a TopAppBar let the app bar handle it.
                            ) {
                                composable("home") { HomeScreen(nav, vm) }
                                composable("graphs") { GraphsScreen(vm) }
                                composable("analysis") { AnalysisScreen(vm) }
                                composable("history") { HistoryScreen(nav, vm) }
                                composable("settings") { SettingsScreen(nav, vm) }
                                composable(
                                    route = "add_edit?txId={txId}&type={type}",
                                    arguments = listOf(
                                        navArgument("txId") {
                                            type = NavType.StringType
                                            nullable = true
                                            defaultValue = null
                                        },
                                        navArgument("type") {
                                            type = NavType.StringType
                                            nullable = true
                                            defaultValue = null
                                        }
                                    )
                                ) { back ->
                                    AddEditTransactionScreen(
                                        nav = nav,
                                        vm = vm,
                                        txId = back.arguments?.getString("txId")?.toLongOrNull(),
                                        initialType = back.arguments?.getString("type"),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val mainTabs = listOf("home", "graphs", "analysis", "history")

private data class NavItem(
    val route: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val label: String,
)

@Composable
private fun SpenzoBottomBar(nav: NavHostController) {
    val c = SpTheme.colors
    val items = listOf(
        NavItem("home", Icons.Rounded.Home, Icons.Outlined.Home, "Home"),
        NavItem("graphs", Icons.Rounded.BarChart, Icons.Outlined.BarChart, "Graphs"),
        NavItem("analysis", Icons.Rounded.AutoGraph, Icons.Outlined.AutoGraph, "Analysis"),
        NavItem("history", Icons.Rounded.History, Icons.Outlined.History, "History"),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = c.card,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, c.border),
            modifier = Modifier.fillMaxWidth().height(64.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val backStack by nav.currentBackStackEntryAsState()
                val current = backStack?.destination?.route

                items.forEach { item ->
                    val selected = current == item.route

                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.08f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                        label = "scale_${item.route}",
                    )
                    val pillBg by animateColorAsState(
                        targetValue = if (selected) c.cardElevated else Color.Transparent,
                        animationSpec = tween(200),
                        label = "pill_${item.route}",
                    )
                    val tint by animateColorAsState(
                        targetValue = if (selected) c.primary else c.textSecondary,
                        animationSpec = tween(200),
                        label = "tint_${item.route}",
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(22.dp))
                            .background(pillBg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                if (current != item.route) {
                                    nav.navigate(item.route) {
                                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = if (selected) item.activeIcon else item.inactiveIcon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier
                                .size(21.dp)
                                .graphicsLayer(scaleX = scale, scaleY = scale),
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            color = tint,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
