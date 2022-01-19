package com.example.picturediary

import java.text.SimpleDateFormat
import java.util.*


class DateManager {
    var mCalendar : Calendar = Calendar.getInstance()

    val days : List<Date>
        get() {
            val startDate = mCalendar.time
            val count = weeks * 7

            mCalendar[Calendar.DATE] = 1
            val dayOfWeek = mCalendar[Calendar.DAY_OF_WEEK] - 1
            mCalendar.add(Calendar.DATE, -dayOfWeek)
            val days: MutableList<Date> = ArrayList()
            for (i in 0 until count) {
                days.add(mCalendar.time)
                mCalendar.add(Calendar.DATE, 1)
            }

            //Restore state
            mCalendar.time = startDate
            return days
        }

    //Check if it is this month
    fun isCurrentMonth(date: Date?): Boolean {
        val format = SimpleDateFormat("yyyy년 MM월", Locale.KOREA)
        val currentMonth = format.format(mCalendar.time)
        return currentMonth == format.format(date)
    }

    //Get the number of weeks
    val weeks: Int
        get() = mCalendar.getActualMaximum(Calendar.WEEK_OF_MONTH)

    //Get the day of the week
    fun getDayOfWeek(date: Date?): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar[Calendar.DAY_OF_WEEK]
    }

    //To the next month
    fun nextMonth() {
        mCalendar.add(Calendar.MONTH, 1)
    }

    //To the previous month
    fun prevMonth() {
        mCalendar.add(Calendar.MONTH, -1)
    }

}