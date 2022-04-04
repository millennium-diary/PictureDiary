package com.example.picturediary

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.picturediary.GroupListAdapter.ViewHolder
import com.example.picturediary.databinding.UserGroupItemBinding
import com.example.picturediary.navigation.model.GroupDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class GroupListAdapter(var items: ArrayList<GroupDTO>) : RecyclerView.Adapter<ViewHolder>() {
    interface ItemClickListener {
        fun onClick(view: View, position: Int)
    }

    //클릭리스너 선언
    private lateinit var itemClickListener: ItemClickListener

    //클릭리스너 등록 매소드
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DiarytimelineActivity::class.java)
            ContextCompat.startActivity(holder.itemView.context, intent, null)
//            itemClickListener.onClick(it, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = UserGroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: UserGroupItemBinding) : RecyclerView.ViewHolder(binding.root) {

        // long 자료형 --> time 자료형
        private fun convertLongToTime(time: Long): String {
            val date = Date(time)
            val format = SimpleDateFormat("yyyy.MM.dd")
            return format.format(date)
        }

        fun bind(item: GroupDTO) {
            binding.groupId.text = item.grpid
            binding.groupName.text = item.grpname
            binding.leader.text = item.leader
            binding.groupTime.text = convertLongToTime(item.timestamp!!)
            binding.shareWith.text = item.shareWith.toString()
        }
    }
}