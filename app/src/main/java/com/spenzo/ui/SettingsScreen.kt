package com.spenzo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.spenzo.BuildConfig
import com.spenzo.TransactionViewModel
import com.spenzo.data.CsvExporter
import com.spenzo.data.SettingsStore
import com.spenzo.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavHostController, vm: TransactionViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val c = SpTheme.colors
    val themeMode by SettingsStore.themeMode.collectAsState()
    val currencyCode by SettingsStore.currencyCode.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var showCurrencyPicker by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.background,
                    titleContentColor = c.textPrimary,
                    navigationIconContentColor = c.textPrimary,
                ),
            )
        },
        containerColor = c.background,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize().padding(horizontal = Space.xl),
            verticalArrangement = Arrangement.spacedBy(Space.md),
            contentPadding = PaddingValues(top = Space.sm, bottom = Space.xxl),
        ) {
            item { SettingsSectionLabel("Appearance") }
            item {
                SettingsGroup {
                    val options = listOf(
                        Triple(SettingsStore.THEME_SYSTEM, "System default", Icons.Rounded.Settings),
                        Triple(SettingsStore.THEME_LIGHT, "Light", Icons.Rounded.LightMode),
                        Triple(SettingsStore.THEME_DARK, "Dark", Icons.Rounded.DarkMode),
                    )
                    options.forEachIndexed { index, (mode, label, icon) ->
                        val selected = themeMode == mode
                        SettingsRow(
                            icon = icon,
                            title = label,
                            selected = selected,
                            iconTint = if (selected) c.income else c.textSecondary,
                            onClick = { SettingsStore.setTheme(context, mode) },
                        )
                        if (index < options.lastIndex) SettingsDivider()
                    }
                }
            }

            item { SettingsSectionLabel("Currency") }
            item {
                val currency = SettingsStore.currencies.firstOrNull { it.code == currencyCode }
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Rounded.CurrencyExchange,
                        title = "Display currency",
                        subtitle = "${currency?.symbol ?: "$"} · ${currency?.name ?: "US Dollar"}",
                        iconTint = c.warn,
                        trailing = {
                            Icon(Icons.Rounded.ChevronRight, null, tint = c.textSecondary)
                        },
                        onClick = { showCurrencyPicker = true },
                    )
                }
            }

            item { SettingsSectionLabel("Data") }
            item {
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Rounded.Download,
                        title = if (exporting) "Preparing export…" else "Export transactions",
                        subtitle = "Share a CSV file with your data",
                        iconTint = c.income,
                        trailing = {
                            if (exporting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Rounded.ChevronRight, null, tint = c.textSecondary)
                            }
                        },
                        onClick = {
                            if (!exporting) {
                                exporting = true
                                scope.launch {
                                    val rows = vm.snapshot()
                                    if (rows.isEmpty()) {
                                        exporting = false
                                        snackbar.showSnackbar("Nothing to export yet")
                                        return@launch
                                    }
                                    val file = withContext(Dispatchers.IO) {
                                        CsvExporter.write(rows, File(context.cacheDir, "exports"))
                                    }
                                    exporting = false
                                    runCatching { CsvExporter.share(context, file) }
                                        .onFailure { snackbar.showSnackbar("No app available to share the file") }
                                }
                            }
                        },
                    )
                }
            }

            item { SettingsSectionLabel("About") }
            item {
                SettingsGroup {
                    SettingsInfoRow(Icons.Rounded.Info, "Version", BuildConfig.VERSION_NAME)
                    SettingsDivider()
                    SettingsInfoRow(Icons.Rounded.Code, "Built with", "Jetpack Compose")
                    SettingsDivider()
                    SettingsInfoRow(Icons.Rounded.Storage, "Storage", "On this device only")
                }
            }
        }
    }

    if (showCurrencyPicker) {
        AlertDialog(
            onDismissRequest = { showCurrencyPicker = false },
            containerColor = c.cardElevated,
            title = { Text("Select currency") },
            text = {
                Column(
                    Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    SettingsStore.currencies.forEach { currency ->
                        val selected = currency.code == currencyCode
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.sm))
                                .background(if (selected) c.income.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable {
                                    SettingsStore.setCurrency(context, currency.code)
                                    showCurrencyPicker = false
                                }
                                .padding(horizontal = Space.md, vertical = Space.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                currency.symbol,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = c.textPrimary,
                                modifier = Modifier.width(36.dp),
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    currency.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = c.textPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(currency.code, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
                            }
                            if (selected) Icon(Icons.Rounded.Check, "Selected", tint = c.income)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyPicker = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    val c = SpTheme.colors
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = c.textSecondary,
        modifier = Modifier.padding(top = Space.sm),
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    val c = SpTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .background(c.card)
            .border(BorderStroke(1.dp, c.border), RoundedCornerShape(Radius.lg))
            .padding(Space.xs),
        content = content,
    )
}

@Composable
private fun SettingsDivider() {
    val c = SpTheme.colors
    HorizontalDivider(
        modifier = Modifier.padding(start = 62.dp),
        thickness = 1.dp,
        color = c.border.copy(alpha = 0.6f),
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    selected: Boolean = false,
    iconTint: Color = SpTheme.colors.textSecondary,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    val c = SpTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(if (selected) c.cardElevated else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = Space.md, vertical = Space.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).clip(CircleShape)
                .background(if (selected) c.income.copy(alpha = 0.15f) else c.iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, title, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(Space.md))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textPrimary,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
            }
        }
        if (selected) {
            Icon(Icons.Rounded.Check, "Selected", tint = c.income, modifier = Modifier.size(20.dp))
        }
        trailing?.invoke()
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, label: String, value: String) {
    val c = SpTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = Space.md, vertical = Space.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).clip(CircleShape).background(c.iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, label, tint = c.textSecondary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(Space.md))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
    }
}
