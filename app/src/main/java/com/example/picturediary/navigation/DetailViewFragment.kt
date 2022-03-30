package com.example.picturediary.navigation

import android.content.*
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.picturediary.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.picturediary.GroupListAdapter
import com.example.picturediary.navigation.model.GroupDTO
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_detail.view.*


class DetailViewFragment : Fragment() {
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private lateinit var detailRecycler: RecyclerView
    private lateinit var groupArrayList: ArrayList<GroupDTO>
//    private lateinit var groupListAdapter: GroupListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_detail, container, false)

        groupArrayList = arrayListOf()
        groupArrayList.add(GroupDTO("안녕@park", "안녕", "park", 12345, arrayListOf("park"), null))
        view.detailRecycler.adapter = GroupListAdapter(requireContext(), groupArrayList)
        view.detailRecycler.layoutManager = LinearLayoutManager(activity)

//        firestore?.collection("groups")
//            ?.orderBy("timestamp", Query.Direction.ASCENDING)
//            ?.addSnapshotListener { querySnapshot, exception ->
//                if (querySnapshot != null) {
//                    for (dc in querySnapshot.documentChanges) {
//                        if (dc.type == DocumentChange.Type.ADDED) {
//                            var firebaseMessage = dc.document.toObject(GroupDTO::class.java)
//                            firebaseMessage.grpid = dc.document.id
//                        }
//                    }
//                }
//            }

        view.add_grp.setOnClickListener { _ ->
            var grpname: String

            // 팝업 설정
            val dlg = AlertDialog.Builder(requireActivity())
            val input = EditText(requireActivity())
            input.inputType = InputType.TYPE_CLASS_TEXT
            dlg.setTitle("생성할 그룹의 이름을 작성하세요")
            dlg.setView(input)
            dlg.setPositiveButton("확인", DialogInterface.OnClickListener { _, _ ->
                grpname = input.text.toString()
                addToGroup(grpname)
            })
            dlg.show()
        }

        return view
    }

    private fun addToGroup(grpname: String) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val uid = auth?.currentUser?.uid.toString()

        // 그룹명이 비어있을 경우
        if (grpname.isBlank())
            Toast.makeText(activity, "그룹명을 다시 확인해 주세요", Toast.LENGTH_SHORT).show()

        // 그룹명을 제대로 입력했을 경우
        else {
            // 그룹 형식에 정보 입력
            val groupDTO = GroupDTO()
            groupDTO.grpid = "$grpname@$uid"
            groupDTO.grpname = grpname
            groupDTO.creator = auth?.currentUser?.uid
            groupDTO.timestamp = System.currentTimeMillis()
            groupDTO.shareWith = arrayListOf(groupDTO.creator.toString())

            // 그룹 데이터베이스 확인
            firestore?.collection("groups")
                ?.document("$grpname@$uid")?.get()
                ?.addOnCompleteListener { task ->
                    val document = task.result
                    val group = document["grpid"]

                    // 그룹 데이터베이스에 이미 존재
                    if (group == "$grpname@$uid")
                        Toast.makeText(activity, "이미 존재하는 그룹입니다", Toast.LENGTH_SHORT).show()

                    // 그룹 데이터베이스에 없으면
                    else {
                        // 그룹 데이터베이스에 추가
                        firestore!!.collection("groups")
                            .document("$grpname@$uid")
                            .set(groupDTO)

                        // 사용자 데이터베이스에 추가
                        addToUserGroups(grpname)
                    }
                }
        }
    }

    // 사용자 데이터베이스에 추가
    private fun addToUserGroups(grpname: String) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val userEmail = auth?.currentUser?.email.toString()
        val uid = auth?.currentUser?.uid.toString()

        // 사용자 데이터베이스에 추가
        firestore?.collection("users")
            ?.document(userEmail)?.get()
            ?.addOnCompleteListener { task ->
                val document = task.result
                var userGroup = document["userGroups"] as ArrayList<String>?

                if (userGroup.isNullOrEmpty())
                    userGroup = arrayListOf("$grpname@$uid")
                else
                    userGroup.add("$grpname@$uid")

                firestore!!.collection("users")
                    .document(userEmail)
                    .update("userGroups", userGroup)
            }

        Toast.makeText(activity, "$grpname 그룹을 생성했습니다", Toast.LENGTH_SHORT).show()
    }

}

