package com.example.picturediary


import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_anim.*


class AnimExamActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anim)

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
            val animation = AnimationUtils.loadAnimation(this, R.anim.shake)
            imgView.startAnimation(animation)
        }

    }
}
