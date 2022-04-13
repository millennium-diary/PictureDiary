package com.example.picturediary

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_crop.*


class CropActivity: AppCompatActivity() {
    var picture : Bitmap? = null
    private var cropView: CropView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        val arr = intent.getByteArrayExtra("picture")
        picture = BitmapFactory.decodeByteArray(arr, 0, arr!!.size)

        cropView = findViewById(R.id.crop_view)
        cropView?.setDrawing(picture!!)
    }
}