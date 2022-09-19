package com.example.picturediary

import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dev.bmcreations.scrcast.ScrCast
import java.io.File
import kotlinx.android.synthetic.main.activity_motion.*


class MotionActivity : AppCompatActivity() {
    val utils = Utils()
    var firestore: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null
    var username: String? = null
    var showAnimationDone = false

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

        // 모션 보여주기
        showAnimations()

        // 사용자가 완료를 눌렀을 경우
        if (record) {
            // 녹화 기능 설정
            val recorder = ScrCast.use(this)
            recorder.apply {
                // configure options via DSL
                options {
                    video { maxLengthSecs = 5 }
                    storage { directoryName = "scrcast-sample" }
                    moveTaskToBack = false
                    startDelayMs = 0
                    stopOnScreenOff = true
                }
            }

            hideSystemUi()
            waitLayout.visibility = VISIBLE
            waitText.text = "  일기 만드는 중...  "
            Glide.with(this).load(R.raw.bookflip).into(bookflip)

            recorder.record()
            showAnimations()
            recorder.stopRecording()
            recorder.onRecordingComplete {
                moveToTextActivity()
            }
        }
    }

    // 영상 경로 --> URI 변환
    private fun getVideoUri(): Uri? {
        var videoUri : Uri? = null
        val videoDir = "/storage/emulated/0/Movies/scrcast/"
        val videoFiles = File(videoDir).listFiles()
        val videoPath = videoFiles[videoFiles.lastIndex].toString()
        videoUri = Uri.parse(videoPath)
        return videoUri
    }

    // 상태바, 네비게이션바 숨기기
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    // 글 작성 페이지로 이동
    private fun moveToTextActivity() {
        val intent = Intent(this, TextActivity::class.java)
        intent.putExtra("pickedDate", pickedDate)
        intent.putExtra("videoUri", getVideoUri().toString())
        startActivity(intent)
    }

    // 모션 보여주기
    private fun showAnimations() {
        val aniRunRight: Animation = AnimationUtils.loadAnimation(this, R.anim.run_right)
        val aniRunLeft: Animation = AnimationUtils.loadAnimation(this, R.anim.run_left)
        val aniBounce: Animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        val aniShake: Animation = AnimationUtils.loadAnimation(this, R.anim.shake)
        val aniCome: Animation = AnimationUtils.loadAnimation(this, R.anim.comein)
        val aniGo: Animation = AnimationUtils.loadAnimation(this, R.anim.goout)
        val aniFade: Animation = AnimationUtils.loadAnimation(this, R.anim.fadeinout)
        val aniBlink: Animation = AnimationUtils.loadAnimation(this, R.anim.blink)
        val aniRollRight: Animation = AnimationUtils.loadAnimation(this, R.anim.roll_right)
        val aniRollLeft: Animation = AnimationUtils.loadAnimation(this, R.anim.roll_left)
        val aniSpin: Animation = AnimationUtils.loadAnimation(this, R.anim.spin)

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
            val objPath = utils.getPath(dbHelper.readObjectPath(drawId, objId))
            val leftX = objectDTO.left!!.toFloat()
            val rightX = objectDTO.right!!.toFloat()
            val topY = objectDTO.top!!.toFloat()
            val bottomY = objectDTO.bottom!!.toFloat()

            val img = BitmapFactory.decodeByteArray(
                objectDTO.drawObjWhole!!, 0, objectDTO.drawObjWhole!!.size
            )
            val motion = objectDTO.motion.toString()

            // 애니메이션 넣은 부분 지우기 (해당 그림에 있는 모든 객체 지우기)
            val erase = Paint()
            erase.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            erase.alpha = 0xFF
//            erase.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
//            erase.color = Color.WHITE
            erase.isAntiAlias = true
//            canvas.drawPath(objPath, erase)
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
                "run_right" -> iv.startAnimation(aniRunRight)
                "run_left" -> iv.startAnimation(aniRunLeft)
                "jump" -> iv.startAnimation(aniBounce)
                "shake" -> iv.startAnimation(aniShake)
                "come" -> iv.startAnimation(aniCome)
                "go" -> iv.startAnimation(aniGo)
                "fade" -> iv.startAnimation(aniFade)
                "blink" -> iv.startAnimation(aniBlink)
                "roll_right" -> iv.startAnimation(aniRollRight)
                "roll_left" -> iv.startAnimation(aniRollLeft)
                "spin" -> iv.startAnimation(aniSpin)
            }
        }
        whole.setImageBitmap(userDrawing)
        showAnimationDone = true
    }
}


