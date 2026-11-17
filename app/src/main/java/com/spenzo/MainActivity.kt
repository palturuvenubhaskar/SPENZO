package com.spenzo

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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

                        Box(Modifier.fillMaxSize()) {
                            NavHost(
                                navController = nav,
                                startDestination = "home",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 0.dp),
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter),
                            ) {
                                SpenzoBottomBar(nav, currentRoute, isMainTab)
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
private fun SpenzoBottomBar(
    nav: NavHostController,
    current: String?,
    isMainTab: Boolean,
) {
    val c = SpTheme.colors
    val haptics = LocalHapticFeedback.current
    // Modern, distinct icons for each tab
    val items = listOf(
        NavItem("home", Icons.Rounded.Home, Icons.Outlined.Home, "Home"),
        NavItem("graphs", Icons.Rounded.ShowChart, Icons.Outlined.ShowChart, "Graphs"),
        NavItem("analysis", Icons.Rounded.Analytics, Icons.Outlined.Analytics, "Analysis"),
        NavItem("history", Icons.Rounded.ReceiptLong, Icons.Outlined.ReceiptLong, "History"),
    )

    AnimatedVisibility(
        visible = isMainTab,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Space.lg, vertical = Space.sm),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = c.card,
                border = BorderStroke(1.dp, c.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .then(
                        if (!c.isDark) Modifier.shadow(14.dp, RoundedCornerShape(28.dp), clip = false)
                        else Modifier
                    ),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = Space.sm, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NavTab(items[0], current, nav, haptics)
                    NavTab(items[1], current, nav, haptics)
                    AddButton { nav.navigate("add_edit?type=EXPENSE") }
                    NavTab(items[2], current, nav, haptics)
                    NavTab(items[3], current, nav, haptics)
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavTab(
    item: NavItem,
    current: String?,
    nav: NavHostController,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
) {
    val c = SpTheme.colors
    val selected = current == item.route
    val bg by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) c.primarySoft else Color.Transparent,
        animationSpec = androidx.compose.animation.core.tween(180),
        label = "tabBg${item.route}",
    )
    val tint by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) c.primary else c.textSecondary,
        animationSpec = androidx.compose.animation.core.tween(180),
        label = "tabTint${item.route}",
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.0f else 0.94f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tabScale${item.route}",
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                if (!selected) {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    nav.navigate(item.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) item.activeIcon else item.inactiveIcon,
            contentDescription = item.label,
            tint = tint,
            modifier = Modifier.size((21 * scale).dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.label,
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}

@Composable
private fun RowScope.AddButton(onClick: () -> Unit) {
    val c = SpTheme.colors
    val haptics = LocalHapticFeedback.current
    Box(
        Modifier.weight(1f).fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(46.dp)
                .then(
                    if (!c.isDark) Modifier.shadow(8.dp, CircleShape, clip = false) else Modifier
                )
                .clip(CircleShape)
                .background(c.primary)
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = "Add transaction",
                tint = c.onPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
