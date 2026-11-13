package com.spenzo.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Per-month, per-category budgets.
 *
 * The previous version stored one global number per category (default 500 for
 * every category, invented), while the screen header claimed to show a monthly
 * budget. Budgets are now keyed by `yyyy-MM` and are empty until the user sets
 * one, and the value is exposed as a StateFlow so the UI recomposes.
 */
object BudgetStore {
    private const val PREF = "spenzo_budgets_v2"
    private const val LEGACY_PREF = "spenzo_budgets"
    private const val SEPARATOR = "|"

    private var prefs: SharedPreferences? = null
    private var initialized = false

    private val _budgets = MutableStateFlow<Map<String, Map<String, Double>>>(emptyMap())
    val budgets: StateFlow<Map<String, Map<String, Double>>> = _budgets

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        val app = context.applicationContext
        prefs = app.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val loaded = mutableMapOf<String, MutableMap<String, Double>>()

        prefs?.all?.forEach { (key, value) ->
            val parts = key.split(SEPARATOR)
            if (parts.size == 2 && value is Float && value > 0f) {
                loaded.getOrPut(parts[0]) { mutableMapOf() }[parts[1]] = value.toDouble()
            }
        }

        // One-time migration of budgets saved by the old global store.
        val legacy = app.getSharedPreferences(LEGACY_PREF, Context.MODE_PRIVATE)
        if (loaded.isEmpty() && legacy.all.isNotEmpty()) {
            val current = Dates.currentMonthKey()
            legacy.all.forEach { (category, value) ->
                if (value is Float && value > 0f) {
                    loaded.getOrPut(current) { mutableMapOf() }[category] = value.toDouble()
                    prefs?.edit()?.putFloat(current + SEPARATOR + category, value)?.apply()
                }
            }
        }

        _budgets.value = loaded
    }

    fun forMonth(monthKey: String): Map<String, Double> = _budgets.value[monthKey].orEmpty()

    /** Pass null (or 0) to remove the budget. */
    fun setBudget(monthKey: String, category: String, value: Double?) {
        val key = monthKey + SEPARATOR + category
        val clean = value?.takeIf { it > 0 }
        val updated = _budgets.value.toMutableMap()
        val month = updated[monthKey].orEmpty().toMutableMap()
        if (clean == null) {
            month.remove(category)
            prefs?.edit()?.remove(key)?.apply()
        } else {
            month[category] = clean
            prefs?.edit()?.putFloat(key, clean.toFloat())?.apply()
        }
        if (month.isEmpty()) updated.remove(monthKey) else updated[monthKey] = month
        _budgets.value = updated
    }
}
