package com.example.picturediary


import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.picturediary.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.timeline_detail.view.*
import kotlinx.android.synthetic.main.group_timeline.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.picturediary.navigation.model.GroupDTO
import com.example.picturediary.navigation.model.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.user_group_item.*
import kotlinx.android.synthetic.main.user_group_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiarytimelineActivity : AppCompatActivity() {
    private val utils = Utils()
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    var user: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_timeline)

        //파이어스토어 인스턴스 초기화
        firestore = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser

        recyclerViewtimeline.adapter = RecyclerViewAdapter()
        recyclerViewtimeline.layoutManager = LinearLayoutManager(this)

        add_memeber.setOnClickListener {
            var memberName: String

            // 팝업 설정
            val dlg = AlertDialog.Builder(this)
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            dlg.setTitle("추가할 멤버의 아이디를 입력하세요")
            dlg.setView(input)
            dlg.setPositiveButton("확인", DialogInterface.OnClickListener { _, _ ->
                memberName = input.text.toString()
                GlobalScope.launch(Dispatchers.IO) {
                    // 사용자명이 비어있을 경우
                    if (memberName.isBlank()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(applicationContext, "사용자명을 다시 확인해 주세요", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // 사용자가 이미 존재할 경우
                    else if (utils.userExists(memberName)) {
                        val groupId = intent.getStringExtra("GroupID")
                        // 사용자가 이미 그룹에 존재할 경우
                        if (utils.userExistsInGroup(groupId!!, memberName)) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "그룹에 이미 존재하는 사용자입니다", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else {
                            utils.addToUserGroups(groupId, memberName)
                            utils.addToShareWith(groupId, memberName)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "$memberName 님이 $groupId 그룹에 추가되었습니다", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    // 사용자가 존재하지 않을 경우
                    else if (!utils.userExists(memberName)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(applicationContext, "존재하지 않는 사용자입니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
            dlg.show()
        }
    }

//    private fun addMember(memberName: String) {
//        auth = Firebase.auth
//        val uid = auth?.currentUser?.uid.toString()
//        val groupId = intent.getStringExtra("GroupID")

//        var tsDocGroup = groupId?.let { firestore?.collection("groups")?.document(it) }
//        firestore?.runTransaction { transaction ->
//
//            var groupDTO = tsDocGroup?.let { transaction.get(it).toObject(GroupDTO::class.java) }
//
//
//            if (groupDTO?.shareWith?.contains(memberName)!!) {
//                    Toast.makeText(applicationContext,"이미 있는 멤버입니다",Toast.LENGTH_SHORT).show()
//            }
//            else {
//                    groupDTO?.shareWith!!.add(memberName)
//                    Toast.makeText(applicationContext,"멤버를 추가하였습니다.",Toast.LENGTH_SHORT).show()
//            }
//
//            transaction.set(tsDocGroup!!, groupDTO!!)
//            return@runTransaction
//
//        }
//    }


    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        // Diary 클래스 ArrayList 생성성
        var contentDTOs: ArrayList<ContentDTO>
        var contentUidList: ArrayList<String>

        val groupId = intent.getStringExtra("GroupID")
        var uid = FirebaseAuth.getInstance().currentUser?.uid

        init {
            contentDTOs = ArrayList()
            contentUidList = ArrayList()
            if (groupId != null) {

                firestore?.collection("groups")?.document(groupId)?.get()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            var GroupDTO = task.result.toObject(GroupDTO::class.java)
                            if (GroupDTO?.shareWith != null) {
                                getCotents(GroupDTO.shareWith!!)
                            }
                        }
                    }
            }

        }

        private fun getCotents(shareWith: ArrayList<String>?) {
            firestore?.collection("images")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    // ArrayList 비워줌
                    contentDTOs.clear()
                    contentUidList.clear()

                    if (querySnapshot == null) return@addSnapshotListener
                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        if (item != null) {
                            if (shareWith != null) {
                                if (shareWith.contains(item.username)==true)
                                    if(groupId==item.groupId.toString())
                                        contentDTOs.add(item)
                            }
                            contentUidList.add(snapshot.id)
                        }

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


            firestore?.collection("users")?.document(contentDTOs[p1].uid!!)
                ?.get()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val url = task.result.toObject(UserDTO::class.java)?.imageUrl
                        Glide.with(p0.itemView.context)
                            .load(url)
                            .apply(RequestOptions().circleCrop())
                            .into(viewHolder.detailviewitem_profile_image)
                    }
                }
            viewHolder.profile_textview.text = contentDTOs!![p1].username

            viewHolder.explain_textview.text = contentDTOs!![p1].explain

            Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUrl)
                .into(viewHolder.Diary_image)

            viewHolder.favorite_imageview.setOnClickListener { favoriteEvent(p1) }

            if (contentDTOs[p1].favorites.containsKey(FirebaseAuth.getInstance().currentUser!!.uid)) {
                //클릭 되었을 경우
                viewHolder.favorite_imageview.setImageResource(R.drawable.ic_favorite)

            } else {
                //클릭이 되지 않았을 경우
                viewHolder.favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }
            viewHolder.like_number.text = "좋아요 " + contentDTOs[p1].favoriteCount + "개"

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        private fun favoriteEvent(position: Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->

                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                val contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if (contentDTO!!.favorites.containsKey(uid)) {
                    // Unstar the post and remove self from stars
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! - 1
                    contentDTO?.favorites?.remove(uid)

                } else {
                    // Star the post and add self to stars
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount!! + 1
                    contentDTO?.favorites?.set(uid, true)
                }
                transaction.set(tsDoc, contentDTO)
            }

        }
    }
}






