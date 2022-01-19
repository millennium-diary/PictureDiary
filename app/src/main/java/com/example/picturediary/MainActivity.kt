// 참고 : https://linuxtut.com/en/0993c489e7ef1c0f969d/
package com.example.picturediary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mCalendarAdapter = CalendarAdapter(this)

        prevButton.setOnClickListener {
            mCalendarAdapter.prevMonth()
            titleText.text = mCalendarAdapter.getTitle()
        }
        nextButton.setOnClickListener {
            mCalendarAdapter.nextMonth()
            titleText.text = mCalendarAdapter.getTitle()
        }
        calendarGridView.adapter = mCalendarAdapter
        titleText.text = mCalendarAdapter.getTitle()
    }
}