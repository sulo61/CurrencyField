package io.sulek.currencyfield

import android.text.Editable
import android.text.TextWatcher

internal interface CustomTextWatcher : TextWatcher {

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
}