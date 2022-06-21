package com.example.picturediary

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import dev.bmcreations.scrcast.ScrCast
import kotlinx.android.synthetic.main.activity_anim.*


class AnimExamActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anim)

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
                notification {
                    title = "Super cool library"
                    description = "shh session in progress"
                    icon = resources.getDrawable(R.drawable.ic_camcorder, null).toBitmap()
                    channel {
                        id = "1337"
                        name = "Recording Service"
                    }
                    showStop = true
                    showPause = true
                    showTimer = true
                }
                moveTaskToBack = false
                startDelayMs = 5_000
            }
        }

        recordBtn.setOnClickListener {
            if (recordBtn.text == "녹화") {
                recordBtn.text = "정지"
                recorder.record()
            }
            else {
                recordBtn.text = "정지"
                recorder.stopRecording()
            }
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
