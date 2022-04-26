package com.example.picturediary

import android.graphics.BitmapFactory
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
            val bitmap = BitmapFactory.decodeByteArray(item.drawObj, 0, item.drawObj!!.size)
            binding.objectParentId.text = item.fullDraw
            binding.objectId.text = item.objId.toString()
            binding.objectView.setImageBitmap(bitmap)
            binding.objectMotion.text = item.motion
        }
    }
}