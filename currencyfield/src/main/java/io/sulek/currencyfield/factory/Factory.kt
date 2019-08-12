package io.sulek.currencyfield.factory

import io.sulek.currencyfield.Constants.DEFAULT_DOUBLE
import io.sulek.currencyfield.Constants.DEFAULT_INT
import io.sulek.currencyfield.Constants.DEFAULT_SELECTION
import io.sulek.currencyfield.Constants.EMPTY_STRING
import io.sulek.currencyfield.Constants.MAX_FRACTION_DIGITS
import io.sulek.currencyfield.data.Code
import io.sulek.currencyfield.data.Result
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*
import kotlin.math.min

internal class Factory(code: Code) {
    private val strategy: CurrencyStrategy = when (code) {
        Code.USD -> USDCurrencyStrategy
    }
    private val formatter = NumberFormat.getCurrencyInstance(strategy.locale).apply {
        currency = Currency.getInstance(strategy.code)
        maximumFractionDigits = MAX_FRACTION_DIGITS
        roundingMode = RoundingMode.DOWN
    }

    private val extraCharsRegex = Regex(strategy.extraCharsRegex)

    private var lastText = EMPTY_STRING
    private var currentText = EMPTY_STRING
    private var currentCleanedText = EMPTY_STRING
    private var nextText = EMPTY_STRING
    private var lastValue = DEFAULT_DOUBLE
    private var currentValue = DEFAULT_DOUBLE
    private var currentSelection = DEFAULT_SELECTION
    private var nextSelection = DEFAULT_SELECTION
    private var currentExtraCharsCounter = DEFAULT_INT
    private var nextExtraCharsCounter = DEFAULT_INT

    fun parse(input: String, inputSelection: Int): Result {
        currentText = input
        currentCleanedText = currentText.replace(extraCharsRegex, EMPTY_STRING)
        currentSelection = inputSelection

        if (currentCleanedText.isEmpty()) {
            nextText = EMPTY_STRING
            lastText = EMPTY_STRING
            currentValue = DEFAULT_DOUBLE
            nextSelection = currentSelection
            updateLastValues()
            return Result(nextText, nextSelection)
        }

        currentValue = currentCleanedText.toDouble()

        if (currentValue == DEFAULT_DOUBLE) {
            nextText = EMPTY_STRING
            currentText = EMPTY_STRING
            currentValue = DEFAULT_DOUBLE
            nextSelection = DEFAULT_SELECTION
            updateLastValues()
            return Result(nextText, nextSelection)
        }

        nextText = formatter.format(currentValue)
        currentSelection = min(currentSelection, nextText.length)

        if (currentText.length > lastText.length) {
            currentExtraCharsCounter = countExtraChars(currentText)
            nextExtraCharsCounter = countExtraChars(nextText)
            nextSelection = currentSelection + nextExtraCharsCounter - currentExtraCharsCounter
            currentText = nextText
            updateLastValues()
            return Result(nextText, nextSelection)
        }


        if (currentText.length <= lastText.length) {
            if (lastText.contains(strategy.printSymbol) && !currentText.contains(strategy.printSymbol)) {
                currentText = lastText
                nextText = lastText
                currentValue = lastValue
                nextSelection = 1
                return Result(nextText, nextSelection)
            }
            if (lastText.contains(strategy.comaDivider) && !currentText.contains(strategy.comaDivider)) {
                currentText = lastText
                nextText = lastText
                currentValue = lastValue
                nextSelection = currentSelection
                return Result(nextText, nextSelection)
            }
            if (currentText.length == lastText.length) {
                nextSelection = currentSelection
                updateLastValues()
                return Result(nextText, nextSelection)
            }
            currentExtraCharsCounter = countExtraChars(currentText)
            nextExtraCharsCounter = countExtraChars(nextText)
            nextSelection = currentSelection + nextExtraCharsCounter - currentExtraCharsCounter
            currentText = nextText
            updateLastValues()
            return Result(nextText, nextSelection)
        }

        if (lastValue == currentValue) {
            nextSelection = currentSelection - 1
            updateLastValues()
            return Result(nextText, nextSelection)
        }

        updateLastValues()
        return Result(nextText, nextSelection)
    }

    private fun updateLastValues() {
        lastText = currentText
        lastValue = currentValue
    }

    private fun countExtraChars(input: String): Int {
        var counter = 0

        if (input.contains(strategy.symbol)) counter += strategy.symbolLength

        for (i in 0 until currentSelection) {
            if (input[i] == strategy.thousandDivider) counter++
        }

        return counter
    }
}