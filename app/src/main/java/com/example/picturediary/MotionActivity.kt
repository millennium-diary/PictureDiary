package com.example.picturediary
import android.app.Activity
import android.graphics.*
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.motion.*


class MotionActivity : Activity() {
    var compositeImageView: ImageView? = null
    var crop = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.motion)

        Toast.makeText(applicationContext, "MotionActivity", Toast.LENGTH_SHORT).show()

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
        val bitmap2 = BitmapFactory.decodeResource(
            resources,
            R.drawable.pk
        )
        val resultingImage = Bitmap.createBitmap(
            widthOfscreen,
            heightOfScreen, bitmap2.config
        )
        val canvas = Canvas(resultingImage)
        val paint = Paint()
        paint.isAntiAlias = true
        val path = Path()
        val imageCropActivity = ImageCropActivity()
        for (i in imageCropActivity.points.indices) {
            path.lineTo(imageCropActivity.points[i]!!.x, imageCropActivity.points[i]!!.y)
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