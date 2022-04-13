package com.example.picturediary

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_crop.view.*


class CropView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet), OnTouchListener {
    var flgPathDraw = true
    private var points = arrayListOf<Point?>()
    private var objects = arrayListOf<Bitmap>()
    private var bitmap: Bitmap? = null
    private var paint: Paint? = null
    private var mfirstpoint: Point? = null
    private var bfirstpoint = false
    private var mlastpoint: Point? = null


    init {
        isFocusable = true
        isFocusableInTouchMode = true
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.style = Paint.Style.STROKE
        paint!!.pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0F)
        paint!!.color = Color.MAGENTA
        paint!!.strokeWidth = 5f
        setOnTouchListener(this)
        bfirstpoint = false
    }

    fun setDrawing(picture: Bitmap) {
        bitmap = picture
    }

    public override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
        val path = Path()
        var first = true
        var i = 0
        while (i < points.size) {
            val point: Point? = points[i]
            when {
                first -> {
                    first = false
                    path.moveTo(point!!.x, point.y)
                }
                i < points.size - 1 -> {
                    val next: Point? = points[i + 1]
                    path.quadTo(point!!.x, point.y, next!!.x, next.y)
                }
                else -> {
                    mlastpoint = points[i]
                    path.lineTo(point!!.x, point.y)
                }
            }
            i += 2
        }
        canvas.drawPath(path, paint!!)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val point = Point()
        point.x = event.x
        point.y = event.y
        if (flgPathDraw) {
            if (bfirstpoint) {
                if (comparePoint(mfirstpoint, point)) {
                    points.add(mfirstpoint)
                    flgPathDraw = false
//                    showCropDialog()
                    val path = getPath()
                    getObject(bitmap!!, path)
                }
                else points.add(point)
            }
            else points.add(point)

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
                    if (!comparePoint(mfirstpoint, mlastpoint)) {
                        flgPathDraw = false
                        points.add(mfirstpoint)
//                        showCropDialog()
                        val path = getPath()
                        getObject(bitmap!!, path)
                    }
                }
            }
        }
        return true
    }

    private fun comparePoint(first: Point?, current: Point?): Boolean {
        val left_range_x = (current!!.x - 3).toInt()
        val left_range_y = (current.y - 3).toInt()
        val right_range_x = (current.x + 3).toInt()
        val right_range_y = (current.y + 3).toInt()
        return if (left_range_x < first!!.x && first.x < right_range_x
            && left_range_y < first.y && first.y < right_range_y
        ) {
            points.size >= 10
        } else false
    }

    private fun getPath(): Path {
        val path = Path()
        for (point in points)
            path.lineTo(point!!.x, point.y)
        return path
    }

    private fun getObject(bitmap: Bitmap, path: Path) {
        val view = this.parent.parent as ConstraintLayout
        val resultingImage = Bitmap.createBitmap(crop_view.width, crop_view.height, bitmap.config)

        val canvas = Canvas(resultingImage)
        val paint = Paint()

        canvas.drawPath(path, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        view.cropped.setImageBitmap(resultingImage)
    }

    private fun showCropDialog() {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                val intent: Intent
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
//                        intent = Intent(context, TestActivity::class.java)
//                        context.startActivity(intent)

//                        intent = Intent(mContext, MotionActivity::class.java)
//                        intent.putExtra("crop", true)
//                        mContext.startActivity(intent)
                    }
//                        DialogInterface.BUTTON_NEGATIVE -> {
//                            intent = Intent(mContext, MotionActivity::class.java)
//                            intent.putExtra("crop", false)
//                            mContext.startActivity(intent)
//                            bfirstpoint = false
//                        }
                }
            }
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setMessage("Do you Want to save Crop or Non-crop image?")
            .setPositiveButton("Crop", dialogClickListener)
//                .setNegativeButton("Non-crop", dialogClickListener).show()
            .setCancelable(true)
            .show()
    }
}