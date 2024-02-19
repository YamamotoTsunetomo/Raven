package com.tsunetomo.raven.ui.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tsunetomo.raven.R
import com.tsunetomo.raven.databinding.BookItemBinding
import com.tsunetomo.raven.model.Book

@SuppressLint("NotifyDataSetChanged")
class BooksAdapter(private val onClick: (Book) -> Unit) : RecyclerView.Adapter<BooksAdapter.VH>() {
    private val books = mutableListOf<Book>()

    fun submit(items: List<Book>) {
        books.clear()
        books.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(BookItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return books.size
    }

    inner class VH(private val binding: BookItemBinding) : ViewHolder(binding.root) {
        fun onBind(position: Int) {
            val book = books[position]
            val context = binding.root.context

            val color = ContextCompat.getColor(context, placeholderColors.random())
            binding.ivCover.setBackgroundColor(color)

            Glide.with(context)
                .load(book.image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.ivCover)

            binding.tvTitle.text = book.title
            binding.tvAuthor.text = book.author
            binding.tvFileType.text = book.extension

            itemView.setOnClickListener { onClick(book) }
        }
    }

    companion object {
        private val placeholderColors = listOf(
            R.color.isabelline,
            R.color.timberwolf,
            R.color.linen,
            R.color.champagne_pink,
            R.color.pale_dogwood
        )
    }
}