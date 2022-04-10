package com.example.picturediary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.diary_text.*

class TextActivity  : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.diary_text)

        confirmText.setOnClickListener {
            val intent = Intent(this, CropActivity::class.java)
            startActivity(intent)
        }
    }
}