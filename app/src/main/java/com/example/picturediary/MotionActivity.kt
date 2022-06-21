package com.example.picturediary

import android.graphics.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_motion.*

class MotionActivity : AppCompatActivity() {
    var firestore: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null
    var picture : Bitmap? = null
    var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motion)

        val dbHelper = Utils().createDBHelper(applicationContext)
        firestore = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        username = auth?.currentUser?.displayName.toString()

        val pickedDate = intent.getStringExtra("pickedDate")
        val arr = intent.getByteArrayExtra("picture")
        val picture = BitmapFactory.decodeByteArray(arr, 0, arr!!.size)

        // 해당 그림에 대한 선택된 객체 모두 불러오기
        val objects = dbHelper.readObjects(pickedDate!!, username!!)

        whole.setImageBitmap(picture)

        //애니메이션 넣은 부분 지우기
        val erase = Paint()
        erase.xermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        erase.color = Color.WHITE
        erase.isAntiAlias = true
        canvas.drawPath(objPath!!, erase)

        //이미지뷰로 애니메이션 적용해서 해당위치에 넣기


    }

}