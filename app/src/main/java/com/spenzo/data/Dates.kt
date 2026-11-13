package com.spenzo.data

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Date handling for storage keys and display.
 *
 * Month/day keys are always ISO (`yyyy-MM`, `yyyy-MM-dd`) built with
 * Locale.ROOT. The old code used `SimpleDateFormat` with the device locale,
 * which silently produces `2569-11-13` on Thai devices and breaks every
 * `LIKE 'yyyy-MM%'` query.
 */
object Dates {
    private val DAY_KEY = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT)
    private val MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM", Locale.ROOT)

    private const val PATTERN_DAY_SHORT = "dd MMM"
    private const val PATTERN_DAY_LONG = "dd MMM yyyy"
    private const val PATTERN_MONTH = "MMMM yyyy"
    private const val PATTERN_WEEKDAY = "EEEE, d MMMM"

    /** Display formatters follow the current locale, even if it changes at runtime. */
    private data class FormatterKey(val locale: Locale, val pattern: String)

    private val displayFormatters = mutableMapOf<FormatterKey, DateTimeFormatter>()

    private fun display(pattern: String): DateTimeFormatter {
        val locale = Locale.getDefault()
        return displayFormatters.getOrPut(FormatterKey(locale, pattern)) {
            DateTimeFormatter.ofPattern(pattern, locale)
        }
    }

    fun today(): LocalDate = LocalDate.now()

    fun dayLabel(date: LocalDate): String = date.format(DAY_KEY)

    fun monthKey(date: LocalDate): String = date.format(MONTH_KEY)

    fun currentMonthKey(): String = monthKey(today())

    fun currentYearKey(): String = today().year.toString()

    fun parseMonth(key: String): YearMonth = YearMonth.parse(key)

    fun shiftMonth(key: String, delta: Long): String = parseMonth(key).plusMonths(delta).toString()

    fun daysInMonth(key: String): Int = parseMonth(key).lengthOfMonth()

    fun yearOf(monthKey: String): Int = parseMonth(monthKey).year

    fun toLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

    fun toMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    /**
     * Days that have actually elapsed in [key] (capped at the month length).
     * Used for "average daily spend" so a 31-day month is not divided by 30
     * and the current month is not divided by its full length.
     */
    fun daysElapsed(key: String, today: LocalDate = today()): Int {
        val ym = parseMonth(key)
        return when {
            ym == YearMonth.from(today) -> today.dayOfMonth
            ym.isAfter(YearMonth.from(today)) -> ym.lengthOfMonth()
            else -> ym.lengthOfMonth()
        }
    }

    fun prettyDay(millis: Long): String = toLocalDate(millis).format(display(PATTERN_DAY_SHORT))

    fun prettyDayLong(millis: Long): String = toLocalDate(millis).format(display(PATTERN_DAY_LONG))

    fun prettyMonth(key: String): String = parseMonth(key).format(display(PATTERN_MONTH))

    fun prettyToday(): String = today().format(display(PATTERN_WEEKDAY))

    /** "Today" / "Yesterday" / "13 Nov 2026" for list section headers. */
    fun groupLabel(dayKey: String, today: LocalDate = today()): String {
        val date = LocalDate.parse(dayKey, DAY_KEY)
        return when (date) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> if (date.year == today.year) {
                date.format(display(PATTERN_DAY_SHORT))
            } else {
                date.format(display(PATTERN_DAY_LONG))
            }
        }
    }
}
