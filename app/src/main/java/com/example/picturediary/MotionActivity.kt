package com.example.picturediary

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MotionActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.motion)
        val intent = intent
        val arr = getIntent().getByteArrayExtra("picture")
        val picture = BitmapFactory.decodeByteArray(arr, 0, arr!!.size)
        val imgView = findViewById<ImageView>(R.id.imgView)
        imgView.setImageBitmap(picture)
    }
    override fun onResume() {
        super.onResume()
        setContentView(CropView(this@MotionActivity))
    }
}

class CropView : View, View.OnTouchListener {
    private var paint: Paint
    var DIST = 2
    var flgPathDraw = true
    var mfirstpoint: Point? = null
    var bfirstpoint = false
    var mlastpoint: Point? = null
    var bitmap = BitmapFactory.decodeResource(
        resources,
        R.drawable.pk
    )
    var mContext: Context

    constructor(c: Context) : super(c) {
        mContext = c
        isFocusable = true
        isFocusableInTouchMode = true
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        paint.pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0F)
        paint.strokeWidth = 5f
        paint.color = Color.WHITE
        setOnTouchListener(this)
        points = ArrayList<Point?>()
        bfirstpoint = false
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        isFocusable = true
        isFocusableInTouchMode = true
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.WHITE
        setOnTouchListener(this)
        points = ArrayList<Point?>()
        bfirstpoint = false
    }

    public override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        val path = Path()
        var first = true
        var i = 0
        while (i < points.size) {
            val point: Point? = points[i]
            if (first) {
                first = false
                path.moveTo(point!!.x, point!!.y)
            } else if (i < points.size - 1) {
                val next: Point? = points[i + 1]
                path.quadTo(point!!.x, point!!.y, next!!.x, next!!.y)
            } else {
                mlastpoint = points[i]
                path.lineTo(point!!.x, point!!.y)
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
            && left_range_y < first.y && first.y < right_range_y) {
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
                        intent = Intent(mContext, ImageCropActivity::class.java)
                        intent.putExtra("crop", true)
                        mContext.startActivity(intent)
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        intent = Intent(mContext, ImageCropActivity::class.java)
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

    companion object {
        lateinit var points: MutableList<Point?>
    }
}