package io.sulek.currencyfield.data

internal enum class Code(private val value: Int) {
    USD(0),
    EUR(1);

    companion object {
        fun getByValue(value: Int) = values().first { it.value == value }
    }
}