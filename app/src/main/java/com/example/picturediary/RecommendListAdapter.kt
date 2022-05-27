package com.example.picturediary

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.example.picturediary.databinding.ItemRecommendedObjectBinding
import kotlin.collections.ArrayList


class RecommendListAdapter(var items: ArrayList<Int>) : RecyclerView.Adapter<RecommendListAdapter.ViewHolder>() {
    interface RecommendClickListener {
        fun onItemClick(position: Int)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecommendedObjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    private lateinit var mItemClickListener: RecommendClickListener

    fun setRecommendClickListener(itemClickListener: RecommendClickListener) {
        mItemClickListener = itemClickListener
    }

    inner class ViewHolder(private val binding: ItemRecommendedObjectBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                mItemClickListener.onItemClick(absoluteAdapterPosition)
            }
        }

        fun bind(item: Int) {
            binding.recommendView.setImageResource(item)
        }
    }
}