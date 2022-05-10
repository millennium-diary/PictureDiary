// 참고 : https://linuxtut.com/en/0993c489e7ef1c0f969d/
package com.example.picturediary

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.calendar.*
import kotlinx.android.synthetic.main.calendar_cell.*
import java.text.SimpleDateFormat
import java.util.*


class CalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val mCalendarAdapter = CalendarAdapter(this)

        // 이전 달 이동 버튼
        // 이전 달 이동 버튼
        prevButton.setOnClickListener {
            mCalendarAdapter.prevMonth()
            titleText.text = mCalendarAdapter.getTitle()
        }

        // 다음 달 이동 버튼
        nextButton.setOnClickListener {
            mCalendarAdapter.nextMonth()
            titleText.text = mCalendarAdapter.getTitle()
        }

        calendarGridView.adapter = mCalendarAdapter
        titleText.text = mCalendarAdapter.getTitle()

        // 해당 날짜 선택 --> 그림판 이동
        calendarGridView.setOnItemClickListener { parent, view, position, id ->
            val pickedDate = mCalendarAdapter.getItem(position)
            val intent = Intent(this, DrawingActivity::class.java)
            intent.putExtra("pickedDate", pickedDate)

            val dateType = Date()
            val datetime = SimpleDateFormat("yyyy.MM.dd").format(dateType)
            if(mCalendarAdapter.getDifferenceTwoDates(pickedDate, datetime) > 0){
                Toast.makeText(this,"미래의 일기는 작성하실 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
            else {
                startActivity(intent)
            }
        }
    }
}