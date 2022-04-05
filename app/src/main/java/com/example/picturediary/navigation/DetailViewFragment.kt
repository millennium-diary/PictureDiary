package com.example.picturediary.navigation

import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.picturediary.GroupListAdapter
import com.example.picturediary.GroupSwipeHelperCallback
import com.example.picturediary.R
import com.example.picturediary.navigation.model.GroupDTO
import com.example.picturediary.navigation.model.UserDTO
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.function.Predicate


class DetailViewFragment : Fragment() {
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private lateinit var groupArrayList: ArrayList<GroupDTO>
    private lateinit var groupListAdapter: GroupListAdapter

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val username = auth?.currentUser?.displayName.toString()

        val view: View = inflater.inflate(R.layout.fragment_detail, container, false)

        groupArrayList = arrayListOf()
        groupListAdapter = GroupListAdapter(groupArrayList)

        val swipeHelperCallback = GroupSwipeHelperCallback(requireContext(), view.detailRecycler).apply {
            setClamp(200f)
        }
        val itemTouchHelper = ItemTouchHelper(swipeHelperCallback)
        itemTouchHelper.attachToRecyclerView(view.detailRecycler)

        view.detailRecycler.apply {
            view.detailRecycler.layoutManager = LinearLayoutManager(activity)
            view.detailRecycler.adapter = groupListAdapter

            setOnTouchListener { _, _ ->
                swipeHelperCallback.removePreviousClamp(this)
                false
            }
        }

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

        // 어댑터에 변화가 생기면 바로 적용
        eventChangeListener(view, this)

        return view
    }

    private fun eventChangeListener(view: View, fragment: Fragment) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val username = auth?.currentUser?.displayName.toString()
        val swipeHelperCallback = GroupSwipeHelperCallback(requireContext(), view.detailRecycler)

        firestore!!.collection("groups")
            .whereArrayContains("shareWith", username)
            .addSnapshotListener(object : EventListener<QuerySnapshot>{
                @SuppressLint("NotifyDataSetChanged")
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        println("EventChangeListener error : " + error.message.toString())
                        return
                    }

                    for (dc : DocumentChange in value?.documentChanges!!) {
                        val grpid = dc.document.toObject(GroupDTO::class.java).grpid
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                println("added $grpid")
                                println("3355 $groupArrayList")
                                groupArrayList.add(dc.document.toObject(GroupDTO::class.java))
                                println("3366 $groupArrayList")
                                groupListAdapter.notifyDataSetChanged()
                                swipeHelperCallback.removePreviousClamp(view.detailRecycler)
                            }
                            DocumentChange.Type.REMOVED -> {
                                val data = dc.document.toObject(GroupDTO::class.java)
                                if (data.leader == username) {
                                    val shareWith = arrayListOf(username)
                                    data.shareWith = shareWith
                                    println("33333 $data")
                                    println("33333 $groupArrayList")
                                    groupArrayList.remove(data)
                                    println("33333 $groupArrayList")
                                }
                                else groupArrayList.remove(data)

                                groupListAdapter.notifyDataSetChanged()
                                swipeHelperCallback.removePreviousClamp(view.detailRecycler)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                println("modified $grpid")
                                groupListAdapter.notifyDataSetChanged()
                                swipeHelperCallback.removePreviousClamp(view.detailRecycler)
                            }
                        }
                    }
                }
            })
    }

    // groups 컬렉션에 그룹 추가
    private fun addToGroup(grpname: String) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val username = auth?.currentUser?.displayName.toString()

        // 그룹명이 비어있을 경우
        if (grpname.isBlank())
            Toast.makeText(activity, "그룹명을 다시 확인해 주세요", Toast.LENGTH_SHORT).show()

        // 그룹명을 제대로 입력했을 경우
        else {
            // 그룹 형식에 정보 입력
            val groupDTO = GroupDTO()
            groupDTO.grpid = "$grpname@$username"
            groupDTO.grpname = grpname
            groupDTO.leader = username
            groupDTO.timestamp = System.currentTimeMillis()
            groupDTO.shareWith = arrayListOf(groupDTO.leader.toString())

            // 그룹 데이터베이스 확인
            firestore!!.collection("groups")
                .document("$grpname@$username")
                .get()
                .addOnCompleteListener { task ->
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

    // users 컬렉션의 userGroups 필드에 추가
    private fun addToUserGroups(grpname: String) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val uid = auth?.currentUser?.uid.toString()
        val username = auth?.currentUser?.displayName.toString()

        GlobalScope.launch(Dispatchers.IO) {
            val userDTO = firestore!!.collection("users")
                .document(uid)
                .get()
                .await()
                .toObject(UserDTO::class.java)

            var userGroups = userDTO?.userGroups

            if (userGroups.isNullOrEmpty()) userGroups = arrayListOf("$grpname@$username")
            else userGroups.add("$grpname@$username")

            firestore!!.collection("users")
                .document(uid)
                .update("userGroups", FieldValue.arrayUnion("$grpname@$username"))
                .await()

        }
        Toast.makeText(activity, "$grpname 그룹을 생성했습니다", Toast.LENGTH_SHORT).show()

//        // 사용자 데이터베이스에 추가
//        firestore!!.collection("users")
//            .document(uid)
//            .get()
//            .addOnCompleteListener { task ->
//                val document = task.result
//                var userGroup = document["userGroups"] as ArrayList<String>?
//
//                if (userGroup.isNullOrEmpty())
//                    userGroup = arrayListOf("$grpname@$username")
//                else
//                    userGroup.add("$grpname@$username")
//
//                firestore!!.collection("users")
//                    .document(uid)
//                    .update("userGroups", userGroup)
//            }
//
//        Toast.makeText(activity, "$grpname 그룹을 생성했습니다", Toast.LENGTH_SHORT).show()
    }
}

