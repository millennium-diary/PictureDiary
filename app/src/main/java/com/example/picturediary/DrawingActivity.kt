package com.example.picturediary

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.drawing_main.*

class DrawingActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    private var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawing_main)

        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val username = auth?.currentUser?.displayName.toString()


        // 해당 사용자의 그룹 모두 가져오기
        firestore!!
            .collection("groups")
            .whereArrayContains("shareWith", username)
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

                    // 그룹 선택창
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
}