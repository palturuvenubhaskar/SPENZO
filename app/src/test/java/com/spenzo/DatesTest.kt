package com.spenzo

import com.spenzo.data.Dates
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class DatesTest {

    private lateinit var previous: Locale

    @Before
    fun setUp() {
        previous = Locale.getDefault()
    }

    @After
    fun tearDown() {
        Locale.setDefault(previous)
    }

    @Test
    fun `storage keys stay ISO even on a non-gregorian locale`() {
        // Thai Buddhist calendar used to produce "2569-11-13" and break every
        // LIKE 'yyyy-MM%' query.
        Locale.setDefault(Locale.forLanguageTag("th-TH-u-ca-buddhist"))
        assertEquals("2026-11-13", Dates.dayLabel(LocalDate.of(2026, 11, 13)))
        assertEquals("2026-11", Dates.monthKey(LocalDate.of(2026, 11, 13)))
    }

    @Test
    fun `shift month crosses year boundaries`() {
        assertEquals("2026-01", Dates.shiftMonth("2025-12", 1))
        assertEquals("2025-12", Dates.shiftMonth("2026-01", -1))
        assertEquals("2029-02", Dates.shiftMonth("2028-01", 13))
    }

    @Test
    fun `days elapsed stops at today for the current month`() {
        val today = LocalDate.of(2026, 11, 13)
        assertEquals(13, Dates.daysElapsed("2026-11", today))
    }

    @Test
    fun `days elapsed uses the full length for past months`() {
        val today = LocalDate.of(2026, 11, 13)
        assertEquals(31, Dates.daysElapsed("2026-10", today))
        assertEquals(28, Dates.daysElapsed("2026-02", today))
        assertEquals(29, Dates.daysElapsed("2028-02", today))
    }

    @Test
    fun `group labels prefer today and yesterday`() {
        val today = LocalDate.of(2026, 11, 13)
        assertEquals("Today", Dates.groupLabel("2026-11-13", today))
        assertEquals("Yesterday", Dates.groupLabel("2026-11-12", today))
    }

    @Test
    fun `round trip between millis and local date`() {
        val date = LocalDate.of(2026, 3, 1)
        assertEquals(date, Dates.toLocalDate(Dates.toMillis(date)))
    }
}
