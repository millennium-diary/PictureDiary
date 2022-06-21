package com.example.picturediary

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.picturediary.navigation.dao.DBHelper
import kotlinx.android.synthetic.main.activity_crop.*
import kotlinx.android.synthetic.main.activity_crop.view.*
import kotlinx.android.synthetic.main.activity_drawing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import java.io.ByteArrayOutputStream

@DelicateCoroutinesApi
class CropActivity : AppCompatActivity() {
    var picture: Bitmap? = null
    var pickedDate: String? = null
    private var cropView: CropView? = null
    private val loggedInUser = PrefApplication.prefs.getString("loggedInUser", "")
    private val username = loggedInUser.split("★")[0]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        // intent 가져오기 (그림 & 날짜)
        val arr = intent.getByteArrayExtra("picture")
        pickedDate = intent.getStringExtra("pickedDate")
        picture = BitmapFactory.decodeByteArray(arr, 0, arr!!.size)

        // 어댑터 띄우기
        val dbHelper = Utils().createDBHelper(applicationContext)
//        val objectArrayList = dbHelper.readObjects(pickedDate!!, username)
//        val objectListAdapter = ObjectListAdapter(objectArrayList)
//        objectRecycler.apply {
//            objectRecycler.layoutManager =
//                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//            objectRecycler.adapter = objectListAdapter
//        }

        // CropView.kt에 작업 넘김
        cropView = findViewById(R.id.crop_view)
        cropView?.setDrawId(pickedDate!!)
        cropView?.setDrawing(picture!!)

        // 완료 버튼
        completeBtn.setOnClickListener {
            val intent = Intent(this, TextActivity::class.java)
            intent.putExtra("picture", arr)
            intent.putExtra("pickedDate", pickedDate)
            startActivity(intent)
        }

        playAll.setOnClickListener { //모두재생 버튼
            val intent = Intent(this, MotionActivity::class.java)
            val stream = ByteArrayOutputStream()
            val picture = Utils().getBitmapFromView(crop_view)
            picture.compress(Bitmap.CompressFormat.PNG, 0, stream)
            val byteArray = stream.toByteArray()
            intent.putExtra("pickedDate", pickedDate)
            intent.putExtra("picture", byteArray)
            startActivity(intent)
        }
    }
}