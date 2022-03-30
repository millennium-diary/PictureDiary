package com.example.picturediary


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.group_timeline.*
import kotlinx.android.synthetic.main.timeline_detail.view.*

class DiarytimelineActivity : AppCompatActivity() {

    var firestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_timeline)

        //파이어스토어 인스턴스 초기화
        firestore = FirebaseFirestore.getInstance()

        val recyclerview : RecyclerView=findViewById(R.id.recyclerViewtimeline)
        recyclerview.adapter = RecyclerViewAdapter()
        recyclerview.layoutManager = LinearLayoutManager(this)
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        // Diary 클래스 ArrayList 생성성
        var ImageDiary: ArrayList<Diary> = arrayListOf()

        init {
            firestore?.collection("telephoneBook")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    // ArrayList 비워줌
                    ImageDiary.clear()

                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(Diary::class.java)
                        ImageDiary.add(item!!)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.timeline_detail, parent, false)
            return ViewHolder(view)
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        }

        // onCreateViewHolder에서 만든 view와 실제 데이터를 연결
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as ViewHolder).itemView

            viewHolder.profile_textview.text = ImageDiary[position].name
            viewHolder.like_number.text = ImageDiary[position].like.toString()
            viewHolder.like_number.text = ImageDiary[position].image
        }

        override fun getItemCount(): Int {
            return ImageDiary.size
        }
    }


}