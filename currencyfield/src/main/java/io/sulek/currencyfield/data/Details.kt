package io.sulek.currencyfield.data

data class Details(
    val currencyCode: String,
    val currencySymbol: String,
    val symbolPosition: SymbolPosition,
    val charset: Charset
)