package com.example.picturediary

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_crop.*


class CropActivity: AppCompatActivity() {
    var picture : Bitmap? = null
    var pickedDate: String? = null
    private var cropView: CropView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        val arr = intent.getByteArrayExtra("picture")
        pickedDate = intent.getStringExtra("pickedDate")
        picture = BitmapFactory.decodeByteArray(arr, 0, arr!!.size)

        cropView = findViewById(R.id.crop_view)
        cropView?.setDrawId(pickedDate!!)
        cropView?.setDrawing(picture!!)

        completeBtn.setOnClickListener {//완료버튼
            val intent = Intent(this, TextActivity::class.java)
            startActivity(intent)
        }

        playAll.setOnClickListener {//모두재생 버튼

        }


    }
}