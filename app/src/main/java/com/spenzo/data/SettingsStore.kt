package com.spenzo.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Lightweight settings store backed by SharedPreferences.
 * Exposes StateFlows so Compose UI recomposes on changes.
 */
object SettingsStore {
    private const val PREF = "spenzo_settings"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_CURRENCY = "currency"

    // Theme modes
    const val THEME_SYSTEM = 0
    const val THEME_LIGHT  = 1
    const val THEME_DARK   = 2

    // Supported currencies
    data class Currency(val code: String, val symbol: String, val name: String)
    val currencies = listOf(
        Currency("USD", "$",  "US Dollar"),
        Currency("EUR", "€",  "Euro"),
        Currency("GBP", "£",  "British Pound"),
        Currency("INR", "₹",  "Indian Rupee"),
        Currency("JPY", "¥",  "Japanese Yen"),
        Currency("KRW", "₩",  "Korean Won"),
        Currency("CNY", "¥",  "Chinese Yuan"),
        Currency("AUD", "A$", "Australian Dollar"),
        Currency("CAD", "C$", "Canadian Dollar"),
        Currency("RUB", "₽",  "Russian Ruble"),
    )

    // StateFlows for reactive UI updates
    private val _themeMode = MutableStateFlow(THEME_SYSTEM)
    val themeMode: StateFlow<Int> = _themeMode

    private val _currencySymbol = MutableStateFlow("$")
    val currencySymbol: StateFlow<String> = _currencySymbol

    private val _currencyCode = MutableStateFlow("USD")
    val currencyCode: StateFlow<String> = _currencyCode

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        _themeMode.value = prefs.getInt(KEY_THEME, THEME_SYSTEM)
        val code = prefs.getString(KEY_CURRENCY, "USD") ?: "USD"
        _currencyCode.value = code
        _currencySymbol.value = currencies.firstOrNull { it.code == code }?.symbol ?: "$"
    }

    fun setTheme(context: Context, mode: Int) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putInt(KEY_THEME, mode).apply()
        _themeMode.value = mode
    }

    fun setCurrency(context: Context, code: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY_CURRENCY, code).apply()
        _currencyCode.value = code
        _currencySymbol.value = currencies.firstOrNull { it.code == code }?.symbol ?: "$"
    }
}
