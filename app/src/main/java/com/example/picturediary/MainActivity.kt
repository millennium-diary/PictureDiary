package com.example.picturediary

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val finishTime: Long = 1000
    private var presstime: Long = 0

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
            val intent = Intent(this, FragmentActivity::class.java)
            startActivity(intent)
        }

//        animExam.setOnClickListener {
//            val intent = Intent(this, AnimExamActivity::class.java)
//            startActivity(intent)
//        }
    }

    override fun onBackPressed() {
        val tempTime = System.currentTimeMillis();
        val intervalTime = tempTime - presstime;

        if (intervalTime in 0..finishTime) {
            finish();
        } else {
            presstime = tempTime;
            Toast.makeText(applicationContext, "한번 더 누르시면 앱이 종료됩니다", Toast.LENGTH_SHORT).show();
        }
    }
}