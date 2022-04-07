package com.example.picturediary

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.motion.*

class ImageCropActivity : AppCompatActivity() {
    var picture : Bitmap? = null
    var points = arrayListOf<Point?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.motion)
//        setContentView(CropView(this@ImageCropActivity))

        Toast.makeText(applicationContext, "ImageCropActivity", Toast.LENGTH_SHORT).show()

        val arr = intent.getByteArrayExtra("picture")
        picture = BitmapFactory.decodeByteArray(arr, 0, arr!!.size)
        imgView.setImageBitmap(picture)
    }

    override fun onResume() {
        super.onResume()
        setContentView(Lasso(this@ImageCropActivity))

        points = arrayListOf<Point?>()
        val arr = intent.getByteArrayExtra("picture")
        picture = BitmapFactory.decodeByteArray(arr, 0, arr!!.size)
    }

    inner class Lasso : View, View.OnTouchListener {


        private var paint: Paint
        var flgPathDraw = true
        var mfirstpoint: Point? = null
        var bfirstpoint = false
        var mlastpoint: Point? = null
        var bitmap = picture
        var mContext: Context
//            get() {
//                TODO()
//            }

        constructor(c: Context) : super(c) {
            mContext = c
            isFocusable = true
            isFocusableInTouchMode = true
            paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.style = Paint.Style.STROKE
            paint.pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0F)
            paint.color = Color.MAGENTA
            paint.strokeWidth = 5f
            setOnTouchListener(this)
            bfirstpoint = false
        }

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            mContext = context
            isFocusable = true
            isFocusableInTouchMode = true
            paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = Color.MAGENTA
            setOnTouchListener(this)
            bfirstpoint = false
        }

        public override fun onDraw(canvas: Canvas) {
            canvas.drawBitmap(bitmap!!, 0f, 0f, null)
            val path = Path()
            var first = true
            var i = 0
            while (i < points.size) {
                val point: Point? = points[i]
                if (first) {
                    first = false
                    path.moveTo(point!!.x, point.y)
                } else if (i < points.size - 1) {
                    val next: Point? = points[i + 1]
                    path.quadTo(point!!.x, point.y, next!!.x, next.y)
                } else {
                    mlastpoint = points[i]
                    path.lineTo(point!!.x, point.y)
                }
                i += 2
            }
            canvas.drawPath(path, paint)
        }

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val point = Point()
            point.x = event.x
            point.y = event.y
            if (flgPathDraw) {
                if (bfirstpoint) {
                    if (comparepoint(mfirstpoint, point)) {
                        points.add(mfirstpoint)
                        flgPathDraw = false
                        showcropdialog()
                    } else {
                        points.add(point)
                    }
                } else {
                    points.add(point)
                }
                if (!bfirstpoint) {
                    mfirstpoint = point
                    bfirstpoint = true
                }
            }
            invalidate()
            if (event.action == MotionEvent.ACTION_UP) {
                mlastpoint = point
                if (flgPathDraw) {
                    if (points.size > 12) {
                        if (!comparepoint(mfirstpoint, mlastpoint)) {
                            flgPathDraw = false
                            points.add(mfirstpoint)
                            showcropdialog()
                        }
                    }
                }
            }
            return true
        }

        private fun comparepoint(first: Point?, current: Point?): Boolean {
            val left_range_x = (current!!.x - 3).toInt()
            val left_range_y = (current.y - 3).toInt()
            val right_range_x = (current.x + 3).toInt()
            val right_range_y = (current.y + 3).toInt()
            return if (left_range_x < first!!.x && first.x < right_range_x
                && left_range_y < first.y && first.y < right_range_y
            ) {
                points.size >= 10
            } else {
                false
            }
        }

        private fun showcropdialog() {
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    val intent: Intent
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            intent = Intent(mContext, MotionActivity::class.java)
                            intent.putExtra("crop", true)
                            mContext.startActivity(intent)
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                            intent = Intent(mContext, MotionActivity::class.java)
                            intent.putExtra("crop", false)
                            mContext.startActivity(intent)
                            bfirstpoint = false
                        }
                    }
                }
            val builder = AlertDialog.Builder(mContext)
            builder.setMessage("Do you Want to save Crop or Non-crop image?")
                .setPositiveButton("Crop", dialogClickListener)
                .setNegativeButton("Non-crop", dialogClickListener).show()
                .setCancelable(false)
        }
    }
//    companion object {
//        lateinit var points: MutableList<Point?>
//    }
}