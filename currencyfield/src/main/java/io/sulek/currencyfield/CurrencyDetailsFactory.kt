package io.sulek.currencyfield

import io.sulek.currencyfield.data.Charset
import io.sulek.currencyfield.data.Details
import io.sulek.currencyfield.data.SymbolPosition
import java.lang.IllegalArgumentException

object CurrencyDetailsFactory {

    fun getDetails(codeValue: Int) = when (codeValue) {
        0 -> Details("USD", "$", SymbolPosition.BEGIN, Charset.COMA_AND_DOT)
        1 -> Details("EUR", "€", SymbolPosition.END, Charset.SPACE_AND_COMA)
        else -> throw IllegalArgumentException("unsupported code")
    }

}