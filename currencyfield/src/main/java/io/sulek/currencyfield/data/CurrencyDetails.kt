package io.sulek.currencyfield.data

data class CurrencyDetails(
    val currencySymbol: String,
    val symbolPosition: SymbolPosition,
    val charset: Charset
)