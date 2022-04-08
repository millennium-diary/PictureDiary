package com.example.picturediary


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.picturediary.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.timeline_detail.view.*
import kotlinx.android.synthetic.main.group_timeline.*
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.user_group_item.*
import kotlinx.android.synthetic.main.user_group_item.view.*

class DiarytimelineActivity : AppCompatActivity() {

    var firestore: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_timeline)

        //파이어스토어 인스턴스 초기화
        firestore = FirebaseFirestore.getInstance()

        recyclerViewtimeline.adapter = RecyclerViewAdapter()
        recyclerViewtimeline.layoutManager = LinearLayoutManager(this)

    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        // Diary 클래스 ArrayList 생성성
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        val groupId = intent.getStringExtra("groupID")

        init {
//            firestore?.collection("images")?.orderBy("timestamp")
//                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                    // ArrayList 비워줌
//                    contentDTOs.clear()
//                    contentUidList.clear()

            firestore?.collection("images")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    // ArrayList 비워줌
                    contentDTOs.clear()
                    contentUidList.clear()

                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.timeline_detail, p0, false)
            return ViewHolder(view)
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        }

        // onCreateViewHolder에서 만든 view와 실제 데이터를 연결
        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewHolder = (p0 as ViewHolder).itemView

            viewHolder.profile_textview.text=contentDTOs!![p1].username

            viewHolder.explain_textview.text=contentDTOs!![p1].explain

            Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUrl).into(viewHolder.Diary_image)

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
    }


}