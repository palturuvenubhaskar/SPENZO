package com.spenzo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.spenzo.ui.components.IconBadge
import com.spenzo.ui.components.SpCard
import com.spenzo.ui.components.spFieldColors
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
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    Box(
                        Modifier
                            .padding(start = Space.sm)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(c.card)
                            .border(BorderStroke(1.dp, c.border), CircleShape)
                            .clickable { nav.popBackStack() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            "Back",
                            tint = c.textPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.background),
            )
        },
        containerColor = c.background,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Insets.gutter),
            verticalArrangement = Arrangement.spacedBy(Space.lg),
        ) {
            SettingsSectionLabel("Appearance")
            ThemeChooser(
                current = themeMode,
                onSelect = { SettingsStore.setTheme(context, it) },
            )

            SettingsSectionLabel("Currency")
            SpCard(contentPadding = PaddingValues(Space.xs)) {
                SettingsRow(
                    icon = Icons.Rounded.CurrencyExchange,
                    title = "Display currency",
                    subtitle = run {
                        val currency = SettingsStore.currencies.firstOrNull { it.code == currencyCode }
                        "${currency?.symbol ?: "$"} · ${currency?.name ?: "US Dollar"}"
                    },
                    iconTint = c.warn,
                    trailing = { Icon(Icons.Rounded.ChevronRight, null, tint = c.textTertiary) },
                    onClick = { showCurrencyPicker = true },
                )
            }

            SettingsSectionLabel("Data")
            SpCard(contentPadding = PaddingValues(Space.xs)) {
                SettingsRow(
                    icon = Icons.Rounded.Download,
                    title = if (exporting) "Preparing export…" else "Export transactions",
                    subtitle = "Share a CSV file with your data",
                    iconTint = c.primary,
                    trailing = {
                        if (exporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = c.primary,
                            )
                        } else {
                            Icon(Icons.Rounded.ChevronRight, null, tint = c.textTertiary)
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

            SettingsSectionLabel("About")
            SpCard(contentPadding = PaddingValues(Space.xs)) {
                SettingsInfoRow(Icons.Rounded.Info, "Version", BuildConfig.VERSION_NAME)
                SettingsDivider()
                SettingsInfoRow(Icons.Rounded.Code, "Built with", "Jetpack Compose")
                SettingsDivider()
                SettingsInfoRow(Icons.Rounded.Storage, "Storage", "On this device only")
            }

            Spacer(Modifier.height(Space.xxl))
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            currentCode = currencyCode,
            onDismiss = { showCurrencyPicker = false },
            onSelect = {
                SettingsStore.setCurrency(context, it)
                showCurrencyPicker = false
            },
        )
    }
}

@Composable
private fun ThemeChooser(current: Int, onSelect: (Int) -> Unit) {
    val c = SpTheme.colors
    val options = listOf(
        Triple(SettingsStore.THEME_SYSTEM, "System", Icons.Rounded.Settings),
        Triple(SettingsStore.THEME_LIGHT, "Light", Icons.Rounded.LightMode),
        Triple(SettingsStore.THEME_DARK, "Dark", Icons.Rounded.DarkMode),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(Space.sm), modifier = Modifier.fillMaxWidth()) {
        options.forEach { (mode, label, icon) ->
            val selected = current == mode
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Radius.md))
                    .background(if (selected) c.primarySoft else c.card)
                    .border(
                        BorderStroke(1.dp, if (selected) c.primary.copy(alpha = 0.45f) else c.border),
                        RoundedCornerShape(Radius.md),
                    )
                    .clickable { onSelect(mode) }
                    .padding(vertical = Space.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    icon,
                    null,
                    tint = if (selected) c.primary else c.textSecondary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.height(Space.sm))
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) c.primary else c.textSecondary,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun CurrencyPickerDialog(
    currentCode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val c = SpTheme.colors
    var query by remember { mutableStateOf("") }
    val list = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) SettingsStore.currencies
        else SettingsStore.currencies.filter {
            it.code.contains(q, ignoreCase = true) || it.name.contains(q, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.cardElevated,
        shape = RoundedCornerShape(Radius.xl),
        title = { Text("Select currency") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search", color = c.textTertiary) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null, tint = c.textTertiary) },
                    singleLine = true,
                    shape = RoundedCornerShape(Radius.md),
                    colors = spFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Space.sm))
                Column(
                    Modifier
                        .heightIn(max = 340.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    list.forEach { currency ->
                        val selected = currency.code == currentCode
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.sm))
                                .background(if (selected) c.primarySoft else Color.Transparent)
                                .clickable { onSelect(currency.code) }
                                .padding(horizontal = Space.md, vertical = Space.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                currency.symbol,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (selected) c.primary else c.textPrimary,
                                modifier = Modifier.width(40.dp),
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    currency.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = c.textPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    currency.code,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.textSecondary,
                                )
                            }
                            if (selected) {
                                Icon(Icons.Rounded.Check, "Selected", tint = c.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    if (list.isEmpty()) {
                        Text(
                            "No currency matches \"$query\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary,
                            modifier = Modifier.padding(Space.md),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = c.textSecondary) }
        },
    )
}

@Composable
private fun SettingsSectionLabel(text: String) {
    val c = SpTheme.colors
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = c.textSecondary,
        modifier = Modifier.padding(top = Space.sm, start = Space.xs),
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp),
        thickness = 1.dp,
        color = SpTheme.colors.border.copy(alpha = 0.7f),
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
            .background(if (selected) c.primarySoft else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = Space.md, vertical = Space.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon = icon, tint = iconTint, size = 38.dp, iconSize = 19.dp)
        Spacer(Modifier.width(Space.md))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = c.textPrimary,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
            }
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
        IconBadge(icon = icon, tint = c.textSecondary, size = 38.dp, iconSize = 19.dp)
        Spacer(Modifier.width(Space.md))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = c.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(value, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
    }
}
