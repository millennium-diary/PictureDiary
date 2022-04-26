package com.example.picturediary

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import kotlinx.coroutines.Dispatchers

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_drawing.*
import kotlinx.coroutines.launch
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class DrawingActivity : AppCompatActivity() {
    private var auth : FirebaseAuth? = null
    private var firestore : FirebaseFirestore? = null

    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint: ImageButton? = null
    var customProgressDialog: Dialog? = null

    //ssdsds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)

        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val username = auth?.currentUser?.displayName.toString()

//        GlobalScope.launch(Dispatchers.IO) {
//            val groupDTOs = firestore!!.collection("groups")
//                .whereArrayContains("shareWith", username)
//                .get()
//                .await()
//                .toObjects(GroupDTO::class.java) as ArrayList<GroupDTO>
//
//            val groups = arrayListOf<String>()
//            for (groupDTO in groupDTOs) {
//                val groupName = groupDTO.grpname.toString()
//                groups.add(groupName)
//            }
//            val finalGroups = groups.toTypedArray()
//            val checkArray = BooleanArray(groups.size)

            // 그룹 선택창
//            select_grp.setOnClickListener {
//                val dlg = AlertDialog.Builder(this@DrawingActivity)
//
//                if (groups.size == 0) {
//                    dlg.setTitle("공유할 그룹이 존재하지 않습니다")
//                }
//                else {
//                    dlg.setTitle("일기를 함께 공유할 그룹을 선택하세요")
//                    dlg.setMultiChoiceItems(finalGroups, checkArray) { dialog, which, isChecked ->
//                        // 해당 그룹에 일기 공유
//                        select_grp.text = groups[which]
//                    }
//                }
//                dlg.setPositiveButton("확인", null)
//                dlg.show()
//            }
//        }

        drawingView = findViewById(R.id.drawing_view)
        val ibBrush: Button = findViewById(R.id.ib_size)
        ibBrush.setOnClickListener {
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
            val intent = Intent(this, CropActivity::class.java)
            lifecycleScope.launch {
                val stream = ByteArrayOutputStream()
                val picture = getBitmapFromView(drawing_view)
                picture.compress(Bitmap.CompressFormat.PNG, 50, stream)
                val byteArray = stream.toByteArray()
                intent.putExtra("picture", byteArray)
            }
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
/*
        val ibSave: Button = findViewById(R.id.ib_save)
        ibSave.setOnClickListener {
            if(isReadStorageAllowed()) {
                lifecycleScope.launch {
//                    val fl: FrameLayout = findViewById(R.id.fl_drawing_view_container)
                    saveBitmapFile(getBitmapFromView(fl_drawing_view_container))
                }
            }
            else requestStoragePermission()
        }
*/
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

//    val openGalleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
//        if (result.resultCode == RESULT_OK && result.data != null){
//            val imageBackground: ImageView = findViewById(R.id.iv_background)
//            imageBackground.setImageURI(result.data?.data)
//        }
//    }

    private val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val perMissionName = it.key
                val isGranted = it.value
                if (isGranted ) {
                    Toast.makeText(
                        this,
                        "Permission granted now you can read the storage files.",
                        Toast.LENGTH_SHORT
                    ).show()
                    //perform operation
                    //create an intent to pick image from external storage

                } else {
                    if (perMissionName == android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        Toast.makeText(
                            this,
                            "Oops you just denied the permission.",
                            Toast.LENGTH_LONG
                        ).show()
                }
            }
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
            // Here the tag is used for swaping the current color with previous color.
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

    private fun isReadStorageAllowed(): Boolean{
        val result = ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission(){
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
        ){
            showRationaleDialog("Paint","Paint " +
                    "needs to Access Your External Storage")
        }
        else {
            requestPermission.launch(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }

    }

    private fun showRationaleDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun getBitmapFromView(view : View): Bitmap{
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

    private suspend fun saveBitmapFile(mBitmap: Bitmap): String{
        var result =  ""
        withContext(Dispatchers.IO){
            try {
                val bytes = ByteArrayOutputStream()
                mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)

                val f = File(externalCacheDir?.absoluteFile.toString()+File.separator+"Paints_"+System.currentTimeMillis()/1000+".png")
                val fo = FileOutputStream(f)
                fo.write(bytes.toByteArray())
                fo.close()
                result = f.absolutePath
                runOnUiThread {
                    if(result.isNotEmpty()) {
                        Toast.makeText(this@DrawingActivity,"File saved successfully: $result",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this@DrawingActivity,"File not saved.",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            catch (e: Exception){
                result = ""
                e.printStackTrace()
            }
        }
        return result
    }
}

