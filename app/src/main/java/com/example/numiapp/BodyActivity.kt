package com.example.numiapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.numiapp.databinding.ActivityBodyBinding

class BodyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBodyBinding
    private val calculatorLogic = CalculatorLogic()
    private var actualOperation = ""
    private var firstNumber: Double = Double.NaN
    private var currentInput: String = ""

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBodyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTouchEffects()
        restoreState(savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // --- BUTTON TOUCH EFFECTS --- //

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchEffects() {
        val buttons = arrayOf(
            binding.btnZero, binding.btnOne, binding.btnTwo, binding.btnThree,
            binding.btnFour, binding.btnFive, binding.btnSix, binding.btnSeven,
            binding.btnEight, binding.btnNine, binding.btnDot, binding.btnPlus,
            binding.btnMinus, binding.btnTimes, binding.btnDivide, binding.btnPercent,
            binding.btnClean, binding.btnCleanAll, binding.btnEqual
        )

        val touchScaleListener = View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }
            false
        }
        buttons.forEach { it.setOnTouchListener(touchScaleListener) }
    }

    // --- LIFECYCLE METHODS --- //

    private fun restoreState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            firstNumber = it.getDouble("firstNumber", Double.NaN)
            actualOperation = it.getString("actualOperation", "")
            binding.tvHistory.text = it.getString("tvHistoryText", "")
            val restored = it.getString("tvCurrentText", "")
            binding.tvCurrent.text = restored
            currentInput = restored

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putDouble("firstNumber", firstNumber)
        outState.putString("actualOperation", actualOperation)
        outState.putString("tvCurrentText", binding.tvCurrent.text.toString())
        outState.putString("tvHistoryText", binding.tvHistory.text.toString())
    }

    // --- UI METHODS --- //

    private fun showToastError() {
        Toast.makeText(
            this,
            getString(R.string.toast_invalid_input),
            Toast.LENGTH_SHORT
        ).show()

        binding.root.performHapticFeedback(
            android.view.HapticFeedbackConstants.CONFIRM)
    }

    fun selectNumber(v: View) {
        val textButton = (v as? Button)?.text?.toString() ?: return

        if (binding.tvCurrent.text == "Error") {
            currentInput = ""
            binding.tvCurrent.text = ""
        }

        if (textButton == ".") {
            if (currentInput.contains(".")) return
            currentInput = if (currentInput.isEmpty()) "0." else "$currentInput."
        } else {
            if (currentInput.length >= 15) {
                showToastError()
                return
            }
            currentInput += textButton
        }

        binding.tvCurrent.text = currentInput
    }

    fun changeOperator(v: View) {
        val operatorString = (v as? Button)?.text.toString().trim()
        val input = currentInput

        if (currentInput == "Error") return

        if (operatorString == "%") {
            val valor = currentInput.toDoubleOrNull() ?: return
            val result = calculatorLogic.calculatePercentage(firstNumber, valor)
            updateDisplay(result)
            return
        }

        if (currentInput.isEmpty()) {
            if (!firstNumber.isNaN()) {
                actualOperation = calculatorLogic.mapOperator(operatorString)
                binding.tvHistory.text = "${calculatorLogic.formatValue(firstNumber)} $operatorString"
            }
            return
        }

        if (!firstNumber.isNaN()) {
            performCalculation()
        } else {
            firstNumber = input.toDoubleOrNull() ?: return
        }

        actualOperation = calculatorLogic.mapOperator(operatorString)
        binding.tvHistory.text = "${calculatorLogic.formatValue(firstNumber)} $operatorString"
        binding.tvCurrent.text = ""
        currentInput = ""
    }

    private fun updateDisplay(result: Double) {
        val errorDivide = getString(R.string.error_divide_by_zero)
        val errorLarge = getString(R.string.error_value_too_large)

        val formatted = calculatorLogic.formatValue(result, errorDivide, errorLarge)
        binding.tvCurrent.text = formatted

        currentInput = if (
            formatted == errorDivide || formatted == errorLarge
        ) {
            ""
        } else {
            result.toString()
        }

        if (formatted == errorDivide || formatted == errorLarge) {
            firstNumber = Double.NaN
            actualOperation = ""
        }
    }

    private fun performCalculation() {
        val valor = currentInput.toDoubleOrNull() ?: return
        firstNumber = calculatorLogic.calculate(firstNumber, valor, actualOperation)
        currentInput = ""
    }

    fun equal(v: View) {
        if (currentInput.isEmpty() || actualOperation.isEmpty()) return

        performCalculation()
        updateDisplay(firstNumber)

        binding.tvHistory.text = ""
        firstNumber = Double.NaN
        actualOperation = ""
    }

    fun clean(v: View) {
        val textButton = (v as? Button)?.text?.toString() ?: return

        if (textButton == "C") {
            if (currentInput.isNotEmpty()) {
                currentInput = currentInput.dropLast(1)
                binding.tvCurrent.text = currentInput
            }
        } else {
            firstNumber = Double.NaN
            actualOperation = ""
            currentInput = ""
            binding.tvCurrent.text = ""
            binding.tvHistory.text = ""
        }
    }
}
