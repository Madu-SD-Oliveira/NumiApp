package com.example.numiapp

import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.numiapp.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // INFLATES THE LAYOUT AND SETS THE BINDING.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()

        binding.btnNext.setOnClickListener {
            val intent = Intent(this, BodyActivity::class.java)
            startActivity(intent)
        }

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        // PREPARES.
        prepareViewsForAnimation()

        // INITIALIZES.
        lifecycleScope.launch {
            startAnimationSequence()
        }
    }

    private fun prepareViewsForAnimation() {
        binding.tvTitleMain.visibility = View.INVISIBLE
        binding.tvSlogan.visibility = View.INVISIBLE
        binding.btnNext.visibility = View.INVISIBLE
    }

    /**
     * DEFINES THE ANIMATION SEQUENCE WITH COROUTINES.
     */
    private suspend fun startAnimationSequence() {
        // 1. ANIMATION TITLE FADE-IN.
        binding.tvTitleMain.fadeIn(1500)
        applyNeonPulse(binding.tvTitleMain)
        delay(1500)

        // 2. ANIMATION SLOGAN LETTER-BY-LETTER.
        val sloganText = getString(R.string.slogan)
        binding.tvSlogan.fadeInLetterByLetter(sloganText, 100)
        delay(sloganText.length * 100L + 500)

        // 3. ANIMATION SLIDE-IN BUTTON.
        binding.btnNext.slideInFromBottom(1000)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    // --- EXTENSION FUNCTIONS ---

    /**
     * APPLIES NEON-PULSE EFFECT TO THE TextView.
     */
    private fun applyNeonPulse(textView: TextView) {
        val neonColor = "#37A1C4".toColorInt()
        val animator = ValueAnimator.ofFloat(5f, 25f).apply {
            duration = 1000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                textView.setShadowLayer(animatedValue, 0f, 0f, neonColor)
            }
        }
        animator.start()
    }

    /**
     * ANIMATES VIEW TO SHOW (fade-in) IN THE FIXED POSITION.
     */
    private fun View.fadeIn(duration: Long) {
        this.alpha = 0f
        this.visibility = View.VISIBLE
        this.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    /**
     * ANIMATES TextView LETTER BY LETTER.
     */
    private fun TextView.fadeInLetterByLetter(text: CharSequence, durationPerLetter: Long) {
        this.visibility = View.VISIBLE
        this.text = text
        this.alpha = 1f

        // CREATE A ValueAnimator THAT GOES FROM 0 TO THE LENGTH OF THE TEXT.
        val animator = ValueAnimator.ofInt(0, text.length)
        animator.duration = (text.length * durationPerLetter)
        animator.interpolator = DecelerateInterpolator()

        animator.addUpdateListener {
            val lastVisibleCharIndex = it.animatedValue as Int
            // USES A Spannable TO CHANGE THE OPACITY OF EACH CHARACTER.
            val spannable = android.text.SpannableStringBuilder(text)
            for (i in text.indices) {
                val alpha = if (i <= lastVisibleCharIndex) 1f else 0f
                spannable.setSpan(
                    android.text.style.ForegroundColorSpan(Color.argb((alpha * 255).toInt(), 80, 74, 79)),
                    i, i + 1,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            this.text = spannable
        }
        animator.start()
    }


    /**
     * BUTTON SLIDE-IN ANIMATION.
     */
    private fun View.slideInFromBottom(duration: Long) {
        this.alpha = 0f
        this.translationY = 100f
        this.visibility = View.VISIBLE

        this.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
}
