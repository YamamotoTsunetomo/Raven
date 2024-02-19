package com.tsunetomo.raven.ui.details.progress_button

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.tsunetomo.raven.R
import com.tsunetomo.raven.databinding.ProgressButtonViewBinding

class ProgressButtonView @JvmOverloads constructor(
    context: Context,
    attrSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrSet, defStyleAttr) {
    private val binding = ProgressButtonViewBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    var buttonModeText: String? = null
    var progressBarModeText: String? = null

    fun setProgress(progress: Float) {
        binding.progressGuideline.setGuidelinePercent(progress)
    }

    fun setText(text: String) {
        binding.button.text = text
    }

    fun switchMode(buttonMode: Boolean) = with(binding) {
        progress.isVisible = !buttonMode
        button.setBackgroundResource(if (buttonMode) R.drawable.search_button_selector else R.drawable.progress_bar_shape)
        button.text = if (buttonMode) buttonModeText else progressBarModeText
        setProgress(0f)
    }

    fun addClickListener(f: () -> Unit) {
        binding.button.setOnClickListener { f() }
    }
}
