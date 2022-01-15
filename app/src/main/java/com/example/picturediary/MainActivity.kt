package com.example.picturediary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(), CalendarAdapter.OnItemListener {
    private lateinit var monthYearText : TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var selectedDate : LocalDate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initWidgets()
        selectedDate = LocalDate.now();
        setMonthView()
    }

    private fun initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        monthYearText = findViewById(R.id.monthYearTV)
    }

    private fun setMonthView() {
        monthYearText.text = monthYearFromDate(selectedDate).toString()
        val daysInMonth : ArrayList<String> = daysInMonthArray(selectedDate)

        val calendarAdapter : CalendarAdapter = CalendarAdapter(daysInMonth, this)
        calendarRecyclerView.layoutManager = GridLayoutManager(applicationContext, 7)
        calendarRecyclerView.adapter = calendarAdapter
    }

    private fun daysInMonthArray(date: LocalDate): ArrayList<String> {
        val daysInMonthArray : ArrayList<String> = ArrayList()
        val yearMonth : YearMonth = YearMonth.from(date)
        val daysInMonth : Int = yearMonth.lengthOfMonth()

        val firstOfMonth : LocalDate = selectedDate.withDayOfMonth(1)
        val dayOfWeek : Int = firstOfMonth.dayOfWeek.value

        for (i in 1..42) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek)
                daysInMonthArray.add("")
            else
                daysInMonthArray.add((i - dayOfWeek).toString())
        }
        return daysInMonthArray
    }

    private fun monthYearFromDate(date: LocalDate): Any {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월");
        return date.format(formatter)
    }

    fun previousMonthAction(view: android.view.View) {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView()
    }

    fun nextMonthAction(view: android.view.View) {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView()
    }

    override fun onItemClick(position: Int, dayText: String?) {
        if (dayText.equals("")) {
            val message : String = "Selected Date " + dayText + " " + monthYearFromDate(selectedDate)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}