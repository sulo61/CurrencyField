package io.sulek.currencyfield

import android.content.Context
import android.text.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import android.text.InputType
import io.sulek.currencyfield.data.Code
import io.sulek.currencyfield.factory.Factory

class CurrencyField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private val factory: Factory =
        Factory(Code.USD)
    private val textWatcher = createTextWatcher()
    private val inputFilters = arrayOf(createInputFilter())
    private var ignoreTextChange = false

    init {
        addTextChangedListener(textWatcher)
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        filters = inputFilters
    }

    private fun createInputFilter() =
        InputFilter { source, _, _, _, _, _ ->
            if (ignoreTextChange || source.length <= Constants.MAX_CHARS_TO_INPUT) source
            else Constants.EMPTY_STRING
        }

    private fun createTextWatcher() = object : CustomTextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = onTextChanged()
    }

    private fun onTextChanged() {
        if (ignoreTextChange) {
            ignoreTextChange = false
            return
        }

        text?.toString()?.let {
            with(factory.parse(it, selectionStart)) {
                ignoreTextChange = true
                setText(text)
                setSelection(position)
            }
        }
    }
}