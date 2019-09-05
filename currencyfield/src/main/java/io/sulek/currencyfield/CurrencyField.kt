package io.sulek.currencyfield

import android.content.Context
import android.text.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import android.text.InputType
import io.sulek.currencyfield.data.Code
import io.sulek.currencyfield.factory.Factory
import java.math.BigDecimal

class CurrencyField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private val inputRegex = Regex("[0-9\\.]")
    private val textWatcher = createTextWatcher()
    private val inputFilters = arrayOf(createInputFilter())

    private var factory: Factory
    private var currencyCode = Code.USD
    private var ignoreTextChange = false

    private var listener: Listener? = null

    init {
        getAttributes(attrs, context)
        factory = Factory(currencyCode)

        addTextChangedListener(textWatcher)
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        filters = inputFilters
    }

    fun getValue() = factory.getLastValue()

    fun setDoubleValue(value: Double?, notifyOnTextChange: Boolean = false) {
        if (value != null) setTextValue(value.toString(), notifyOnTextChange)
        else setEmptyValue(notifyOnTextChange)
    }

    fun setBigDecimalValue(value: BigDecimal?, notifyOnTextChange: Boolean = false) {
        if (value != null) setTextValue(value.toString(), notifyOnTextChange)
        else setEmptyValue(notifyOnTextChange)
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    private fun setTextValue(textValue: String, notifyOnTextChange: Boolean) {
        val cleanedTextValue = textValue.replace(".00", "").replace(".0", "")
        val parseResult = factory.parse(cleanedTextValue, cleanedTextValue.length, true)

        this.ignoreTextChange = true
        setText(parseResult.text)
        setSelection(parseResult.position)
        if (notifyOnTextChange) listener?.onChange(parseResult.text, factory.getLastValue())
    }

    private fun setEmptyValue(notifyOnTextChange: Boolean) {
        this.ignoreTextChange = true
        setText(Constants.EMPTY_STRING)
        setSelection(Constants.DEFAULT_SELECTION)
        if (notifyOnTextChange) listener?.onChange(Constants.EMPTY_STRING, Constants.DEFAULT_VALUE)
    }

    private fun getAttributes(attrs: AttributeSet?, context: Context) {
        attrs?.let {
            with(context.obtainStyledAttributes(attrs, R.styleable.CurrencyField)) {
                currencyCode = Code.getByValue(getInt(R.styleable.CurrencyField_currency, 0))
                recycle()
            }
        }
    }

    private fun createInputFilter() =
        InputFilter { source, _, _, _, _, _ ->
            if (ignoreTextChange || inputRegex.matches(source)) source
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
                listener?.onChange(text, factory.getLastValue())
            }
        }
    }

    interface Listener {
        fun onChange(text: String, value: Double)
    }
}