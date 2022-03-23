package com.example.picturediary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.picturediary.GroupListAdapter.ViewHolder
import com.example.picturediary.navigation.model.ContentDTO
import com.example.picturediary.navigation.model.GroupDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.user_groups.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class GroupListAdapter(var context : Context, var items: ArrayList<GroupDTO>) : RecyclerView.Adapter<ViewHolder>() {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.groupId.text = item.grpid
        holder.groupName.text = item.grpname
        holder.creator.text = item.creator
        holder.groupTime.text = convertLongToTime(item.timestamp!!)
        holder.shareWith.text = item.shareWith.toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.user_groups, parent, false)
        return ViewHolder(inflatedView)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val groupId: TextView = v.groupId
        val groupName: TextView = v.groupName
        val creator: TextView = v.creator
        val groupTime: TextView = v.groupTime
        val shareWith: TextView = v.shareWith
    }

    // long 자료형 --> time 자료형
    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return format.format(date)
    }
}