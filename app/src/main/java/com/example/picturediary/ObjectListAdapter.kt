package com.example.picturediary

import android.graphics.BitmapFactory
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.example.picturediary.databinding.ItemChosenObjectBinding
import com.example.picturediary.navigation.model.ObjectDTO
import kotlin.collections.ArrayList


// CropActivity 파일에서 사용되는 어댑터
class ObjectListAdapter(var items: ArrayList<ObjectDTO>) : RecyclerView.Adapter<ObjectListAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChosenObjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemChosenObjectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ObjectDTO) {
            val bitmapWhole =
                BitmapFactory.decodeByteArray(item.drawObjWhole, 0, item.drawObjWhole!!.size)
            val bitmapOnly =
                BitmapFactory.decodeByteArray(item.drawObjOnly, 0, item.drawObjOnly!!.size)
            binding.objectParentId.text = item.fullDraw
            binding.objectId.text = item.objId.toString()
            binding.left.text = item.left.toString()
            binding.right.text = item.right.toString()
            binding.top.text = item.top.toString()
            binding.bottom.text = item.bottom.toString()
            binding.objectView.setImageBitmap(bitmapWhole)
            binding.objectViewOnly.setImageBitmap(bitmapOnly)
            binding.objectMotion.text = item.motion
        }
    }
}