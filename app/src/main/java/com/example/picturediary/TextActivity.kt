package com.example.picturediary

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.picturediary.navigation.dao.DBHelper
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
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class TextActivity  : AppCompatActivity() {
    var firestore: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null
    var storage : FirebaseStorage? = null
    var picture : Bitmap? = null
    var fileUri : Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.diary_text)

        val arr = intent.getByteArrayExtra("picture")
        picture = BitmapFactory.decodeByteArray(arr, 0, arr!!.size)

        diaryImg.setImageBitmap(picture)

        fileUri = getImageUri(this@TextActivity, picture)

        firestore = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        storage = FirebaseStorage.getInstance()

        val dbName = "pictureDiary.db"
        val dbHelper = DBHelper(applicationContext, dbName, null, 1)



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

                                val filename = "picturediary_$username$.png"
                                storage!!.getReference().child("/images/$filename").putFile(fileUri!!)

                                val contentDTO = ContentDTO()
                                contentDTO.uid = uid
                                contentDTO.explain = diarystory
                                contentDTO.username = username
                                contentDTO.groupId = finalGroupsID[i]
                                contentDTO.timestamp = System.currentTimeMillis()
                                contentDTO.imageUrl = fileUri.toString()


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

    fun getImageUri(inContext: Context?, inImage: Bitmap?): Uri? {
        val bytes = ByteArrayOutputStream()
        if (inImage != null) {
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        }
        val path = MediaStore.Images.Media.insertImage(inContext?.getContentResolver(), inImage, "Title" + " - " + Calendar.getInstance().getTime(), null)
        return Uri.parse(path)
    }

}