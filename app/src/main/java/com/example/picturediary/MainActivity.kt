package com.example.picturediary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 일기 그리기 및 작성
        drawDiary.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        // 일기 공유 갤러리
        shareDiary.setOnClickListener {
            val intent = Intent(this, TimelineActivity::class.java)
            startActivity(intent)
        }
    }
}