package com.example.picturediary
import android.app.Activity
import android.graphics.*
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView


class ImageCropActivity : Activity() {
    var compositeImageView: ImageView? = null
    var crop = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.motion)
        val extras = intent.extras
        if (extras != null) {
            crop = extras.getBoolean("crop")
        }
        var widthOfscreen = 0
        var heightOfScreen = 0
        val dm = DisplayMetrics()
        try {
            windowManager.defaultDisplay.getMetrics(dm)
        } catch (ex: Exception) {
        }
        widthOfscreen = dm.widthPixels
        heightOfScreen = dm.heightPixels
        compositeImageView = findViewById<View>(R.id.imgView) as ImageView
        val bitmap2 =
        val resultingImage = Bitmap.createBitmap(
            widthOfscreen,
            heightOfScreen, bitmap2.config
        )
        val canvas = Canvas(resultingImage)
        val paint = Paint()
        paint.isAntiAlias = true
        val path = Path()
        for (i in CropView.points.indices) {
            path.lineTo(CropView.points[i]!!.x.toFloat(), CropView.points[i]!!.y.toFloat())
        }
        canvas.drawPath(path, paint)
        if (crop) {
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        } else {
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        }
        canvas.drawBitmap(bitmap2, 0f, 0f, paint)
        compositeImageView!!.setImageBitmap(resultingImage)
    }
}