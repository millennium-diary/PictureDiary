package com.example.picturediary

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.example.picturediary.databinding.ChosenObjectItemBinding
import com.example.picturediary.navigation.model.ObjectDTO
import kotlin.collections.ArrayList



class ObjectListAdapter(var items: ArrayList<ObjectDTO>) : RecyclerView.Adapter<ObjectListAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChosenObjectItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ChosenObjectItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ObjectDTO) {
            binding.objectView.setImageBitmap(item.drawObj)
            println("뭐야 " + item.drawObj)
            binding.objectMotion.text = item.motion
        }
    }
}