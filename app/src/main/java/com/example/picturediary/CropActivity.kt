package com.example.picturediary

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.picturediary.navigation.dao.DBHelper
import kotlinx.android.synthetic.main.activity_crop.*
import kotlinx.android.synthetic.main.activity_crop.view.*

class CropActivity: AppCompatActivity() {
    var picture : Bitmap? = null
    var pickedDate: String? = null
    private var cropView: CropView? = null
    private val loggedInUser = PrefApplication.prefs.getString("loggedInUser", "")
    private val username = loggedInUser.split("★")[0]

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        // intent 가져오기 (그림 & 날짜)
        val arr = intent.getByteArrayExtra("picture")
        pickedDate = intent.getStringExtra("pickedDate")
        picture = BitmapFactory.decodeByteArray(arr, 0, arr!!.size)

        // 어댑터 띄우기
        val dbName = "pictureDiary.db"
        val dbHelper = DBHelper(applicationContext, dbName, null, 1)
        val objectArrayList = dbHelper.readObjects(pickedDate!!, username)
        val objectListAdapter = ObjectListAdapter(objectArrayList)
        objectRecycler.apply {
            objectRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            objectRecycler.adapter = objectListAdapter
        }

        // CropView.kt에 작업 넘김
        cropView = findViewById(R.id.crop_view)
        cropView?.setDrawId(pickedDate!!)
        cropView?.setDrawing(picture!!)

        completeBtn.setOnClickListener {    //완료버튼
            val intent = Intent(this, TextActivity::class.java)
            startActivity(intent)
        }

        playAll.setOnClickListener {    //모두재생 버튼

        }
    }
}