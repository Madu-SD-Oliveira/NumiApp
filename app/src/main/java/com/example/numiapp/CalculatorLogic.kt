package com.example.numiapp

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class CalculatorLogic {

    private val formatDecimal = DecimalFormat("0.########").apply {
        maximumFractionDigits = 8
        isGroupingUsed = false
    }

    fun appendDecimal(currentInput: String): String {
        return when {
            currentInput.isEmpty() -> "0."
            currentInput.contains(".") -> currentInput
            else -> "$currentInput."
        }
    }

    fun mapOperator(op: String): String = when (op) {
        "×" -> "*"
        "÷" -> "/"
        else -> op
    }

    fun calculate(firstNumber: Double, secondValue: Double, operation: String): Double {
        return when (operation) {
            "+" -> firstNumber + secondValue
            "-" -> firstNumber - secondValue
            "*" -> firstNumber * secondValue
            "/" -> if (secondValue != 0.0) firstNumber / secondValue else Double.NaN
            else -> secondValue
        }
    }

    fun calculatePercentage(firstNumber: Double, currentInput: Double): Double {
        return if (firstNumber.isNaN()) {
            currentInput / 100
        } else {
            (firstNumber * currentInput) / 100
        }
    }

    fun formatValue(
        value: Double,
        msgDivideZero: String = "Error: Div/0",
        msgTooLarge: String = "TOO_LARGE"
    ): String {
        return when {
            value.isNaN() -> msgDivideZero
            value.isInfinite() -> msgTooLarge
            kotlin.math.abs(value) >= 1e15 || (kotlin.math.abs(value) < 1e-7 && value != 0.0) -> {
                String.format(Locale.US, "%.5E", value)
            }
            else -> formatDecimal.format(value)
        }
    }
}