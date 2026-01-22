package com.example.numiapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.numiapp.databinding.ActivityBodyBinding
import java.text.DecimalFormat
class BodyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBodyBinding
    private var actualOperation = ""
    private var firstNumber: Double = Double.NaN

    // --- FORMAT --- //
    private val formatDecimal = DecimalFormat("0.########").apply {
        maximumFractionDigits = 8
        isGroupingUsed = false
    }

    // --- ON CREATE --- //

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBodyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- TOUCH SCALE: BUTTONS SIZE EFFECT--- //

        val buttons = arrayOf(
            binding.btnZero,
            binding.btnOne,
            binding.btnTwo,
            binding.btnThree,
            binding.btnFour,
            binding.btnFive,
            binding.btnSix,
            binding.btnSeven,
            binding.btnEight,
            binding.btnNine,
            binding.btnDot,
            binding.btnPlus,
            binding.btnMinus,
            binding.btnTimes,
            binding.btnDivide,
            binding.btnPercent,
            binding.btnClean,
            binding.btnCleanAll,
            binding.btnEqual
        )
        val touchScaleListener = View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(80).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                }
            }
            false
        }

        buttons.forEach { it.setOnTouchListener(touchScaleListener) }

        // --- RESTORE STATE: IF ORIENTATION CHANGES --- //

        if (savedInstanceState != null) {
            firstNumber = savedInstanceState.getDouble("firstNumber", Double.NaN)
            actualOperation = savedInstanceState.getString("actualOperation", "")
            binding.tvCurrent.text = savedInstanceState.getString("tvCurrentText", "")
            binding.tvHistory.text = savedInstanceState.getString("tvHistoryText", "")
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // --- SAVE INSTANCE --- //
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putDouble("firstNumber", firstNumber)
        outState.putString("actualOperation", actualOperation)
        outState.putString("tvCurrentText", binding.tvCurrent.text.toString())
        outState.putString("tvHistoryText", binding.tvHistory.text.toString())
    }

    // --- FUNCTIONS --- //

    // --- INPUT NUMBERS FROM BUTTONS --- //
    @SuppressLint("SetTextI18n")
    fun selectNumber(v: View) {
        val textButton = (v as? Button)?.text?.toString()?.trim() ?: return
        val textActual = binding.tvCurrent.text.toString()

        if (textActual == "Error") {
            binding.tvCurrent.text = ""
        }

        if (textActual.length >= 15 && textButton != ".") return

        if (textButton == ".") {
            if (!textActual.contains(".")) {
                binding.tvCurrent.text = if (textActual.isEmpty()) "0." else "$textActual."
            }
        } else {
            binding.tvCurrent.text = "$textActual$textButton"
        }
    }

    // --- CHANGE OPERATOR --- //

    @SuppressLint("SetTextI18n")
    fun changeOperator(v: View) {
        val operatorString = (v as? Button)?.text.toString().trim()
        val currentInput = binding.tvCurrent.text.toString()

        if (currentInput == "Error") return

        // --- PERCENTAGE --- //

        if (operatorString == "%") {
            val valor = currentInput.toDoubleOrNull() ?: return
            val resultPct =
                if (firstNumber.isNaN()) valor / 100
                else (firstNumber * valor) / 100

            showSafeResult(resultPct)
            return
        }

        // --- IF THERE IS NO NUMBER --- //

        if (currentInput.isEmpty()) {
            if (!firstNumber.isNaN()) {
                actualOperation = mapOperator(operatorString)
                binding.tvHistory.text =
                    "${formatDecimal.format(firstNumber)} $operatorString"
            }
            return
        }

        // --- IF THERE IS A ONGOING OPERATION --- //

        if (!firstNumber.isNaN()) {
            calculate()
        } else {
            firstNumber = currentInput.toDoubleOrNull() ?: return
        }

        actualOperation = mapOperator(operatorString)
        if (firstNumber.isNaN()) {
            binding.tvCurrent.text = "Error"
            actualOperation = ""
            return
        }

        binding.tvHistory.text =
            "${formatDecimal.format(firstNumber)} $operatorString"
        binding.tvCurrent.text = ""
    }

    private fun mapOperator(op: String): String = when (op) {
        "×" -> "*"
        "÷" -> "/"
        else -> op
    }


    // --- TO SHOW RESULT AS SCIENTIFIC NOTATION --- //
    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun showSafeResult(value: Double) {
        when {
            value.isNaN() || value.isInfinite() -> {
                binding.tvCurrent.text = "Error"
                firstNumber = Double.NaN
                actualOperation = ""
            }

            kotlin.math.abs(value) >= 1e15 ||
                    (kotlin.math.abs(value) < 1e-7 && value != 0.0) -> {
                binding.tvCurrent.text = String.format("%.5E", value)
            }

            else -> {
                binding.tvCurrent.text = formatDecimal.format(value)
            }
        }
    }

    // --- CALCULATE --- //
    fun calculate() {
        val inputText = binding.tvCurrent.text.toString()
        val valor = inputText.toDoubleOrNull() ?: return

        firstNumber = when (actualOperation) {
                "+" -> firstNumber + valor
                "-" -> firstNumber - valor
                "*" -> firstNumber * valor
                "/" -> if (valor != 0.0) firstNumber / valor else Double.NaN
                else -> valor
        }
    }

    // --- EQUAL BUTTON --- //
    @SuppressLint("SetTextI18n")
    fun equal(v: View) {
        val currentInput = binding.tvCurrent.text.toString()
        if (currentInput.isEmpty() || actualOperation.isEmpty()) return

        calculate()

        showSafeResult(firstNumber)
        binding.tvHistory.text = ""
        firstNumber = Double.NaN
        actualOperation = ""

    }

    // --- CLEAN AND CLEAN ALL BUTTONS --- //
    fun clean(v: View) {
        val textButton = (v as? Button)?.text?.toString()?.trim() ?: return
        if (textButton == "C") {
            val s = binding.tvCurrent.text.toString()
            if (s.isNotEmpty()) binding.tvCurrent.text = s.dropLast(1)
        } else { // CA - Clear All
            firstNumber = Double.NaN
            actualOperation = ""
            binding.tvCurrent.text = ""
            binding.tvHistory.text = ""
        }
    }
}
