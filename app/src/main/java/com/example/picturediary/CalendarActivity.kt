// 참고 : https://linuxtut.com/en/0993c489e7ef1c0f969d/
package com.example.picturediary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.calendar.*

class CalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar)

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