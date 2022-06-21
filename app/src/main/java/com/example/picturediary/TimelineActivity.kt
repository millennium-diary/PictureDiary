package com.example.picturediary


import android.content.DialogInterface
import android.media.MediaPlayer.OnPreparedListener
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.picturediary.navigation.model.ContentDTO
import com.example.picturediary.navigation.model.GroupDTO
import com.example.picturediary.navigation.model.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_timeline.*
import kotlinx.android.synthetic.main.item_timeline.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TimelineActivity : AppCompatActivity() {
    private val utils = Utils()
    var firestore: FirebaseFirestore? = null
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        // 파이어스토어 인스턴스 초기화
        firestore = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser

        recyclerViewtimeline.adapter = RecyclerViewAdapter()
        recyclerViewtimeline.layoutManager = LinearLayoutManager(this)

        // 멤버 추가 버튼 클릭 시
        add_member.setOnClickListener {
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
                            Toast.makeText(
                                applicationContext,
                                "사용자명을 다시 확인해 주세요",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    // 사용자가 존재하지 않을 경우
                    else if (!utils.userExists(memberName)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(applicationContext, "존재하지 않는 사용자입니다", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    // 가입된 사용자일 경우
                    else if (utils.userExists(memberName)) {
                        val groupId = intent.getStringExtra("GroupID")

                        // 사용자가 이미 그룹에 존재할 경우
                        if (utils.userExistsInGroup(groupId!!, memberName)) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    applicationContext,
                                    "그룹에 이미 존재하는 사용자입니다",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        // 사용자가 그룹에 없을 경우
                        else {
                            utils.addToUserGroups(groupId, memberName)
                            utils.addToShareWith(groupId, memberName)
                            withContext(Dispatchers.Main) {
                                val groupName = groupId.split("@")[0]
                                Toast.makeText(
                                    applicationContext,
                                    "$memberName 님이 $groupName 그룹에 추가되었습니다",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            })
            dlg.show()
        }
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        // Diary 클래스 ArrayList 생성
        var contentDTOs: ArrayList<ContentDTO> = ArrayList()
        var contentUidList: ArrayList<String> = ArrayList()
        private val firestore = FirebaseFirestore.getInstance()
        private val groupId = intent.getStringExtra("GroupID")

        init {
            if (groupId != null) {
                firestore.collection("groups")
                    .document(groupId)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val groupDTO = task.result.toObject(GroupDTO::class.java)
                            if (groupDTO?.shareWith != null) {
                                getContents(groupDTO.shareWith!!)
                            }
                        }
                    }
            }
        }

        // 파이어베이스에서 그룹에 맞는 contentDTO를 불러옴
        private fun getContents(shareWith: ArrayList<String>?) {
            // 파이어베이스에서 가져온 컨텐츠들을 시간 내림차순으로 정렬함
            firestore.collection("contents")
                .whereEqualTo("explain", "확인")
//                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { querySnapshot, _ ->
                    // ArrayList 비워줌
                    contentDTOs.clear()
                    contentUidList.clear()

                    if (querySnapshot == null) return@addSnapshotListener
                    for (snapshot in querySnapshot.documents) {
                        val item = snapshot.toObject(ContentDTO::class.java)
                        if (item != null) {
                            if (shareWith != null) {
                                if (shareWith.contains(item.username))
                                    if (groupId == item.groupId.toString())
                                        contentDTOs.add(item)
                            }
                            contentUidList.add(snapshot.id)
                        }
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(p0.context).inflate(R.layout.item_timeline, p0, false)
            return ViewHolder(view)
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {}

        // onCreateViewHolder에서 만든 view와 실제 데이터를 연결
        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var videoPlaying = false
            val viewHolder = (p0 as ViewHolder).itemView

            firestore.collection("users")
                .document(contentDTOs[p1].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result.toObject(UserDTO::class.java)?.imageUrl
                        if (url == "") {
                            viewHolder.detailviewitem_profile_image.setImageResource(R.drawable.user)
                        } else {
                            Glide.with(p0.itemView.context)
                                .load(url)
                                .apply(RequestOptions().circleCrop())
                                .into(viewHolder.detailviewitem_profile_image)
                        }
                    }
                }
            viewHolder.profile_textview.text = contentDTOs[p1].username
            viewHolder.explain_textview.text = contentDTOs[p1].explain

            //현재 사용자가 해당 일기 작성자라면, 삭제 버튼 표시
            firestore.collection("users")
                .document(user?.uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userDTO = task.result.toObject(UserDTO::class.java)
                        if (userDTO?.username ==contentDTOs[p1].username)
                            viewHolder.delete_picture.visibility=View.VISIBLE
                        else
                            viewHolder.delete_picture.visibility=View.INVISIBLE
                    }
                }

//            Glide.with(p0.itemView.context)
//                .load(contentDTOs[p1].imageUrl)
//                .into(viewHolder.Diary_image)

            viewHolder.Diary_image.setOnClickListener {
                viewHolder.Diary_image.setVideoPath(contentDTOs[p1].imageUrl)
                if (!videoPlaying) {
                    videoPlaying = true
                    viewHolder.Diary_image.setOnPreparedListener{ it.isLooping = true }
                    viewHolder.Diary_image.start()
                }
                else {
                    videoPlaying = false
                    viewHolder.Diary_image.pause()
                    viewHolder.Diary_image.stopPlayback()
                }
            }

            // 좋아요 누르기
            viewHolder.favorite_imageview.setOnClickListener { favoriteEvent(p1) }

            // 날짜 적기
            viewHolder.diary_date.text = contentDTOs[p1].diaryDate

            if (contentDTOs[p1].favorites.containsKey(FirebaseAuth.getInstance().currentUser!!.uid)) {
                // 클릭 되었을 경우
                viewHolder.favorite_imageview.setImageResource(R.drawable.ic_favorite)

            } else {
                // 클릭이 되지 않았을 경우
                viewHolder.favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }

            viewHolder.like_number.text = "좋아요 " + contentDTOs[p1].favoriteCount + "개"
            viewHolder.delete_picture.setOnClickListener {
                deleteDiary(contentDTOs[p1].contentId)
            }
        }

        private fun deleteDiary(ContentID: String?) {
            if (ContentID != null) {
                firestore.collection("contents")
                    .document(ContentID)
                    .delete()
            }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        // 좋아요 이벤트
        private fun favoriteEvent(position: Int) {
            val tsDoc = firestore.collection("contents")
                .document(contentUidList[position])
            firestore.runTransaction { transaction ->

                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                val contentDTO = transaction.get(tsDoc).toObject(ContentDTO::class.java)

                // 좋아요를 취소할 경우 좋아요 수를 줄이고 유저 목록에서 삭제함
                if (contentDTO!!.favorites.containsKey(uid)) {
                    contentDTO.favoriteCount = contentDTO.favoriteCount - 1
                    contentDTO.favorites.remove(uid)
                    
                // 좋아요를 누를 경우, 좋아요 수를 늘리고 유저 목록에 추가함
                } else {
                    contentDTO.favoriteCount = contentDTO.favoriteCount + 1
                    contentDTO.favorites[uid] = true
                }
                transaction.set(tsDoc, contentDTO)
            }
        }
    }
}






