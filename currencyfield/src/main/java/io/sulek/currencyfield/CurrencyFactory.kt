package io.sulek.currencyfield

import android.util.Log
import io.sulek.currencyfield.Constants.DEFAULT_VALUE
import io.sulek.currencyfield.Constants.DEFAULT_INT
import io.sulek.currencyfield.Constants.DEFAULT_SELECTION
import io.sulek.currencyfield.Constants.EMPTY_STRING
import io.sulek.currencyfield.Constants.MAX_FRACTION_DIGITS
import io.sulek.currencyfield.Constants.NO_FRACTION_DIGITS
import io.sulek.currencyfield.data.Charset
import io.sulek.currencyfield.data.SymbolPosition
import io.sulek.currencyfield.data.Result
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*
import kotlin.math.min

internal class CurrencyFactory(
    private val currencySymbol: String,
    private val currencyCode: String,
    private val symbolPosition: SymbolPosition,
    private val charset: Charset
) {

    companion object {
        private const val PRINT_DEBUG_LOGS = true
        private const val TAG = "CurrencyFactory"
    }

    private val formatter = NumberFormat.getCurrencyInstance(getLocale()).apply {
        currency = Currency.getInstance(currencyCode)
        maximumFractionDigits = NO_FRACTION_DIGITS
        roundingMode = RoundingMode.DOWN
    }
    private val thousandDividerRegex = Regex(
        when (charset) {
            Charset.COMA_AND_DOT -> "[,]"
            Charset.SPACE_AND_COMA -> "[\\s]"
        }
    )
    private val decimalDivider = when (charset) {
        Charset.COMA_AND_DOT -> '.'
        Charset.SPACE_AND_COMA -> ','
    }
    private val currencySymbolInText = when (symbolPosition) {
        SymbolPosition.BEGIN -> currencySymbol
        SymbolPosition.END -> " $currencySymbol"
    }

    private var lastText = EMPTY_STRING
    private var cleanedText = EMPTY_STRING
    private var nextText = EMPTY_STRING
    private var lastValue = DEFAULT_VALUE
    private var currentValue = DEFAULT_VALUE
    private var lastSelection = DEFAULT_SELECTION
    private var nextSelection = DEFAULT_SELECTION
    private var lastSpecialCharsCounter = DEFAULT_INT
    private var currentSpecialCharsCounter = DEFAULT_INT
    private var nextSpecialCharsCounter = DEFAULT_INT
    private var currentFractionDigits = NO_FRACTION_DIGITS

    fun parse(currentText: String, currentSelection: Int, cleanHistory: Boolean = false): Result {
        if (cleanHistory) {
            lastSelection = DEFAULT_SELECTION
            lastText = EMPTY_STRING
            lastValue = DEFAULT_VALUE
        }

        cleanedText = cleanTextFromExtraChars(currentText)

        // EMPTY TEXT
        if (cleanedText.isEmpty() || cleanedText == decimalDivider.toString()) {
            printStep("Empty string")
            nextText = EMPTY_STRING
            nextSelection = DEFAULT_SELECTION
            updateLastValues()
            return Result(nextText, nextSelection)
        }

        currentValue = cleanedText.currencyTextToDouble()

        // EMPTY VALUE
        if (currentValue == DEFAULT_VALUE) {
            printStep("Empty value")
            nextText = EMPTY_STRING
            nextSelection = DEFAULT_SELECTION
            updateLastValues()
            return Result(nextText, nextSelection)
        }

        // INCORRECT FORMAT - MISSING SYMBOL
        if (lastText.contains(currencySymbolInText) && !currentText.contains(currencySymbolInText)) {
            printStep("Incorrect format - missing symbol")
            nextText = lastText

            if (symbolPosition == SymbolPosition.BEGIN) {
                nextSelection = currencySymbolInText.length
                lastSelection = nextSelection
            } else {
                nextSelection = nextText.length - currencySymbolInText.length
                lastSelection = nextSelection
            }

            return Result(nextText, nextSelection)
        }

        // INCORRECT FORMAT - TOO MANY DIVIDER CHARS
        if (currentText.count { it == decimalDivider } > 1) {
            printStep("Incorrect format - too many divider chars")
            nextText = lastText
            nextSelection = lastSelection
            updateLastValues()
            return Result(nextText, nextSelection)
        }

        // ADD CHAR
        if (currentText.length > lastText.length) {
            printStep("Add char")

            if (!lastText.contains(decimalDivider) && cleanedText.last() == decimalDivider) {
                printStep("Add char - divider at the end")
                nextText = currentText
                nextSelection = currentSelection
                updateLastValues()
                return Result(nextText, nextSelection)
            }

            setFractionDigitsForFormatter(currentText)

            nextText = formatter.format(currentValue)
            nextSelection = min(currentSelection, nextText.length)

            if (nextText == lastText) {
                printStep("Add char - next and last are the same")
                nextSelection = currentSelection - 1
                lastSelection = nextSelection
                return Result(nextText, nextSelection)
            }

            currentSpecialCharsCounter = countSpecialChars(symbolPosition, currentText, currentSelection)
            nextSpecialCharsCounter = countSpecialChars(symbolPosition, nextText, currentSelection)
            nextSelection = currentSelection - currentSpecialCharsCounter + nextSpecialCharsCounter
            updateLastValues()
            return Result(nextText, nextSelection)
        }

        if (currentText.length < lastText.length) {
            printStep("Remove char")

            if (currentText.last() == decimalDivider) {
                printStep("Remove char - divider at the end")
                nextText = currentText
                nextSelection = currentSelection
                updateLastValues()
                return Result(nextText, nextSelection)
            }

            setFractionDigitsForFormatter(currentText)

            nextText = formatter.format(currentValue)
            nextSelection = min(currentSelection, nextText.length)

            lastSpecialCharsCounter = countSpecialChars(symbolPosition, lastText, lastText.length)
            currentSpecialCharsCounter = countSpecialChars(symbolPosition, currentText, currentText.length)

            if (currentValue == lastValue && currentSpecialCharsCounter != lastSpecialCharsCounter) {
                printStep("Remove char - thousand separator, remove one more")
                currentValue = cleanedText.substring(0, cleanedText.length - 1).currencyTextToDouble()
                nextText = formatter.format(currentValue)
                nextSelection = min(currentSelection, nextText.length)
            }

            lastSpecialCharsCounter = countSpecialChars(symbolPosition, lastText, lastSelection)
            nextSpecialCharsCounter =
                countSpecialChars(symbolPosition, nextText, min(nextText.length, lastSelection - 1))
            nextSelection = currentSelection - lastSpecialCharsCounter + nextSpecialCharsCounter
            updateLastValues()
            return Result(nextText, nextSelection)
        }

        return Result(currentText, currentSelection)
    }

    private fun cleanTextFromExtraChars(currentText: String): String {
        return currentText
            .replace(thousandDividerRegex, EMPTY_STRING)
            .replace(currencySymbolInText, EMPTY_STRING)
            .replace(currencySymbol, EMPTY_STRING)
    }

    fun getLastText() = lastText

    fun getLastValue() = lastValue

    private fun updateLastValues() {
        lastText = nextText
        lastSelection = nextSelection
        lastValue = if (nextText.isEmpty()) DEFAULT_VALUE else cleanTextFromExtraChars(nextText).currencyTextToDouble()
    }

    private fun countSpecialChars(symbolPosition: SymbolPosition, input: String, limit: Int): Int {
        var counter = 0

        if (symbolPosition == SymbolPosition.BEGIN && input.contains(currencySymbol)) counter += currencySymbolInText.length

        for (i in 0 until limit) if (thousandDividerRegex.matches(input[i].toString())) counter++

        return counter
    }

    private fun String.currencyTextToDouble() = replace(",", ".").toDouble()

    private fun setFractionDigitsForFormatter(currentText: String) {
        currentFractionDigits =
            if (currentText.contains(decimalDivider)) {
                currentText.length - currentText.indexOf(decimalDivider) - 1 - if (symbolPosition == SymbolPosition.END) currencySymbolInText.length else 0
            } else NO_FRACTION_DIGITS
        formatter.maximumFractionDigits = min(currentFractionDigits, MAX_FRACTION_DIGITS)
        formatter.minimumFractionDigits = min(currentFractionDigits, MAX_FRACTION_DIGITS)
    }

    private fun getLocale() = when (charset) {
        Charset.COMA_AND_DOT -> Locale.US
        Charset.SPACE_AND_COMA -> Locale.CANADA_FRENCH
    }

    // ONLY FOR DEBUG
    private fun printProperties(currentText: String, currentSelection: Int) {
        if (!PRINT_DEBUG_LOGS) return
        Log.d(TAG, "-------------------------- START")
        Log.d(TAG, "TEXT:")
        Log.d(TAG, "\t\tlastText: $lastText")
        Log.d(TAG, "\t\tcurrentText: $currentText")
        Log.d(TAG, "\t\tcleanedText: $cleanedText")
        Log.d(TAG, "\t\tnextText: $nextText")
        Log.d(TAG, "VALUES:")
        Log.d(TAG, "\t\tlastValue: $lastValue")
        Log.d(TAG, "\t\tcurrentValue: $currentValue")
        Log.d(TAG, "SELECTION:")
        Log.d(TAG, "\t\tlastSelection: $lastSelection")
        Log.d(TAG, "\t\tcurrentSelection: $currentSelection")
        Log.d(TAG, "\t\tnextSelection: $nextSelection")
        Log.d(TAG, "COUNTER:")
        Log.d(TAG, "\t\tlastSpecialCharsCounter: $lastSpecialCharsCounter")
        Log.d(TAG, "\t\tcurrentSpecialCharsCounter: $currentSpecialCharsCounter")
        Log.d(TAG, "\t\tnextSpecialCharsCounter: $nextSpecialCharsCounter")
        Log.d(TAG, "-------------------------- END")
        Log.d(TAG, " ")
    }

    private fun printStep(step: String) {
        if (!PRINT_DEBUG_LOGS) return
        Log.d(TAG, "STEP: $step")
    }
}