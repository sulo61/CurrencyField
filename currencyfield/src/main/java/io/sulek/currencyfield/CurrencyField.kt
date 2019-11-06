package io.sulek.currencyfield

import android.content.Context
import android.text.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import android.text.method.DigitsKeyListener
import io.sulek.currencyfield.data.Charset
import io.sulek.currencyfield.data.Details
import java.math.BigDecimal

class CurrencyField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var currencyCode: String = Constants.EMPTY_STRING
    private val details: Details
    private var currencyFactory: CurrencyFactory
    private var ignoreTextChange = false
    private var listener: Listener? = null
    private val inputRegex: Regex
    private val textWatcher: TextWatcher

    private val inputFilters: Array<InputFilter>

    init {
        getAttributes(attrs, context)
        details = CurrencyDetailsFactory.getDetails(currencyCode)
        inputRegex = createInputRegex()
        inputFilters = arrayOf(createInputFilter())
        textWatcher = createTextWatcher()

        currencyFactory = CurrencyFactory(currencyCode, details)

        addTextChangedListener(textWatcher)
        keyListener = DigitsKeyListener.getInstance("0123456789.,")
        filters = inputFilters
    }

    fun getValue() = currencyFactory.getLastValue()

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
        val parseResult = currencyFactory.parse(cleanedTextValue, cleanedTextValue.length, true)

        this.ignoreTextChange = true
        setText(parseResult.text)
        setSelection(parseResult.position)
        if (notifyOnTextChange) listener?.onChange(parseResult.text, currencyFactory.getLastValue())
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
                with(R.styleable.CurrencyField_attrCurrencyCode) {
                    if (hasValue(this)) currencyCode = getString(this) ?: Constants.EMPTY_STRING
                    else throw Exception("attrCurrencyCode is required")
                }

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
            with(currencyFactory.parse(it, selectionStart)) {
                ignoreTextChange = true
                setText(text)
                setSelection(position)
                listener?.onChange(text, currencyFactory.getLastValue())
            }
        }
    }

    private fun createInputRegex() = Regex(
        when (details.charset) {
            Charset.COMA_AND_DOT -> "[0-9\\.]"
            Charset.SPACE_AND_COMA -> "[0-9\\,]"
        }
    )

    interface Listener {
        fun onChange(text: String, value: Double)
    }
}