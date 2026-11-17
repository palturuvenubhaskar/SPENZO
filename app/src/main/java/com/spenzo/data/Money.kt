package com.spenzo.data

import kotlin.math.abs
import kotlin.math.roundToLong
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * All money formatting and rounding happens here.
 *
 * Amounts are still persisted as Double (changing the column would need a Room
 * migration), but every sum/rounding goes through integer cents so numbers
 * cannot drift, and the sign is always rendered in front of the symbol
 * (`−$12.00`, never `$−12.00`).
 *
 * The sign is U+2212 MINUS, not the hyphen on the keyboard: the hyphen sits
 * too high and too short next to tabular figures and reads like a dash.
 */
object Money {
    /** U+2212 MINUS SIGN — the typographic minus, not "-". */
    private const val MINUS = "\u2212"

    fun cents(value: Double): Long = (value * 100).roundToLong()

    fun fromCents(cents: Long): Double = cents / 100.0

    /** Sums a list of Double amounts without accumulating float error. */
    fun sum(values: Iterable<Double>): Double = fromCents(values.sumOf { cents(it) })

    /** `$1,234.56`, `−$12.00`, `₹0.00` */
    fun format(value: Double, symbol: String): String {
        val c = cents(value)
        val sign = if (c < 0) MINUS else ""
        return sign + symbol + formatter.get()!!.format(abs(fromCents(c)))
    }

    /** `+$1,234.56` / `−$12.00` — for transaction rows. */
    fun signed(value: Double, symbol: String, isExpense: Boolean): String =
        (if (isExpense) MINUS else "+") + symbol + formatter.get()!!.format(abs(fromCents(cents(value))))

    /**
     * `$304.50`, `$1.2k`, `$3.4M` — for tight tiles.
     *
     * Below 1000 the cents are kept: showing `$304.5` next to a balance card
     * that says `$1,395.52` looks like a rounding bug, and the tile has room
     * for two extra characters.
     */
    fun compact(value: Double, symbol: String): String {
        val c = cents(value)
        val v = abs(fromCents(c))
        val sign = if (c < 0) "-" else ""
        val body = when {
            v >= 1_000_000 -> compactFormatter.get()!!.format(v / 1_000_000) + "M"
            v >= 1_000 -> compactFormatter.get()!!.format(v / 1_000) + "k"
            else -> formatter.get()!!.format(v)
        }
        return sign + symbol + body
    }

    /**
     * Parses user input into an amount, accepting both `12.50` and `12,50`
     * regardless of the device locale. Returns null for anything unparseable
     * so the caller can show a field error instead of failing silently.
     */
    fun parse(input: String): Double? {
        val cleaned = input.trim()
            .replace(" ", "")
            .replace(',', '.')
            .let { if (it.count { ch -> ch == '.' } > 1) return null else it }
        if (cleaned.isEmpty()) return null
        val value = cleaned.toDoubleOrNull() ?: return null
        return if (value.isNaN() || value.isInfinite() || value < 0 || value > 9_999_999) null else value
    }

    /**
     * Editor-friendly text for an amount (`12.5`, not `12.499999`, and no
     * grouping separators that the parser would misread).
     */
    fun toInput(value: Double): String {
        val raw = String.format(Locale.ROOT, "%.2f", fromCents(cents(value)))
        return raw.trimEnd('0').trimEnd('.')
    }

    private val formatter = object : ThreadLocal<DecimalFormat>() {
        override fun initialValue(): DecimalFormat =
            DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.getDefault()))
    }

    private val compactFormatter = object : ThreadLocal<DecimalFormat>() {
        override fun initialValue(): DecimalFormat =
            DecimalFormat("#,##0.#", DecimalFormatSymbols.getInstance(Locale.getDefault()))
    }
}
