package io.sulek.currencyfield.data

enum class SymbolPosition(private val id: Int) {
    BEGIN(0),
    END(1);

    companion object {
        fun getById(id: Int) = values().first { it.id == id }
    }
}