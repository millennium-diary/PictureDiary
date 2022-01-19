package com.example.picturediary

import android.content.Context
import android.graphics.Color
import android.view.*
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*


class CalendarAdapter(context: Context?): BaseAdapter() {
    private var mContext: Context? = context
    private var mDateManager: DateManager? = DateManager()
    private var dateArray: List<Date> = mDateManager!!.days
    private var mLayoutInflater: LayoutInflater? = LayoutInflater.from(mContext)

    //After expanding the custom cell, define Wiget here
    private class ViewHolder {
        var dateText: TextView? = null
    }

    override fun getCount(): Int {
        return dateArray.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            convertView = mLayoutInflater!!.inflate(R.layout.calendar_cell, null)
            holder = ViewHolder()
            holder.dateText = convertView.findViewById(R.id.dateText)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        //Specify cell size
        val dp = mContext!!.resources.displayMetrics.density
        val params = AbsListView.LayoutParams(
            parent.width / 7 - dp.toInt(),
            (parent.height - dp.toInt() * mDateManager!!.weeks) / mDateManager!!.weeks
        )
        convertView!!.layoutParams = params

        // Display only the date
        val dateFormat = SimpleDateFormat("d", Locale.KOREA)
        holder.dateText!!.text = dateFormat.format(dateArray[position])

        // 일요일 --> 빨강, 토요일 --> 파랑
        val colorId: Int = when (mDateManager!!.getDayOfWeek(dateArray[position])) {
            1 -> Color.parseColor("#F15F5F")
            7 -> Color.parseColor("#4374D9")
            else -> Color.parseColor("#8C8C8C")
        }
        holder.dateText!!.setTextColor(colorId)
        convertView.setBackgroundColor(Color.WHITE)

        // 이번 달 아니면 텍스트 색 연하게
        if (!(mDateManager!!.isCurrentMonth(dateArray[position]))) {
            holder.dateText!!.alpha = 0.4f
        }

        return convertView
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    fun getTitle(): String? {
        val format = SimpleDateFormat("yyyy년 MM월", Locale.KOREA)
        return format.format(mDateManager!!.mCalendar.time)
    }

    fun nextMonth() {
        mDateManager!!.nextMonth()
        dateArray = mDateManager!!.days
        notifyDataSetChanged()
    }

    fun prevMonth() {
        mDateManager!!.prevMonth()
        dateArray = mDateManager!!.days
        notifyDataSetChanged()
    }
}