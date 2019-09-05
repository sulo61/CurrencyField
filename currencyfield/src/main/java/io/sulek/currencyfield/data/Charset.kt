package io.sulek.currencyfield.data

enum class Charset(private val id: Int) {
    COMA_AND_DOT(0),
    SPACE_AND_COMA(1);

    companion object {
        fun getById(id: Int) = values().first { it.id == id }
    }
}