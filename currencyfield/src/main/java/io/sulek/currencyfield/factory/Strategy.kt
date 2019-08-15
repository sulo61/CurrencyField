package io.sulek.currencyfield.factory

import io.sulek.currencyfield.data.SymbolPosition
import java.util.*

internal sealed class CurrencyStrategy(
    val symbol: String,
    val printSymbol: String,
    val symbolPosition: SymbolPosition,
    val code: String,
    val dividerChar: Char,
    val thousandRegex: Regex,
    val specialCharsRegex: Regex,
    val locale: Locale,
    val symbolLength: Int = printSymbol.length
)

internal object USDCurrencyStrategy : CurrencyStrategy(
    "$",
    "$",
    SymbolPosition.START,
    "USD",
    '.',
    Regex("[,]"),
    Regex("[\$,]"),
    Locale.US
)

internal object EURCurrencyStrategy : CurrencyStrategy(
    "€",
    " €",
    SymbolPosition.END,
    "EUR",
    ',',
    Regex("[\\s]"),
    Regex("[€\\s]"),
    Locale.CANADA_FRENCH
)