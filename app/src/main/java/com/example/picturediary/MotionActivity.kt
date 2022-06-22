package com.example.picturediary

import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motion)

        val aniRun: Animation = AnimationUtils.loadAnimation(this, R.anim.run)
        val aniBounce: Animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        val aniShake : Animation = AnimationUtils.loadAnimation(this, R.anim.shake)
        val aniCome : Animation = AnimationUtils.loadAnimation(this, R.anim.comein)
        val aniGo : Animation = AnimationUtils.loadAnimation(this, R.anim.goout)
        val aniFade : Animation = AnimationUtils.loadAnimation(this, R.anim.fadeinout)
        val aniRoll : Animation = AnimationUtils.loadAnimation(this, R.anim.roll)
        val aniSpin : Animation = AnimationUtils.loadAnimation(this, R.anim.spin)

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
            val img = BitmapFactory.decodeByteArray(objectDTO.originalDraw!!, 0, objectDTO.originalDraw!!.size)

            // 애니메이션 넣은 부분 지우기 (해당 그림에 있는 모든 객체 지우기)
            val erase = Paint()
            erase.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
            erase.color = Color.WHITE
            erase.isAntiAlias = true
            canvas.drawRect(leftX, topY, rightX, bottomY, erase)

            val iv: ImageView = ImageView(this)
//            iv.layoutParams = ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//            )

//            val IVRelativeLayout = LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//            )
            iv.setImageBitmap(img)
//            val IVRelativeLayout = iv.layoutParams as? LinearLayout.LayoutParams
//            IVRelativeLayout.topMargin = topY.toInt()
//            IVRelativeLayout.leftMargin =leftX.toInt()
//            iv.layoutParams = IVRelativeLayout
            val IVRelativeLayout = iv.layoutParams as? RelativeLayout.LayoutParams
            IVRelativeLayout!!.setMargins(leftX.toInt(), topY.toInt(), 0, 0)

            iv.layoutParams = IVRelativeLayout

            iv.left=leftX.toInt()
            iv.top=topY.toInt()
//            iv.layoutParams.width=img.width
//            iv.layoutParams.height=img.height
            Log.d("size",iv.layoutParams.width.toString()+", " +iv.layoutParams.height.toString())
            Log.d("size", "$leftX, $rightX, $topY, $bottomY")
            iv.requestLayout()

            M_layout.addView(iv, img.width, img.height)


            //모션넣기
            if(objectDTO.motion != null) {
                when(objectDTO.motion!!.toString()){
                    "run" -> iv.startAnimation(aniRun)
                    "jump" -> iv.startAnimation(aniBounce)
                    "shake" -> iv.startAnimation(aniShake)
                    "come" -> iv.startAnimation(aniCome)
                    "go" -> iv.startAnimation(aniGo)
                    "fade" -> iv.startAnimation(aniFade)
                    "roll" -> iv.startAnimation(aniRoll)
                    "spin" -> iv.startAnimation(aniSpin)
                }
            }



        }
        whole.setImageBitmap(userDrawing)
    }
}

