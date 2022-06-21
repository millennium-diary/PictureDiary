package com.example.picturediary

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dev.bmcreations.scrcast.ScrCast
import kotlinx.android.synthetic.main.activity_motion.*
import kotlinx.android.synthetic.main.fragment_user.*
import java.io.File


class MotionActivity : AppCompatActivity() {
    val utils = Utils()
    var firestore: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null
    var username: String? = null

    private lateinit var pickedDate: String
    lateinit var arr: ByteArray
    lateinit var picture: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motion)

        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        firestore = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        username = auth?.currentUser?.displayName.toString()
//        ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        val record = intent.getBooleanExtra("record", false)
        pickedDate = intent.getStringExtra("pickedDate")!!
        arr = intent.getByteArrayExtra("picture")!!
        picture = BitmapFactory.decodeByteArray(arr, 0, arr.size)

        if (record) {
            waitText.text = "  결과물을 저장하고 있습니다  \n잠시 기다려 주세요"
//            val textBlink = AnimationUtils.loadAnimation(this, R.anim.text_blink)

            val recorder = ScrCast.use(this)
            recorder.apply {
                // configure options via DSL
                options {
                    video { maxLengthSecs = 5 }
                    storage { directoryName = "scrcast-sample" }
                    moveTaskToBack = false
                    startDelayMs = 5_000
                }
            }
            recorder.record()
            showAnimations()
            recorder.stopRecording()

            val intent = Intent(this, TextActivity::class.java)
            intent.putExtra("pickedDate", pickedDate)
            intent.putExtra("videoUri", getVideoUri())
            startActivity(intent)
        }

        else {
            showAnimations()
        }
    }

    private fun showAnimations() {
        val aniRotate: Animation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        val aniBounce: Animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        val aniShake : Animation = AnimationUtils.loadAnimation(this, R.anim.shake)
        val dbHelper = utils.createDBHelper(applicationContext)

        // 사용자 그림에 대한 캔버스 초기화
        val size = Resources.getSystem().displayMetrics.widthPixels
        val userDrawing = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(userDrawing)
        canvas.drawBitmap(picture, 0f, 0f, null)

        // 해당 그림에 대한 선택된 객체 모두 불러오기
        val objects = dbHelper.readObjects(pickedDate, username!!)
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

            // 선택된 객체는 해당 위치에 배치
            val iv = ImageView(this)
            iv.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            iv.setImageBitmap(img)
            iv.left = leftX.toInt()
            iv.top = topY.toInt()
            M_layout.addView(iv)

            // 모션 적용
            when (motion) {
                "bingle" -> iv.startAnimation(aniRotate)
                "jump" -> iv.startAnimation(aniBounce)
                "shake" -> iv.startAnimation(aniShake)
            }
        }
        whole.setImageBitmap(userDrawing)
    }

    // 비디오 URI 가져오기 (어떻게 하는지 모르겠다)
    // 파일명만으로 uri 가져오는 방법 한 번 써봐봐
    private fun getVideoUri(): Uri? {
        val video: File = "?"
        val uri = Uri.parse()
        return uri
    }
}