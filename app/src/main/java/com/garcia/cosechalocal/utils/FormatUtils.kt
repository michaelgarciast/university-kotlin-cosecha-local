package com.garcia.cosechalocal.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object FormatUtils {
    fun formatPrice(amount: Int): String {
        val symbols = DecimalFormatSymbols(Locale.forLanguageTag("es-CO")).apply {
            groupingSeparator = '.'
        }
        val formatter = DecimalFormat("#,###", symbols)
        return "$${formatter.format(amount)}"
    }

    fun formatMillions(amount: Int): String =
        "$" + String.format(Locale.forLanguageTag("es-CO"), "%.1f", amount / 1_000_000.0) + " M"
}
