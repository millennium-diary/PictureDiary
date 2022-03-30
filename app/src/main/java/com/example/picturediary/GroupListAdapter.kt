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


class GroupListAdapter(var context : Context, var items: ArrayList<GroupDTO>) : RecyclerView.Adapter<ViewHolder>() {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bind(items[position])

        val item = items[position]

        holder.groupId.text = item.grpid
        holder.groupName.text = item.grpname
        holder.creator.text = item.creator
        holder.groupTime.text = item.timestamp.toString()
        holder.shareWith.text = item.shareWith.toString()

//        val listener = View.OnClickListener { it ->
//            Toast.makeText(it.context, "Clicked: " + item.grpname, Toast.LENGTH_SHORT).show()
//        }
//        holder.apply {
//            bind(listener, item)
//            itemView.tag = item
//        }
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

//        private var view : View = v

//        fun bind(item: GroupDTO) {
//            view.groupId.text = item.grpid
//            view.groupName.text = item.grpname
//            view.creator.text = item.creator
//            view.groupTime.text = item.timestamp.toString()
//            view.shareWith.text = item.shareWith.toString()

//            val auth = Firebase.auth
//            val firestore = FirebaseFirestore.getInstance()
//            val uid = auth.currentUser?.uid.toString()
//
//            firestore.collection("users").document(uid).get()
//                .addOnSuccessListener {
//                    val groupDTO = it.toObject(GroupDTO::class.java)
//                    groupName.text = groupDTO?.grpname
//                }
    }
}