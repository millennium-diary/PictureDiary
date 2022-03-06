package com.example.picturediary

import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.*
import com.example.picturediary.navigation.*
import com.example.picturediary.navigation.model.GroupDTO
import com.example.picturediary.navigation.model.UserDTO
import com.google.android.gms.tasks.*
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.community.*
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList


class GalleryActivity : AppCompatActivity(), OnItemSelectedListener {
    var auth : FirebaseAuth? = null
    private var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.community)
        bottom_navigation.setOnItemSelectedListener(this)

        // 그룹 생성
        add_grp.setOnClickListener {
            var grpname = ""

            // 팝업 설정
            val dlg = AlertDialog.Builder(this)
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            dlg.setTitle("생성할 그룹의 이름을 작성하세요")
            dlg.setView(input)
            dlg.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                grpname = input.text.toString()
                addGroup(grpname)
            })
            dlg.show()
        }

        // (일기 공유할 그룹) 그룹 목록 파이어스토어에서 가져와야 됨
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val uid = auth?.currentUser?.uid.toString()

        // 해당 사용자의 그룹 모두 가져오기
        firestore!!
            .collection("groups")
            .whereArrayContains("shareWith", uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val groupList = ArrayList<String>()
                    for (doc in task.result) {
                        val group = doc.data["grpname"].toString()
                        groupList.add(group)
                    }
                    val finalGroups = groupList.toTypedArray()
                    val checkArray = BooleanArray(finalGroups.size)
                    select_grp.setOnClickListener {
                        val dlg = AlertDialog.Builder(this)
                        dlg.setTitle("일기를 함께 공유할 그룹을 선택하세요")
                        dlg.setMultiChoiceItems(finalGroups, checkArray) { dialog, which, isChecked ->
                            // 해당 그룹에 일기 공유
                            select_grp.text = finalGroups[which]
                        }
                        dlg.setPositiveButton("확인", null)
                        dlg.show()
                    }
                }
            }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 그룹 목록
            R.id.action_home -> {
                val detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content, detailViewFragment)
                    .commit()
                return true
            }

            // 사용자 계정
            R.id.action_account -> {
                val userFragment = UserFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content, userFragment)
                    .commit()
                return true
            }
        }
        return false
    }

    private fun addGroup(grpname : String) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val userEmail = auth?.currentUser?.email.toString()
        val uid = auth?.currentUser?.uid.toString()

        // 그룹 데이터베이스에 추가
        val groupDTO = GroupDTO()
        groupDTO.grpid = "$uid@$grpname"
        groupDTO.grpname = grpname
        groupDTO.creator = auth?.currentUser?.uid
        groupDTO.timestamp = System.currentTimeMillis()
        groupDTO.shareWith = arrayListOf(groupDTO.creator.toString())
        firestore?.collection("groups")?.document("$uid@$grpname")?.set(groupDTO)

        // 사용자 데이터베이스에 추가
        firestore!!.collection("users")
            .document(userEmail).get()
            .addOnCompleteListener { task ->
                val document = task.result
                var group = document["userGroups"] as ArrayList<String>?

                // 그룹 없으면 추가
                if (group == null)
                    group = arrayListOf("$uid@$grpname")
                else {
                    // 이미 있는 그룹이면 메시지 띄움
                    if (group.contains("$uid@$grpname"))
                        Toast.makeText(this, "이미 존재하는 그룹입니다", Toast.LENGTH_SHORT).show()
                    // 겹치지 않으면 그룹 추가
                    else group.add("$uid@$grpname")
                }
                firestore!!.collection("users")
                    .document(userEmail)
                    .update("userGroups", group)
            }
    }
}