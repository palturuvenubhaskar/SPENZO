package com.spenzo.ui

import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {
    @Composable
    fun currencyFormat(): NumberFormat = NumberFormat.getCurrencyInstance(LocalConfiguration.current.locale)

    @Composable
    fun formatCurrency(amount: Double): String = currencyFormat().format(amount)

    @Composable
    fun formatCurrencyNoSymbol(amount: Double): String {
        val fmt = currencyFormat()
        return fmt.format(amount).replace(fmt.currency.symbol, "").trim()
    }

    @Composable
    fun formatPercent(value: Double): String = "%.0f%%".format(value)

    @Composable
    fun formatPercentWithSign(value: Double): String = "%+.0f%%".format(value)

    @Composable
    fun dateFormat(pattern: String = "dd MMM"): SimpleDateFormat =
        SimpleDateFormat(pattern, LocalConfiguration.current.locale)

    @Composable
    fun formatDate(timestamp: Long, pattern: String = "dd MMM"): String =
        dateFormat(pattern).format(Date(timestamp))

    @Composable
    fun formatMonthYear(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM", LocalConfiguration.current.locale).format(Date(timestamp))

    @Composable
    fun formatYear(timestamp: Long): String =
        SimpleDateFormat("yyyy", LocalConfiguration.current.locale).format(Date(timestamp))
}