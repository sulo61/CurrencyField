package io.sulek.currencyfield

import io.sulek.currencyfield.data.Charset
import io.sulek.currencyfield.data.CurrencyDetails
import io.sulek.currencyfield.data.SymbolPosition
import java.lang.IllegalArgumentException

object CurrencyDetailsFactory {

    fun getDetails(code: String) = when (code) {
        "USD" -> CurrencyDetails("$", SymbolPosition.BEGIN, Charset.COMA_AND_DOT)
        "EUR" -> CurrencyDetails("€", SymbolPosition.END, Charset.SPACE_AND_COMA)
        else -> throw IllegalArgumentException("unsupported code")
    }

}