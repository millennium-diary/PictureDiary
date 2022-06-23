package com.example.picturediary

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.example.picturediary.navigation.dao.DBHelper
import com.example.picturediary.navigation.model.DrawingDTO

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var eraserMode = false
    private var fullDrawing: DrawingDTO? = null
    private var pickedDate: String? = null
    private val dbHelper = Utils().createDBHelper(context)
    private val loggedInUser = PrefApplication.prefs.getString("loggedInUser", "")
    private val username = loggedInUser.split("★")[0]

    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 5.toFloat()
    private var color = Color.BLACK
    private var canvas: Canvas? = null
    private var mPaths = ArrayList<CustomPath>()
    private val mUndoPaths = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

    fun setDrawId(drawId: String) {
        pickedDate = drawId
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        fullDrawing = dbHelper.readDrawing(pickedDate!!, username)
        // 해당 날짜에 저장된 그림이 있으면 그림판에 해당 그림 띄우기
        if (fullDrawing != null) {
            val bitmap =
                BitmapFactory.decodeByteArray(fullDrawing!!.image, 0, fullDrawing!!.image!!.size)
            canvas?.drawBitmap(bitmap!!, 0f, 0f, mCanvasPaint)
        }
        // 저장된 그림이 없으면 빈 캔버스 띄우기
        else canvas?.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        for (path in mPaths) {
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas?.drawPath(path, mDrawPaint!!)
        }
        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas?.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize
                mDrawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }
        invalidate()
        return true
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
    }

    fun setBrushSize(newSize: Float) {
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
        )
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor: String) {
        if (newColor == "eraser") {
            eraserMode = true
            color = Color.WHITE
            mDrawPaint?.color = color
        }
        else {
            eraserMode = false
            color = Color.parseColor(newColor)
            mDrawPaint?.color = color
        }
    }

    fun onClickUndo() {
        if (mPaths.size > 0) {
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1))
            invalidate() // Invalidate the whole view. If the view is visible
        }
    }

    fun onClickRedo() {
        if (mUndoPaths.size > 0) {
            mPaths.add(mUndoPaths.removeAt(mUndoPaths.size - 1))
            invalidate() // Invalidate the whole view. If the view is visible
        }
    }

    fun onReset() {
        mPaths = ArrayList<CustomPath>()
        invalidate()
    }

    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {}
}