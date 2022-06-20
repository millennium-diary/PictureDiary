package com.example.picturediary

import android.content.*
import android.graphics.*
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.*
import com.example.picturediary.navigation.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_text.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import kotlin.collections.ArrayList

class TextActivity  : AppCompatActivity() {
    val utils = Utils()
    var firestore: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null
    var picture : Bitmap? = null
    var username: String? = null
    private var pickedDate: String? = null
    private var storage : FirebaseStorage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)

        // 인텐트 설정
        val arr = intent.getByteArrayExtra("picture")
        pickedDate = intent.getStringExtra("pickedDate")
        picture = BitmapFactory.decodeByteArray(arr, 0, arr!!.size)
        val intent = Intent(this, MainActivity::class.java)

        // 파이어스토어, 파이어베이스 설정
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = Firebase.auth
        username = auth?.currentUser?.displayName.toString()
        val uid = auth?.currentUser?.uid.toString()

        val dbHelper = Utils().createDBHelper(applicationContext)
        val drawingDTO = dbHelper.readDrawing(pickedDate!!, username!!)
        if  (drawingDTO != null)
            editTextTextMultiLine.setText(drawingDTO.content)
        diaryImg.setImageBitmap(picture)

        GlobalScope.launch(Dispatchers.IO) {
            val groupDTOs = firestore!!.collection("groups")
                .whereArrayContains("shareWith", username!!)
                .get()
                .await()
                .toObjects(GroupDTO::class.java) as ArrayList<GroupDTO>

            val groups = arrayListOf<String>()
            val groupsId = arrayListOf<String>()
            for (groupDTO in groupDTOs) {
                val groupName = groupDTO.grpname.toString()
                val groupId = groupDTO.grpid.toString()
                groups.add(groupName)
                groupsId.add(groupId)
            }
            val finalGroups = groups.toTypedArray()
            val checkArray = BooleanArray(groups.size)

            val finalGroupsID = groupsId.toTypedArray()
            val checkArrayId = BooleanArray(groupsId.size)

            // 그룹 선택창
            confirmText.setOnClickListener {
                val dlg = AlertDialog.Builder(this@TextActivity)
                val diaryStory = editTextTextMultiLine.text.toString()

                // 와이파이 연결이 안 되어있을 경우
                if (!utils.checkWifi(applicationContext)) {
                    dlg.setTitle("와이파이 연결이 되어 있지 않습니다")
                    dlg.setMessage("데이터는 저장되지만 공유되지 않습니다")
                    dlg.setNegativeButton("취소", null)
                    dlg.setPositiveButton("확인",
                        DialogInterface.OnClickListener { dialog, id ->
                            saveInDb(diaryStory)
                            startActivity(intent)
                        })
                }
                // 공유할 그룹이 없을 경우
                else if (groups.size == 0) {
                    dlg.setTitle("공유할 그룹이 존재하지 않습니다")
                    dlg.setMessage("데이터는 저장되지만 공유되지 않습니다")
                    dlg.setNegativeButton("취소", null)
                    dlg.setPositiveButton("확인",
                        DialogInterface.OnClickListener { dialog, id ->
                            saveInDb(diaryStory)
                            startActivity(intent)
                        })
                }
                // 공유할 그룹이 있을 경우
                else {
                    dlg.setTitle("일기를 함께 공유할 그룹을 선택하세요")
                    dlg.setMultiChoiceItems(finalGroups, checkArray) { dialog, which, isChecked ->
                        // 해당 그룹에 일기 공유
                        //confirmText.text = groups[which]
                        checkArray[which] = isChecked
                        checkArrayId[which] = isChecked
                    }
                    dlg.setPositiveButton("확인",
                        DialogInterface.OnClickListener { dialog, id ->
                            for (i in checkArray.indices) {
                                val checked = checkArray[i]

                                if (checked) {
                                    val groupID = finalGroupsID[i]
                                    val storageRef = storage!!.reference
                                    val data = saveInDb(diaryStory)

                                    // 파이어스토어에 일기 업데이트
                                    storageRef.child("images/$username-$groupID-$pickedDate")
                                        .putBytes(data)
                                        .addOnSuccessListener {
                                            val result = it.metadata!!.reference!!.downloadUrl
                                            result.addOnSuccessListener { uri ->
                                                val imageLink = uri.toString()

                                                val contentDTO = ContentDTO()
                                                contentDTO.uid = uid
                                                contentDTO.contentId = "$username-$groupID-$pickedDate"
                                                contentDTO.explain = diaryStory
                                                contentDTO.username = username
                                                contentDTO.groupId = finalGroupsID[i]
                                                contentDTO.timestamp = System.currentTimeMillis()
                                                contentDTO.imageUrl = imageLink
                                                contentDTO.diaryDate = pickedDate

                                                firestore!!.collection("contents")
                                                    .document()
                                                    .set(contentDTO)
                                            }
                                                .addOnFailureListener {
                                                    Toast.makeText(this@TextActivity,
                                                        "처리하는 중 오류가 발생했습니다",
                                                        Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    startActivity(intent)
                                }
                            }
                        })
                }
                dlg.show()
            }
        }
    }

    // 확인 눌렀을 당시 내장 DB에 저장
    private fun saveInDb(diaryStory: String): ByteArray {
        val dbHelper = Utils().createDBHelper(applicationContext)
        val baos = ByteArrayOutputStream()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            picture!!.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, baos)
        else
            picture!!.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        // 내장 DB에 일기 업데이트
        dbHelper.updateDrawing(pickedDate!!, username!!, diaryStory, data)

        return data
    }
}