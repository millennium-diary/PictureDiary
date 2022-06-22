package com.example.picturediary

import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dev.bmcreations.scrcast.ScrCast
import kotlinx.android.synthetic.main.activity_motion.*
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

        firestore = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        username = auth?.currentUser?.displayName.toString()

        val record = intent.getBooleanExtra("record", false)
        pickedDate = intent.getStringExtra("pickedDate")!!
        arr = intent.getByteArrayExtra("picture")!!
        picture = BitmapFactory.decodeByteArray(arr, 0, arr.size)

        showAnimations()

        if (record) {
            // 녹화 기능 설정
            val recorder = ScrCast.use(this)
            recorder.apply {
                // configure options via DSL
                options {
                    video { maxLengthSecs = 60 }
                    storage { directoryName = "scrcast-sample" }
                    moveTaskToBack = false
                    startDelayMs = 0
                    stopOnScreenOff = true
                }
            }

            startRecord.visibility = VISIBLE
            skipRecord.visibility = VISIBLE
            waitText.text = " '녹화 시작' 버튼을 눌러 원하는 부분을 녹화하세요 "
//            waitText.text = "  결과물을 저장하고 있습니다  \n잠시 기다려 주세요"
//            val textBlink = AnimationUtils.loadAnimation(this, R.anim.text_blink)

            skipRecord.setOnClickListener {
                moveToTextActivity(false)
            }

            startRecord.setOnClickListener {
                // 녹화 시작
                if (startRecord.text == "    녹화 시작    ") {
                    hideSystemUi()
                    skipRecord.visibility = GONE
                    waitText.text = " '녹화 끝내기' 버튼을 눌러 결과물을 저장하세요 "

                    startRecord.text = "녹화 끝내기"
                    recorder.record()
                }
                // 녹화 중단
                else if (startRecord.text == "녹화 끝내기") {
                    skipRecord.visibility = GONE
                    waitText.text = "  결과물을 저장하고 있습니다  \n잠시 기다려 주세요"

                    startRecord.text = "녹화 끝!"
                    recorder.stopRecording()

                    moveToTextActivity(true)
                }
            }
        }
    }

    private fun getVideoUri(): Uri? {
        val videoDir = "/storage/emulated/0/Movies/scrcast/"
        val videoFiles = File(videoDir).listFiles()
        val videoPath = videoFiles[videoFiles.lastIndex].toString()
        videoUri = Uri.parse(videoPath)
        Log.println(Log.INFO, "비디오 경로 2", videoUri.toString())
        return videoUri
    }

    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun moveToTextActivity(isVideo: Boolean) {
        val intent = Intent(this, TextActivity::class.java)
        intent.putExtra("pickedDate", pickedDate)
        if (isVideo)
            intent.putExtra("videoUri", getVideoUri().toString())

        intent.putExtra("isVideo", isVideo)
        startActivity(intent)
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

//            if (objectDTO.replaceDraw == null)
//                val img = BitmapFactory.decodeByteArray(objectDTO.originalDraw!!, 0, objectDTO.originalDraw!!.size)
//            else
//                val img = BitmapFactory.decodeByteArray(objectDTO.replaceDraw!!, 0, objectDTO.replaceDraw!!.size)

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
}