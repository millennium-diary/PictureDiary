package com.example.picturediary

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.picturediary.navigation.model.ContentDTO
import com.example.picturediary.navigation.model.GroupDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.diary_text.*
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TextActivity  : AppCompatActivity() {
    var firestore: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null
    var storage : FirebaseStorage? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.diary_text)

        // dlg후 파이어베이스에 저장
        firestore = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        storage = FirebaseStorage.getInstance()

        val username = auth?.currentUser?.displayName.toString()
        val uid = auth?.currentUser?.uid.toString()

        val intent = Intent(this, MainActivity::class.java)




        GlobalScope.launch(Dispatchers.IO) {
            val groupDTOs = firestore!!.collection("groups")
                .whereArrayContains("shareWith", username)
                .get()
                .await()
                .toObjects(GroupDTO::class.java) as ArrayList<GroupDTO>

            val groups = arrayListOf<String>()
            val groupsid = arrayListOf<String>()
            for (groupDTO in groupDTOs) {
                val groupName = groupDTO.grpname.toString()
                val groupId = groupDTO.grpid.toString()
                groups.add(groupName)
                groupsid.add(groupId)
            }
            val finalGroups = groups.toTypedArray()
            val checkArray = BooleanArray(groups.size)

            val finalGroupsID = groupsid.toTypedArray()
            val checkArrayId = BooleanArray(groupsid.size)

            //그룹 선택창
            confirmText.setOnClickListener {
                val dlg = AlertDialog.Builder(this@TextActivity)

                if (groups.size == 0) {
                    dlg.setTitle("공유할 그룹이 존재하지 않습니다")
                } else {
                    dlg.setTitle("일기를 함께 공유할 그룹을 선택하세요")
                    dlg.setMultiChoiceItems(finalGroups, checkArray) { dialog, which, isChecked ->
                        // 해당 그룹에 일기 공유
                        //confirmText.text = groups[which]
                        checkArray[which]= isChecked
                        checkArrayId[which]= isChecked

                    }
                }
                dlg.setPositiveButton("확인",
                    DialogInterface.OnClickListener { dialog, id ->
                        for(i in checkArray.indices){
                            val checked = checkArray[i]
                            if(checked){
                                finalGroups[i]
                                finalGroupsID[i]
                                val diarystory =editTextTextMultiLine.text.toString()
                                val diaryPicture = R.drawable.pk

                                val pictureURI = Uri.parse("android.resource://" + packageName+"/"+diaryPicture)

                                storage!!.getReference().child("images").putFile(pictureURI)

                                val contentDTO = ContentDTO()
                                contentDTO.uid = uid
                                contentDTO.explain = diarystory
                                contentDTO.username = username
                                contentDTO.groupId = finalGroupsID[i]
                                contentDTO.timestamp = System.currentTimeMillis()
                                contentDTO.imageUrl = pictureURI.toString()


                                firestore!!.collection("images")
                                    .document()
                                    .set(contentDTO)
                                    //.await()
                                startActivity(intent)
                            }
                        }
                    })
                dlg.show()
            }
        }

    }
}