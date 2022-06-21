package com.example.picturediary

import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_motion.*


class MotionActivity : AppCompatActivity() {
    val utils = Utils()
    var firestore: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null
    var picture : Bitmap? = null
    var username: String? = null
    var height = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motion)

        val aniRotate: Animation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        val aniBounce: Animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        val aniShake : Animation = AnimationUtils.loadAnimation(this, R.anim.shake)

//        val layout = findViewById<View>(R.id.M_layout) as FrameLayout
//        val vto = layout.viewTreeObserver
//        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                val width = layout.measuredWidth
//                height = layout.measuredHeight
//            }
//        })

        val dbHelper = Utils().createDBHelper(applicationContext)
        firestore = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        username = auth?.currentUser?.displayName.toString()

        val pickedDate = intent.getStringExtra("pickedDate")
        val arr = intent.getByteArrayExtra("picture")
        val picture = BitmapFactory.decodeByteArray(arr, 0, arr!!.size)

        // 사용자 그림에 대한 캔버스 초기화
        val size = Resources.getSystem().displayMetrics.widthPixels
        val userDrawing = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(userDrawing)
        canvas.drawBitmap(picture, 0f, 0f, null)

        // 해당 그림에 대한 선택된 객체 모두 불러오기
        val objects = dbHelper.readObjects(pickedDate!!, username!!)
        for (object_ in objects) {
            val drawId = object_.fullDraw.toString()
            val objId = object_.objId.toString()
            val objectDTO = dbHelper.readSingleObject(drawId, objId)
//            val objPath = utils.getPath(dbHelper.readObjectPath(drawId, objId))
            val leftX = objectDTO.left!!.toFloat()
            val rightX = objectDTO.right!!.toFloat()
            val topY = objectDTO.top!!.toFloat()
            val bottomY = objectDTO.bottom!!.toFloat()
            val img = BitmapFactory.decodeByteArray(objectDTO.drawObjWhole!!, 0, objectDTO.drawObjWhole!!.size)
            val motion = objectDTO.motion.toString()

            // 애니메이션 넣은 부분 지우기 (해당 그림에 있는 모든 객체 지우기)
            val erase = Paint()
            erase.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
            erase.color = Color.WHITE
            erase.isAntiAlias = true
            canvas.drawRect(leftX, topY, rightX, bottomY, erase)

            val iv = ImageView(this)
            iv.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            iv.setImageBitmap(img)
            iv.left = leftX.toInt()
            iv.top = topY.toInt()
            M_layout.addView(iv)

            when (motion) {
                "bingle" -> iv.startAnimation(aniRotate)
                "jump" -> iv.startAnimation(aniBounce)
                "shake" -> iv.startAnimation(aniShake)
            }
        }
        whole.setImageBitmap(userDrawing)


    }
}