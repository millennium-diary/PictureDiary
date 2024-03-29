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
import com.example.picturediary.Utils
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
import kotlinx.coroutines.withContext
import java.util.function.Predicate


class DetailViewFragment : Fragment() {
    private val utils = Utils()
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
                GlobalScope.launch(Dispatchers.IO) {
                    // 그룹명이 비어있을 경우
                    if (grpname.isBlank()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity, "그룹명을 다시 확인해 주세요", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // 그룹이 이미 존재할 경우
                    else if (utils.groupExists(grpname)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity, "이미 존재하는 그룹입니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // 그룹이 존재하지 않을 경우
                    else if (!utils.groupExists(grpname)) {
                        utils.addToGroup(grpname, requireActivity())
                    }
                }
            })
            dlg.show()
        }

        // 어댑터에 변화가 생기면 바로 적용
        eventChangeListener(view)

        return view
    }

    private fun eventChangeListener(view: View) {
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
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                groupArrayList.add(dc.document.toObject(GroupDTO::class.java))
                                groupListAdapter.notifyDataSetChanged()
                                swipeHelperCallback.removePreviousClamp(view.detailRecycler)
                            }
                            DocumentChange.Type.REMOVED -> {
                                val data = dc.document.toObject(GroupDTO::class.java)
                                if (data.leader == username) {
                                    val shareWith = arrayListOf(username)
                                    data.shareWith = shareWith
                                    groupArrayList.remove(data)
                                }
                                else groupArrayList.remove(data)

                                groupListAdapter.notifyDataSetChanged()
                                swipeHelperCallback.removePreviousClamp(view.detailRecycler)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                groupListAdapter.notifyDataSetChanged()
                                swipeHelperCallback.removePreviousClamp(view.detailRecycler)
                            }
                        }
                    }
                }
            })
    }
}

