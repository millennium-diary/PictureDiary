package com.example.picturediary

import android.graphics.Bitmap
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.example.picturediary.databinding.ItemRecommendedObjectBinding
import kotlin.collections.ArrayList


class RecommendListAdapter(var items: ArrayList<Bitmap>) : RecyclerView.Adapter<RecommendListAdapter.ViewHolder>() {
    // 어댑터 아이템 선택했을 때 클릭 리스너 설정을 위한 인터페이스
    private lateinit var mItemClickListener: RecommendClickListener
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

    // 어댑터 아이템 선택했을 때 클릭 리스너 설정을 위한 함수
    fun setRecommendClickListener(itemClickListener: RecommendClickListener) {
        mItemClickListener = itemClickListener
    }

    inner class ViewHolder(private val binding: ItemRecommendedObjectBinding) : RecyclerView.ViewHolder(binding.root) {
        // 어댑터 아이템 선택했을 때 클릭 리스너 설정
        init {
            itemView.setOnClickListener {
                mItemClickListener.onItemClick(absoluteAdapterPosition)
            }
        }

        fun bind(item: Bitmap) {
            binding.recommendView.setImageBitmap(item)
        }
    }
}