package com.spenzo

import com.spenzo.data.Money
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Locale

class MoneyTest {

    private lateinit var previous: Locale

    @Before
    fun setUp() {
        previous = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Locale.setDefault(previous)
    }

    @Test
    fun `negative amounts put the sign before the symbol`() {
        assertEquals("\u2212$12.00", Money.format(-12.0, "$"))
    }

    @Test
    fun `formats with grouping and two decimals`() {
        assertEquals("$1,234.50", Money.format(1234.5, "$"))
    }

    @Test
    fun `cents rounds half away from zero`() {
        assertEquals(30L, Money.cents(0.1 + 0.2))
        assertEquals(100L, Money.cents(0.995))
    }

    @Test
    fun `sum does not accumulate float error`() {
        assertEquals(0.3, Money.sum(listOf(0.1, 0.2)), 0.0)
        assertEquals(1.0, Money.sum(List(10) { 0.1 }), 0.0)
    }

    @Test
    fun `compact keeps cents below one thousand`() {
        assertEquals("$1.2k", Money.compact(1234.0, "$"))
        assertEquals("$3M", Money.compact(3_000_000.0, "$"))
        assertEquals("$12.40", Money.compact(12.4, "$"))
        assertEquals("$304.50", Money.compact(304.5, "$"))
    }

    @Test
    fun `parse accepts comma decimals`() {
        assertEquals(12.5, Money.parse("12,50")!!, 0.0)
        assertEquals(12.5, Money.parse("12.50")!!, 0.0)
        assertEquals(12.0, Money.parse(" 12 ")!!, 0.0)
    }

    @Test
    fun `parse rejects malformed and negative input`() {
        assertNull(Money.parse("1.2.3"))
        assertNull(Money.parse("-5"))
        assertNull(Money.parse("abc"))
        assertNull(Money.parse(""))
    }

    @Test
    fun `signed uses the transaction type`() {
        assertEquals("\u2212$5.00", Money.signed(5.0, "$", isExpense = true))
        assertEquals("+$5.00", Money.signed(5.0, "$", isExpense = false))
    }
}
