package com.example.picturediary

import android.graphics.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


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
    }
}