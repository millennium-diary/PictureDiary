package com.example.picturediary

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.example.picturediary.navigation.dao.DBHelper
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import kotlinx.android.synthetic.main.activity_drawing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import java.io.ByteArrayOutputStream


class DrawingActivity : AppCompatActivity() {
    private val loggedInUser = PrefApplication.prefs.getString("loggedInUser", "")
    private val username = loggedInUser.split("★")[0]
    lateinit var dbHelper: DBHelper

    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint: ImageButton? = null


    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)

        dbHelper = Utils().createDBHelper(this)
        val pickedDate = intent.getStringExtra("pickedDate")

        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setDrawId(pickedDate!!)

        val ibBrush: Button = findViewById(R.id.ib_brush)
        ibBrush.setOnClickListener{
            showBrushSizeChooserDialog()
        }


        val ibMotion: Button = findViewById(R.id.ib_motion)
        ibMotion.setOnClickListener {
            // 다음 액티비티에 인텐트 넘겨줌 (그림 & 날짜)
            val intent = Intent(this, CropActivity::class.java)

            val stream = ByteArrayOutputStream()
            val picture = Utils().getBitmapFromView(drawing_view)
            picture.compress(Bitmap.CompressFormat.PNG, 0, stream)
            val byteArray = stream.toByteArray()

            val fullDrawing = dbHelper.readDrawing(pickedDate!!, username)
            // 그림 존재하지 않음 --> DB에 추가
            if (fullDrawing == null)
                dbHelper.insertDrawing(pickedDate, username, byteArray)
            // 그림 존재 --> 그림 수정 --> 그림 업데이트 & 객체 삭제
            else {
                val drawing = fullDrawing.image
                val bitmap = BitmapFactory.decodeByteArray(drawing, 0, drawing!!.size)

                if (!bitmap.sameAs(picture)) {
                    dbHelper.updateDrawing(pickedDate, username, fullDrawing.content!!, byteArray)
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

    fun colorPicker(view: View?){
        drawingView?.showColorPicker(view)
    }

}

