package io.sulek.currencyfield

import io.sulek.currencyfield.data.Charset
import io.sulek.currencyfield.data.Details
import io.sulek.currencyfield.data.SymbolPosition
import java.lang.IllegalArgumentException

object CurrencyDetailsFactory {

    fun getDetails(code: String) = when (code) {
        "USD" -> Details("$", SymbolPosition.BEGIN, Charset.COMA_AND_DOT)
        "EUR" -> Details("€", SymbolPosition.END, Charset.SPACE_AND_COMA)
        else -> throw IllegalArgumentException("unsupported code")
    }

}