package com.example.picturediary

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.picturediary.navigation.model.GroupDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.drawing_main.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

class DrawingActivity : AppCompatActivity() {
    private var auth : FirebaseAuth? = null
    private var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawing_main)

        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val username = auth?.currentUser?.displayName.toString()

        GlobalScope.launch(Dispatchers.IO) {
            val groupDTOs = firestore!!.collection("groups")
                .whereArrayContains("shareWith", username)
                .get()
                .await()
                .toObjects(GroupDTO::class.java) as ArrayList<GroupDTO>

            val groups = arrayListOf<String>()
            for (groupDTO in groupDTOs) {
                val groupName = groupDTO.grpname.toString()
                groups.add(groupName)
            }
            val finalGroups = groups.toTypedArray()
            val checkArray = BooleanArray(groups.size)

            // 그룹 선택창
            select_grp.setOnClickListener {
                val dlg = AlertDialog.Builder(this@DrawingActivity)

                if (groups.size == 0) {
                    dlg.setTitle("공유할 그룹이 존재하지 않습니다")
                }
                else {
                    dlg.setTitle("일기를 함께 공유할 그룹을 선택하세요")
                    dlg.setMultiChoiceItems(finalGroups, checkArray) { dialog, which, isChecked ->
                        // 해당 그룹에 일기 공유
                        select_grp.text = groups[which]
                    }
                }
                dlg.setPositiveButton("확인", null)
                dlg.show()
            }
        }
    }
}

