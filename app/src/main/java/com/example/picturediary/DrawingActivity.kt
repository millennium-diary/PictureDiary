package com.example.picturediary

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_drawing.*
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.example.picturediary.navigation.dao.DBHelper
import java.io.ByteArrayOutputStream


class DrawingActivity : AppCompatActivity() {
    private val loggedInUser = PrefApplication.prefs.getString("loggedInUser", "")
    private val username = loggedInUser.split("★")[0]
    private val dbName = "pictureDiary.db"
    lateinit var dbHelper: DBHelper

    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint: ImageButton? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)

        dbHelper = DBHelper(this, dbName, null, 1)
        val pickedDate = intent.getStringExtra("pickedDate")

        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setDrawId(pickedDate!!)

        val ibBrush: Button = findViewById(R.id.ib_brush)
        ibBrush.setOnClickListener{
            showBrushSizeChooserDialog()
        }

        val linearLayoutPaintColors = findViewById<LinearLayout>(R.id.ll_paint_colors)
        mImageButtonCurrentPaint = linearLayoutPaintColors[1] as ImageButton
        mImageButtonCurrentPaint?.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_pressed
            )
        )

        val ibMotion: Button = findViewById(R.id.ib_motion)
        ibMotion.setOnClickListener {
            // 다음 액티비티에 인텐트 넘겨줌 (그림 & 날짜)
            val intent = Intent(this, CropActivity::class.java)

            val stream = ByteArrayOutputStream()
            val picture = getBitmapFromView(drawing_view)
            picture.compress(Bitmap.CompressFormat.PNG, 50, stream)
            val byteArray = stream.toByteArray()

            val fullDrawing = dbHelper.readDrawing(pickedDate!!, username)
            // 그림 존재하지 않음 --> DB에 추가
            if (fullDrawing == null)
                dbHelper.insertDrawing(pickedDate, username, byteArray)
            // 그림 존재 --> 그림 수정 --> 그림 업데이트 & 객체 삭제
            else {
                val drawing = fullDrawing.image
                if (!drawing.contentEquals(byteArray)) {
                    dbHelper.updateDrawing(pickedDate, username, byteArray)
                    dbHelper.deleteAllObject(pickedDate, username)
                }
            }

            intent.putExtra("pickedDate", pickedDate)
            intent.putExtra("picture", byteArray)

            startActivity(intent)
        }

        val ibEraser : Button = findViewById(R.id.ib_eraser)
        ibEraser.setOnClickListener {
            val colorTag = ibEraser.tag.toString()
            drawingView?.setColor(colorTag)
        }

        val ibUndo: ImageButton = findViewById(R.id.ib_undo)
        ibUndo.setOnClickListener {
            drawingView?.onClickUndo()
        }

        val ibRedo: ImageButton = findViewById(R.id.ib_redo)
        ibRedo.setOnClickListener {
            drawingView?.onClickRedo()
        }

        val ibReset: ImageButton = findViewById(R.id.ib_reset)
        ibReset.setOnClickListener {
            drawingView?.onReset()
            val imageBackground: ImageView = findViewById(R.id.iv_background)
            imageBackground.setImageResource(0)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size :")

        val BruSize1: ImageButton = brushDialog.findViewById(R.id.ib_brush_size1)
        BruSize1.setOnClickListener(View.OnClickListener {
            drawingView?.setBrushSize(5.toFloat())
            brushDialog.dismiss()
        })

        val BruSize2: ImageButton = brushDialog.findViewById(R.id.ib_brush_size2)
        BruSize2.setOnClickListener(View.OnClickListener {
            drawingView?.setBrushSize(10.toFloat())
            brushDialog.dismiss()
        })
        val BruSize3: ImageButton = brushDialog.findViewById(R.id.ib_brush_size3)
        BruSize3.setOnClickListener(View.OnClickListener {
            drawingView?.setBrushSize(15.toFloat())
            brushDialog.dismiss()
        })
        val BruSize4: ImageButton = brushDialog.findViewById(R.id.ib_brush_size4)
        BruSize4.setOnClickListener(View.OnClickListener {
            drawingView?.setBrushSize(20.toFloat())
            brushDialog.dismiss()
        })

        val BruSize5: ImageButton = brushDialog.findViewById(R.id.ib_brush_size5)
        BruSize5.setOnClickListener(View.OnClickListener {
            drawingView?.setBrushSize(30.toFloat())
            brushDialog.dismiss()
        })
        brushDialog.show()
    }

    fun paintClicked(view: View?) {
        if (view !== mImageButtonCurrentPaint) {
            // Update the color
            val imageButton = view as ImageButton
            // Here the tag is used for swapping the current color with previous color.
            // The tag stores the selected view
            val colorTag = imageButton.tag.toString()
            // The color is set as per the selected tag here.
            drawingView?.setColor(colorTag)
            // Swap the backgrounds for last active and currently active image button.
            imageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))
            mImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_normal
                )
            )

            //Current view is updated with selected view in the form of ImageButton.
            mImageButtonCurrentPaint = view
        }
    }

    private fun getBitmapFromView(view : View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)

        val bgDrawable = view.background
        if(bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.TRANSPARENT)
        }
        view.draw(canvas)
        return returnedBitmap
    }
}

