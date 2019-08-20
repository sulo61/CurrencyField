package io.sulek.currencyfield.factory

import android.util.Log
import io.sulek.currencyfield.Constants.DEFAULT_VALUE
import io.sulek.currencyfield.Constants.DEFAULT_INT
import io.sulek.currencyfield.Constants.DEFAULT_SELECTION
import io.sulek.currencyfield.Constants.EMPTY_STRING
import io.sulek.currencyfield.Constants.MAX_FRACTION_DIGITS
import io.sulek.currencyfield.data.Code
import io.sulek.currencyfield.data.Result
import io.sulek.currencyfield.data.SymbolPosition
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*
import kotlin.math.min

internal class Factory(code: Code) {

    companion object {
        private const val PRINT_DEBUG_LOGS = false
        private const val TAG = "Factory"
    }

    private val strategy: CurrencyStrategy = when (code) {
        Code.USD -> USDCurrencyStrategy
        Code.EUR -> EURCurrencyStrategy
    }
    private val formatter = NumberFormat.getCurrencyInstance(strategy.locale).apply {
        currency = Currency.getInstance(strategy.code)
        maximumFractionDigits = MAX_FRACTION_DIGITS
        roundingMode = RoundingMode.DOWN
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

    fun parse(currentText: String, currentSelection: Int, cleanHistory: Boolean = false): Result {
        if (cleanHistory) {
            lastSelection = DEFAULT_SELECTION
            lastText = EMPTY_STRING
            lastValue = DEFAULT_VALUE
        }

        cleanedText = currentText.replace(strategy.specialCharsRegex, EMPTY_STRING)

        // EMPTY
        if (cleanedText.isEmpty()) {
            printStep("Empty string")
            nextText = EMPTY_STRING
            nextSelection = DEFAULT_SELECTION
            updateLastValues()
            return Result(nextText, nextSelection)
        }

        currentValue = cleanedText.currencyTextToDouble()

        // EMPTY
        if (currentValue == DEFAULT_VALUE) {
            printStep("Empty value")
            nextText = EMPTY_STRING
            nextSelection = DEFAULT_SELECTION
            updateLastValues()
            return Result(nextText, nextSelection)
        }

        nextText = formatter.format(currentValue)
        nextSelection = min(currentSelection, nextText.length)

        // INCORRECT FORMAT
        if (strategy.printSymbolRegex.containsMatchIn(lastText) && !strategy.printSymbolRegex.containsMatchIn(
                currentText
            )
        ) {
            printStep("Incorrect format - missing symbol")
            nextText = lastText

            if (strategy.symbolPosition == SymbolPosition.START) {
                nextSelection = strategy.symbolLength
                lastSelection = nextSelection
            } else {
                nextSelection = nextText.length - strategy.symbolLength
                lastSelection = nextSelection
            }

            return Result(nextText, nextSelection)
        }

        // INCORRECT FORMAT
        if (lastText.contains(strategy.dividerChar) && !currentText.contains(strategy.dividerChar)) {
            printStep("Incorrect format - missing divider")
            nextText = lastText
            nextSelection = currentSelection
            lastSelection = currentSelection
            return Result(nextText, nextSelection)
        }

        // ADD CHAR
        if (currentText.length > lastText.length) {
            printStep("Add char")
            if (nextText == lastText) {
                printStep("Add char - next and last are the same")
                nextSelection = currentSelection - 1
                lastSelection = nextSelection
                return Result(nextText, nextSelection)
            }
            currentSpecialCharsCounter = countSpecialChars(strategy.symbolPosition, currentText, currentSelection)
            nextSpecialCharsCounter = countSpecialChars(strategy.symbolPosition, nextText, nextSelection)
            nextSelection = nextSelection + nextSpecialCharsCounter - currentSpecialCharsCounter
            printProperties(currentText, currentSelection)
            updateLastValues()
            return Result(nextText, nextSelection)
        }

        // REMOVE CHAR
        if (currentText.length < lastText.length) {
            printStep("Remove char")
            lastSpecialCharsCounter = countSpecialChars(strategy.symbolPosition, lastText, lastText.length)
            currentSpecialCharsCounter = countSpecialChars(strategy.symbolPosition, currentText, currentText.length)

            // ON REMOVE THOUSAND DIVIDER, THEN REMOVE CHAR BEFORE DIVIDER
            if (currentValue == lastValue && currentSpecialCharsCounter != lastSpecialCharsCounter) {
                printStep("Removed separator - calling parse again")
                with(currentSelection - 1) {
                    return parse(nextText.removeRange(this, currentSelection), this)
                }
            }

            // REMOVE ANOTHER CHAR - DIGIT
            printStep("Removed digit")
            lastSpecialCharsCounter = countSpecialChars(strategy.symbolPosition, lastText, currentSelection)
            nextSpecialCharsCounter = countSpecialChars(strategy.symbolPosition, nextText, currentSelection - 1)
            nextSelection = currentSelection - lastSpecialCharsCounter + nextSpecialCharsCounter
            updateLastValues()
            return Result(nextText, nextSelection)
        }

        printStep("Unknown")
        updateLastValues()
        return Result(nextText, nextSelection)
    }

    fun getLastText() = lastText

    fun getLastValue() = lastValue

    private fun updateLastValues() {
        lastText = nextText
        lastSelection = nextSelection
        lastValue =
            if (nextText.isEmpty()) DEFAULT_VALUE
            else nextText.replace(strategy.specialCharsRegex, EMPTY_STRING).currencyTextToDouble()
    }

    private fun countSpecialChars(symbolPosition: SymbolPosition, input: String, limit: Int): Int {
        var counter = 0

        if (symbolPosition == SymbolPosition.START && input.contains(strategy.symbol)) {
            counter += strategy.symbolLength
        }

        for (i in 0 until limit) {
            if (strategy.thousandRegex.matches(input[i].toString())) counter++
        }

        return counter
    }

    private fun String.currencyTextToDouble() = replace(",", ".").toDouble()


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