package com.tsunetomo.raven.ui.search.search_options

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tsunetomo.raven.databinding.SearchOptionsViewBinding

class SearchOptionsView @JvmOverloads constructor(
    context: Context,
    attrSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrSet, defStyleAttr) {
    private val binding = SearchOptionsViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val adapter by lazy { SearchOptionsAdapter(::onOptionClick) }
    private var defaultOption: SearchOption? = null
    private val selectedOptions = mutableListOf<SearchOption>()
    var multipleChoiceEnabled = false

    init {
        binding.root.setOnClickListener {
            binding.rvOptions.isVisible = !binding.rvOptions.isVisible
            spinArrow(binding.rvOptions.isVisible)
        }
    }

    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun setAvailableOptions(
        options: List<SearchOption>,
        selected: List<SearchOption>,
        default: SearchOption? = null,
    ) {
        adapter.multipleChoiceEnabled = multipleChoiceEnabled
        binding.rvOptions.adapter = adapter
        binding.rvOptions.layoutManager = object : LinearLayoutManager(context) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                return super.checkLayoutParams(lp)
            }
        }

        defaultOption = default
        binding.tvChosen.text = defaultOption?.displayName
        adapter.submit(options, selected)
    }

    private fun onOptionClick(selectedOptions: List<SearchOption>) {
        val chosenText = if (selectedOptions.size in listOf(adapter.itemCount, 0)) {
            defaultOption?.displayName
        } else {
            selectedOptions.joinToString(",") { it.displayName }
        }
        binding.tvChosen.text = chosenText

        this.selectedOptions.clear()
        this.selectedOptions.addAll(selectedOptions)

        if (!multipleChoiceEnabled) {
            binding.rvOptions.isVisible = false
        }
    }

    private fun spinArrow(down: Boolean) {
        val values = if (down) floatArrayOf(0f, 90f) else floatArrayOf(90f, 0f)
        ValueAnimator.ofFloat(*values).apply {
            interpolator = LinearInterpolator()
            duration = 200L
            addUpdateListener {
                val rot = it.animatedValue as Float
                binding.ivArrow.rotation = rot
            }
            start()
        }
    }

    fun getSelectedOptions(): List<SearchOption> {
        return selectedOptions
    }

    fun hideOptions() {
        binding.rvOptions.isVisible = false
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearFilter() {
        selectedOptions.clear()
        adapter.submitSelected(selectedOptions)
        adapter.notifyDataSetChanged()
        binding.tvChosen.text = defaultOption?.displayName
        hideOptions()
    }
}
