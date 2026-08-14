package com.example.spenzo.presentation.graphs

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spenzo.presentation.components.BarChart
import com.example.spenzo.presentation.components.GlassCard
import com.example.spenzo.presentation.components.getCategoryColor
import com.example.spenzo.presentation.components.getCategoryIcon
import com.example.spenzo.presentation.theme.*
import java.text.NumberFormat
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GraphsScreen(
    viewModel: GraphsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Scaffold(
        containerColor = BackgroundBase
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Graphs", style = Typography.headlineMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            // Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x14000000), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (state.period == TimePeriod.MONTHLY) CardDark else Color.Transparent)
                        .clickable { viewModel.onEvent(GraphsEvent.TogglePeriod(TimePeriod.MONTHLY)) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Monthly", color = if (state.period == TimePeriod.MONTHLY) TextOnDark else TextPrimary)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (state.period == TimePeriod.YEARLY) CardDark else Color.Transparent)
                        .clickable { viewModel.onEvent(GraphsEvent.TogglePeriod(TimePeriod.YEARLY)) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Yearly", color = if (state.period == TimePeriod.YEARLY) TextOnDark else TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Month/Year Navigator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = if (state.period == TimePeriod.MONTHLY) {
                        Pair(state.currentMonth, state.currentYear)
                    } else {
                        Pair(null, state.currentYear)
                    },
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "PeriodNav"
                ) { (month, year) ->
                    Column {
                        if (month != null) {
                            val monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())
                            Text(text = monthName, style = Typography.headlineMedium, color = TextPrimary)
                            Text(text = year.toString(), style = Typography.titleLarge, color = TextSecondary)
                        } else {
                            Text(text = year.toString(), style = Typography.headlineMedium, color = TextPrimary)
                        }
                    }
                }

                Row {
                    IconButton(onClick = { viewModel.onEvent(GraphsEvent.PreviousPeriod) }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous")
                    }
                    IconButton(onClick = { viewModel.onEvent(GraphsEvent.NextPeriod) }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    // Bar Chart
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            val chartTitle = if (state.period == TimePeriod.MONTHLY) "Daily Spending" else "Monthly Spending"
                            Text(chartTitle, style = Typography.bodyLarge, color = TextSecondary)
                            Text(
                                "Total: ${formatter.format(state.totalSpent)}",
                                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            BarChart(data = state.barData)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Text("Category-wise Spending", style = Typography.titleLarge, color = TextPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(state.categorySummaries) { summary ->
                    val color = getCategoryColor(summary.category)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(getCategoryIcon(summary.category), contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(summary.category.displayName, style = Typography.bodyLarge, color = TextPrimary)
                                Text(formatter.format(summary.totalAmount), style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Progress bar
                            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0x14000000))) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(summary.percentage)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
