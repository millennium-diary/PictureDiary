package com.example.picturediary

import android.content.Context
import android.graphics.BitmapFactory
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.example.picturediary.databinding.ChosenObjectItemBinding
import com.example.picturediary.navigation.dao.DBHelper
import com.example.picturediary.navigation.model.ObjectDTO
import kotlinx.android.synthetic.main.chosen_object_item.view.*
import kotlin.collections.ArrayList


class ObjectListAdapter(var items: ArrayList<ObjectDTO>) : RecyclerView.Adapter<ObjectListAdapter.ViewHolder>() {
//    private var onClickListener: OnClickListener? = null
//
//    interface OnClickListener {
//        fun onClick(position: Int)
//    }
//
//    fun setOnClickListener(onClickListener: OnClickListener) {
//        this.onClickListener = onClickListener
//    }

    private fun removeItem(context: Context, drawId: String, objId: String, position: Int) {
        val dbName = "pictureDiary.db"
        val dbHelper = DBHelper(context, dbName, null, 1)

        val objectDTO = dbHelper.readSingleObject(drawId, objId)

        items.remove(objectDTO)
        notifyItemRemoved(position)
        dbHelper.deleteObject(drawId, objId)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])

        val context = holder.itemView.context
        val drawId = items[position].fullDraw.toString()
        val objId = items[position].objId.toString()

        holder.itemView.delete_object.setOnClickListener {
            val cropView = CropView(context, )
//            onClickListener?.onClick(position)
//            println("하하하 $position")
//            removeItem(context, drawId, objId, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChosenObjectItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ChosenObjectItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ObjectDTO) {
            val bitmap = BitmapFactory.decodeByteArray(item.drawObj, 0, item.drawObj!!.size)
            binding.fullDrawId.text = item.fullDraw
            binding.objectId.text = item.objId.toString()
            binding.objectView.setImageBitmap(bitmap)
            binding.objectMotion.text = item.motion
        }
    }
}