package com.example.picturediary

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.Image
import android.view.*
import android.widget.*
import com.example.picturediary.navigation.dao.DBHelper
import com.example.picturediary.navigation.model.DrawingDTO
import com.google.type.DateTime
import java.text.SimpleDateFormat
import java.util.*


class CalendarAdapter(context: Context): BaseAdapter() {
    private var mContext: Context? = context
    private var mDateManager: DateManager? = DateManager()
    private var dateArray: List<Date> = mDateManager!!.days
    private var mLayoutInflater: LayoutInflater? = LayoutInflater.from(mContext)

    private val dbHelper = Utils().createDBHelper(context)
    private var fullDrawing: DrawingDTO? = null
    private val loggedInUser = PrefApplication.prefs.getString("loggedInUser", "")
    private val username = loggedInUser.split("★")[0]

    // After expanding the custom cell, define Widget here
    private class ViewHolder {
        var dateText: TextView? = null
        var dateImg: ImageView? = null
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
            holder.dateImg = convertView.findViewById(R.id.dateImg)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        // Specify cell size
        val dp = mContext!!.resources.displayMetrics.density
        val params = AbsListView.LayoutParams(
            parent.width / 7 - dp.toInt(),
            (parent.height - dp.toInt() * mDateManager!!.weeks) / mDateManager!!.weeks
        )
        convertView!!.layoutParams = params

        // Display only the date
        val dateFormat = SimpleDateFormat("d", Locale.KOREA)
        holder.dateText!!.text = dateFormat.format(dateArray[position])
        holder.dateImg!!.setImageBitmap(null)

        val datetime = SimpleDateFormat("yyyy.MM.dd").format(dateArray[position])
        fullDrawing = dbHelper.readDrawing(datetime, username)
        // 해당 날짜에 저장된 그림이 있으면 해당 그림 띄우기
        if (fullDrawing != null) {
            val bitmap = BitmapFactory.decodeByteArray(fullDrawing!!.image, 0, fullDrawing!!.image!!.size)
            holder.dateImg!!.setImageBitmap(bitmap!!)
        }

        // 일요일 --> 빨강, 토요일 --> 파랑
        val colorId: Int = when (mDateManager!!.getDayOfWeek(dateArray[position])) {
            1 -> Color.parseColor("#F15F5F")
            7 -> Color.parseColor("#4374D9")
            else -> Color.parseColor("#8C8C8C")
        }
        holder.dateText!!.setTextColor(colorId)
        convertView.setBackgroundColor(Color.WHITE)

        // 이번 달 아니면 텍스트 색 연하게
        holder.dateText!!.alpha = 1f
        if (!(mDateManager!!.isCurrentMonth(dateArray[position]))) {
            holder.dateText!!.alpha = 0.4f
        }
        return convertView
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getItem(position: Int): String {
        val datetime = dateArray[position]
        return SimpleDateFormat("yyyy.MM.dd").format(datetime)
    }

    fun getTitle(): String? {
        val format = SimpleDateFormat("yyyy년 MM월", Locale.KOREA)
        return format.format(mDateManager!!.mCalendar.time)
    }

    // 두 날짜 간의 차이를 구하는 메소드
    // input: "yyyy-MM-dd" 형식의 String
    fun getDifferenceTwoDates(startDate: String, endDate: String) : Long{
        var result: Long = 0
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val firstDate = sdf.parse(startDate)
        val secondDate = sdf.parse(endDate)
        val firstTime = firstDate?.time ?: 0L
        val secondTime = secondDate?.time ?: 0L

        result = firstTime - secondTime
        return result
    }

//    fun getDifferenceTwoDates(startDate: String, endDate: String) : Long {
//
//    }

    // 다음 달
    fun nextMonth() {
        mDateManager!!.nextMonth()
        dateArray = mDateManager!!.days
        notifyDataSetChanged()
    }

    // 이전 달
    fun prevMonth() {
        mDateManager!!.prevMonth()
        dateArray = mDateManager!!.days
        notifyDataSetChanged()
    }
}