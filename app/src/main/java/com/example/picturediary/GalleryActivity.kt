package com.example.picturediary

import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.*
import com.example.picturediary.navigation.*
import com.google.android.material.navigation.NavigationBarView.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.community.*

class GalleryActivity : AppCompatActivity(), OnItemSelectedListener {
    var auth : FirebaseAuth? = null
    var uid : String? = null
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
//                addGroup(grpname)
            })
            dlg.show()
        }

        // (일기 공유할 그룹) 그룹 목록 파이어스토어에서 가져와야 됨
        val myArray = arrayOf("내 일기", "가족", "친구")
        val checkArray = booleanArrayOf(false, false, false)
        select_grp.setOnClickListener {
            var dlg = AlertDialog.Builder(this)
            dlg.setTitle("해당 일기를 함께 공유할 그룹을 선택하세요")
            dlg.setMultiChoiceItems(myArray, checkArray) { dialog, which, isChecked ->
                select_grp.text = myArray[which]
            }
            dlg.setPositiveButton("확인", null)
            dlg.show()
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

//    private fun addGroup(grpname : String) {
//        auth = Firebase.auth
//        firestore = FirebaseFirestore.getInstance()
//
//        val groupDTO = GroupDTO()
//        groupDTO.maker = auth?.currentUser?.uid
//        groupDTO.grpname = grpname
//        groupDTO.timestamp = System.currentTimeMillis()
//        groupDTO.friends = mutableListOf(groupDTO.maker.toString())
//
//        firestore?.collection(auth!!.currentUser!!.uid)?.document(grpname)?.set(groupDTO)
//    }

//    private fun getGroups(grpname : String): GroupDTO? {
//        uid = FirebaseAuth.getInstance().currentUser?.uid
//        firestore = FirebaseFirestore.getInstance()
//        var groupDTO : GroupDTO? = null
//
//        val docRef = firestore?.collection(auth!!.currentUser!!.uid)
//        docRef!!.get().addOnSuccessListener { documentSnapshot ->
//            groupDTO = documentSnapshot.toObject(GroupDTO::class.java)
//        }
//        return groupDTO
//    }
}