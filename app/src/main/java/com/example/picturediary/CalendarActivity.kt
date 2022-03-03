// 참고 : https://linuxtut.com/en/0993c489e7ef1c0f969d/
package com.example.picturediary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.calendar.*
import android.widget.Toast

import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView
import kotlinx.android.synthetic.main.calendar_cell.*


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

        calendarGridView.setOnItemClickListener { parent, view, position, id ->
            val element = mCalendarAdapter.getItemId(position)
            println(element)
            val intent = Intent(this, DrawingActivity::class.java)
            startActivity(intent)
        }
    }
}