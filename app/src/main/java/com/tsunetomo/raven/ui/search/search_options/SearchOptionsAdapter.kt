package com.tsunetomo.raven.ui.search.search_options

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.tsunetomo.raven.databinding.SearchOptionItemBinding

@SuppressLint("NotifyDataSetChanged")
class SearchOptionsAdapter(
    private val onClick: (List<SearchOption>) -> Unit,
) : RecyclerView.Adapter<SearchOptionsAdapter.VH>() {
    var multipleChoiceEnabled = false

    private val options = mutableListOf<SearchOption>()
    private val selectedOptions = mutableListOf<SearchOption>()

    fun submit(options: List<SearchOption>, selected: List<SearchOption>) {
        submitOptions(options)
        submitSelected(selected)
        notifyDataSetChanged()
    }

    fun submitOptions(options: List<SearchOption>) {
        this.options.clear()
        this.options.addAll(options)
    }

    fun submitSelected(selected: List<SearchOption>) {
        this.selectedOptions.clear()
        this.selectedOptions.addAll(selected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            SearchOptionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return options.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(position)
    }

    inner class VH(private val binding: SearchOptionItemBinding) : ViewHolder(binding.root) {
        private val regularButtonDrawable = binding.checkbox.buttonDrawable

        fun onBind(position: Int) {
            val item = options[position]
            binding.tvOption.text = item.displayName
            setSelected(selectedOptions.contains(item))

            itemView.setOnClickListener {
                if (multipleChoiceEnabled) {
                    if (selectedOptions.contains(item)) {
                        selectedOptions.remove(item)
                    } else {
                        selectedOptions.add(item)
                    }
                } else {
                    options.addAll(selectedOptions)
                    selectedOptions.clear()
                    selectedOptions.add(item)
                    options.remove(item)
                }


                notifyDataSetChanged()
                onClick(selectedOptions)
            }
        }

        private fun setSelected(selected: Boolean) {
            if (!multipleChoiceEnabled) {
                binding.checkbox.buttonDrawable = if (selected) {
                    regularButtonDrawable
                } else {
                    ColorDrawable(Color.TRANSPARENT)
                }
            }
            binding.checkbox.isChecked = selected
        }
    }
}
