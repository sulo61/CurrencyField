package io.sulek.currencyfield.factory

import java.util.*

internal sealed class CurrencyStrategy(
    val symbol: String,
    val printSymbol: String,
    val code: String,
    val thousandDivider: Char,
    val comaDivider: Char,
    val extraCharsRegex: String,
    val locale: Locale,
    val symbolLength: Int = symbol.length
)

internal object USDCurrencyStrategy : CurrencyStrategy(
    "$",
    "$",
    "USD",
    ',',
    '.',
    "[\$,]",
    Locale.US
)