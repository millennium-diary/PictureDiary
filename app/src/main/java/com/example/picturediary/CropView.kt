package com.example.picturediary

import android.annotation.SuppressLint
import android.content.*
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import android.view.View.OnTouchListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.picturediary.databinding.ChosenObjectItemBinding
import com.example.picturediary.navigation.dao.DBHelper
import com.example.picturediary.navigation.model.ObjectDTO
import kotlinx.android.synthetic.main.activity_crop.view.*
import kotlinx.android.synthetic.main.chosen_object_item.view.*
import java.io.ByteArrayOutputStream


class CropView(context: Context, attrs: AttributeSet) : View(context, attrs), OnTouchListener {
    private var flgPathDraw = true
    private var points = arrayListOf<Point?>()
    private var bitmap: Bitmap? = null
    private var paint: Paint? = null
    private var firstPointExist = false     // 첫 좌표 존재 여부
    private var mfirstpoint: Point? = null
    private var mlastpoint: Point? = null
    private var isNewObject = false

    private val dbName = "pictureDiary.db"
    private var dbHelper: DBHelper = DBHelper(context, dbName, null, 1)
    private var pickedDate: String? = null
    private val loggedInUser = PrefApplication.prefs.getString("loggedInUser", "")
    private val username = loggedInUser.split("★")[0]

    private var objectArrayList = arrayListOf<ObjectDTO>()
    private var objectListAdapter: ObjectListAdapter? = null

    init {
        initDrawing()
    }

    private fun initDrawing() {
        isFocusable = true
        isFocusableInTouchMode = true
        setOnTouchListener(this)
        firstPointExist = false
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.style = Paint.Style.STROKE
        paint!!.pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0F)
        paint!!.color = Color.MAGENTA
        paint!!.strokeWidth = 5f
    }

    fun setDrawing(picture: Bitmap) { bitmap = picture }
    fun setDrawId(drawId: String) { pickedDate = drawId }

    @SuppressLint("DrawAllocation")
    public override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
        val path = Path()
        var first = true
        var i = 0

        while (i < points.size) {
            val point: Point? = points[i]
            when {
                first -> {
                    first = false
                    path.moveTo(point!!.x, point.y)     // 기준점 이동
                }
                i < points.size - 1 -> {
                    val next: Point? = points[i + 1]
                    path.quadTo(point!!.x, point.y, next!!.x, next.y)   // 곡선
                }
                else -> {
                    mlastpoint = points[i]
                    path.lineTo(point!!.x, point.y)     // 경로 추가
                }
            }
            i += 2
        }
        canvas.drawPath(path, paint!!)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        var path: Path
        val point = Point()
        point.x = event.x
        point.y = event.y

        if (isNewObject) {
            firstPointExist = false
            isNewObject = false
        }

        if (flgPathDraw) {
            if (firstPointExist) {
                if (mfirstpoint == point) {
                    points.add(mfirstpoint)
                    flgPathDraw = false

                    path = getPath()
                    val croppedImage = getObject(bitmap!!, path)
                    setObject(croppedImage)     // 어댑터에 객체 추가
                    isNewObject = true
                }
                else points.add(point)
            }
            else {
                points.add(point)
                mfirstpoint = point
                firstPointExist = true
            }
        }

        if (event.action == MotionEvent.ACTION_UP) {
            mlastpoint = point
            if (flgPathDraw && points.size > 12) {
                if (mfirstpoint != point) {
                    flgPathDraw = false
                    points.add(mfirstpoint)

                    path = getPath()
                    val croppedImage = getObject(bitmap!!, path)
                    setObject(croppedImage)     // 어댑터에 객체 추가
                    isNewObject = true
                }
            }
        }
        flgPathDraw = true
        invalidate()    // onDraw() 실행

        return true
    }

    private fun getPath(): Path {
        val path = Path()
        for (point in points)
            path.lineTo(point!!.x, point.y)
        points.removeAll(points)
        return path
    }

    // 선택한 객체만 추출
    private fun getObject(bitmap: Bitmap, path: Path): Bitmap {
        val resultingImage = Bitmap.createBitmap(crop_view.width, crop_view.height, bitmap.config)
        val paint = Paint()
        val canvas = Canvas(resultingImage)     // 어댑터 캔버스

        canvas.drawPath(path, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return resultingImage
    }

    // 선택한 객체를 어댑터에 추가
    @SuppressLint("NotifyDataSetChanged")
    fun setObject(croppedBitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        croppedBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream)
        val byteArray = stream.toByteArray()

        val view = this.parent.parent as ConstraintLayout
        val emptyBitmap = Bitmap.createBitmap(croppedBitmap.width, croppedBitmap.height, croppedBitmap.config)

        objectArrayList = dbHelper.readObjects(pickedDate!!, username)
        objectListAdapter = ObjectListAdapter(objectArrayList)
        val objId = dbHelper.readLastIndex(pickedDate!!, username)
        objectListAdapter?.notifyDataSetChanged()

        view.objectRecycler.apply {
            view.objectRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            view.objectRecycler.adapter = objectListAdapter
        }

        if (!croppedBitmap.sameAs(emptyBitmap)) {
            val objectDTO = ObjectDTO()
            objectDTO.fullDraw = pickedDate
            objectDTO.objId = objId
            objectDTO.drawObj = byteArray
            objectArrayList.add(objectDTO)
            objectListAdapter?.notifyDataSetChanged()

            dbHelper.insertObject("$username@$pickedDate", objId, byteArray, "")
        }
    }

    // 어댑터 내부 클래스
    inner class ObjectListAdapter(var items: ArrayList<ObjectDTO>) : RecyclerView.Adapter<ObjectListAdapter.ViewHolder>() {
        override fun getItemCount(): Int {
            return items.size
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])

            val drawId = items[position].fullDraw.toString()
            val objId = items[position].objId.toString()
            holder.itemView.delete_object.setOnClickListener {
                items.removeAt(position)
                objectListAdapter?.notifyDataSetChanged()

                dbHelper.deleteObject(drawId, objId)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ChosenObjectItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        inner class ViewHolder(private val binding: ChosenObjectItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: ObjectDTO) {
                val bitmap = BitmapFactory.decodeByteArray(item.drawObj, 0, item.drawObj!!.size)
                binding.objectParentId.text = item.fullDraw
                binding.objectId.text = item.objId.toString()
                binding.objectView.setImageBitmap(bitmap)
                binding.objectMotion.text = item.motion
            }
        }
    }
}