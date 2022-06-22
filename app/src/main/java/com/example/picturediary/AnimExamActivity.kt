package com.example.picturediary

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import dev.bmcreations.scrcast.ScrCast
import io.ktor.http.ContentDisposition.Companion.File
import kotlinx.android.synthetic.main.activity_anim.*
import kotlinx.android.synthetic.main.activity_show.*
import java.io.File
import java.text.SimpleDateFormat

public var videoUri : Uri? = null

class AnimExamActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anim)

        var videoName = ""

        // 불러올 때 사용할 파일명 생성
        fun newVideoName() : String {
            val sdf = SimpleDateFormat("MM_dd_yyyy_HHmmss")
            val filename = sdf.format(System.currentTimeMillis())
            return "${filename}.mp4"
        }

        val recorder = ScrCast.use(this) // activity required for media projection
        recorder.apply {
            // configure options via DSL
            options {
                video {
                    maxLengthSecs = 360
                }
                storage {
                    directoryName = "scrcast-sample"
                }
//                notification {
//                    title = "Super cool library"
//                    description = "shh session in progress"
//                    icon = resources.getDrawable(R.drawable.ic_camcorder, null).toBitmap()
//                    channel {
//                        id = "1337"
//                        name = "Recording Service"
//                    }
//                    showStop = true
//                    showPause = true
//                    showTimer = true
//                }
                moveTaskToBack = false
                startDelayMs = 5_000
            }
        }

        recordBtn.setOnClickListener {
            if (recordBtn.text == "녹화") {
                recordBtn.text = "정지"
                recorder.record()
                videoName = newVideoName()
            }
            else {
                recordBtn.text = "정지"
                recorder.stopRecording()

                var videoDir = "/storage/emulated/0/Movies/scrcast/"
                var videoFiles = File(videoDir).listFiles()
                var videoPath = videoFiles[videoFiles.lastIndex].toString()
                videoUri = Uri.parse(videoPath)
                Log.println(Log.INFO, "비디오 경로", videoUri.toString())
            }
        }

        showBtn.setOnClickListener {
            val intent = Intent(this, ShowExamActivity::class.java)
            startActivity(intent)
        }

        btnRotate.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.rotate)
            imgView.startAnimation(animation)
        }

        btnBounce.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
            imgView.startAnimation(animation)
        }

        btnFade.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.fadeinout)
            imgView.startAnimation(animation)
        }

        btnTwist.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.twist)
            imgView.startAnimation(animation)
        }
    }
}
