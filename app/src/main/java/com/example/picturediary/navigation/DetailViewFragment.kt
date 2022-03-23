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
import com.example.picturediary.GroupListAdapter
import com.example.picturediary.navigation.model.GroupDTO
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlin.collections.ArrayList


class DetailViewFragment : Fragment() {
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private lateinit var groupArrayList: ArrayList<GroupDTO>
    private lateinit var groupListAdapter: GroupListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_detail, container, false)

        groupArrayList = arrayListOf()
        groupListAdapter = GroupListAdapter(requireContext(), groupArrayList)
        view.detailRecycler.adapter = groupListAdapter
        view.detailRecycler.layoutManager = LinearLayoutManager(activity)

        EventChangeListener()

        // 그룹 추가 버튼
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

    private fun EventChangeListener() {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val username = auth?.currentUser?.email.toString().replace("@fake.com", "")

        firestore?.collection("groups")
            ?.whereArrayContains("shareWith", username)
            ?.addSnapshotListener(object : EventListener<QuerySnapshot>{
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        println("EventChangeListener error : " + error.message.toString())
                        return
                    }

                    for (dc : DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            groupArrayList.add(dc.document.toObject(GroupDTO::class.java))
                            groupListAdapter.notifyDataSetChanged()
                        }
                        else if (dc.type == DocumentChange.Type.REMOVED) {
                            groupArrayList.remove(groupArrayList[0])
                            groupListAdapter.notifyDataSetChanged()
                        }
                    }
                }
            })
    }

    private fun addToGroup(grpname: String) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val username = auth?.currentUser?.email.toString().replace("@fake.com", "")

        // 그룹명이 비어있을 경우
        if (grpname.isBlank())
            Toast.makeText(activity, "그룹명을 다시 확인해 주세요", Toast.LENGTH_SHORT).show()

        // 그룹명을 제대로 입력했을 경우
        else {
            // 그룹 형식에 정보 입력
            val groupDTO = GroupDTO()
            groupDTO.grpid = "$grpname@$username"
            groupDTO.grpname = grpname
            groupDTO.creator = username
            groupDTO.timestamp = System.currentTimeMillis()
            groupDTO.shareWith = arrayListOf(groupDTO.creator.toString())

            // 그룹 데이터베이스 확인
            firestore?.collection("groups")
                ?.document("$grpname@$username")?.get()
                ?.addOnCompleteListener { task ->
                    val document = task.result
                    val group = document["grpid"]

                    // 그룹 데이터베이스에 이미 존재
                    if (group == "$grpname@$username")
                        Toast.makeText(activity, "이미 존재하는 그룹입니다", Toast.LENGTH_SHORT).show()

                    // 그룹 데이터베이스에 없으면
                    else {
                        // 그룹 데이터베이스에 추가
                        firestore!!.collection("groups")
                            .document("$grpname@$username")
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
        val username = auth?.currentUser?.email.toString().replace("@fake.com", "")

        // 사용자 데이터베이스에 추가
        firestore?.collection("users")
            ?.document(username)?.get()
            ?.addOnCompleteListener { task ->
                val document = task.result
                var userGroup = document["userGroups"] as ArrayList<String>?

                if (userGroup.isNullOrEmpty())
                    userGroup = arrayListOf("$grpname@$username")
                else
                    userGroup.add("$grpname@$username")

                firestore!!.collection("users")
                    .document(username)
                    .update("userGroups", userGroup)
            }

        Toast.makeText(activity, "$grpname 그룹을 생성했습니다", Toast.LENGTH_SHORT).show()
    }
}

