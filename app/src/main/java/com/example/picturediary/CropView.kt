package com.example.picturediary

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.picturediary.databinding.ItemChosenObjectBinding
import com.example.picturediary.navigation.model.ObjectDTO
import kotlinx.android.synthetic.main.activity_crop.view.*
import kotlinx.android.synthetic.main.item_chosen_object.view.*
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt


@DelicateCoroutinesApi
class CropView(context: Context, attrs: AttributeSet) : View(context, attrs), OnTouchListener {
    private var flgPathDraw = true
    private var points = arrayListOf<Point?>()
    private var bitmap: Bitmap? = null
    private var paint: Paint? = null
    private var firstPointExist = false     // 첫 좌표 존재 여부
    private var mfirstpoint: Point? = null
    private var mlastpoint: Point? = null
    private var isNewObject = false

    val dbHelper = Utils().createDBHelper(context)
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

    fun setDrawing(picture: Bitmap) {
        bitmap = picture
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDrawId(drawId: String) {
        pickedDate = drawId

        val view = this.parent.parent as ConstraintLayout
        objectArrayList = dbHelper.readObjects(pickedDate!!, username)
        objectListAdapter = ObjectListAdapter(objectArrayList)
        objectListAdapter?.notifyDataSetChanged()

        view.objectRecycler.apply {
            view.objectRecycler.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            view.objectRecycler.adapter = objectListAdapter
        }
    }

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
                    val (croppedImage, croppedImageOnly) = getObject(bitmap!!, path)
                    setObject(croppedImage, croppedImageOnly)     // 어댑터에 객체 추가
//                    points.removeAll(points)
                    isNewObject = true
                } else points.add(point)
            } else {
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
                    val (croppedImage, croppedImageOnly) = getObject(bitmap!!, path)
                    setObject(croppedImage, croppedImageOnly)     // 어댑터에 객체 추가
//                    points.removeAll(points)
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
//        points.removeAll(points)
        return path
    }

    // 선택한 객체만 추출
    private fun getObject(bitmap: Bitmap, path: Path): Pair<Bitmap, Bitmap> {
        // 전체 그림판에 선택된 객체
        val drawingInFullCanvas =
            Bitmap.createBitmap(crop_view.width, crop_view.height, bitmap.config)
        val paint = Paint()
        val canvas = Canvas(drawingInFullCanvas)     // 어댑터 캔버스

        canvas.drawPath(path, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        val topMost = getTopMost(points)
        val bottomMost = getBottomMost(points)
        val leftMost = getLeftMost(points)
        val rightMost = getRightMost(points)

        // 선택된 객체 only
        val drawingOnly =
            Bitmap.createBitmap(
                drawingInFullCanvas,
                leftMost.x.roundToInt(),
                topMost.y.roundToInt(),
                (rightMost.x - leftMost.x).roundToInt(),
                (bottomMost.y - topMost.y).roundToInt()
            )

        points.removeAll(points)

        return Pair(drawingInFullCanvas, drawingOnly)
    }

    // 선택한 객체를 어댑터에 추가
    @SuppressLint("NotifyDataSetChanged")
    fun setObject(croppedBitmap: Bitmap, croppedBitmapOnly: Bitmap) {
        val stream = ByteArrayOutputStream()
        croppedBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream)
        val byteArray = stream.toByteArray()

        val stream2 = ByteArrayOutputStream()
        croppedBitmapOnly.compress(Bitmap.CompressFormat.PNG, 50, stream2)
        val byteArray2 = stream2.toByteArray()

        val emptyBitmap =
            Bitmap.createBitmap(croppedBitmap.width, croppedBitmap.height, croppedBitmap.config)

        val objId = dbHelper.readLastIndex(pickedDate!!, username)

        // 선택된 객체가 비어있지 않으면 내장 DB에 정보 저장
        if (!croppedBitmap.sameAs(emptyBitmap)) {
            val objectDTO = ObjectDTO()
            objectDTO.fullDraw = pickedDate
            objectDTO.objId = objId
            objectDTO.drawObjWhole = byteArray
            objectDTO.drawObjOnly = byteArray2

            objectArrayList.add(objectDTO)
            objectListAdapter?.notifyDataSetChanged()

            dbHelper.insertObject("$username@$pickedDate", objId, byteArray, byteArray2, "")
        }
    }

    // 어댑터 내부 클래스
    inner class ObjectListAdapter(var items: ArrayList<ObjectDTO>) :
        RecyclerView.Adapter<ObjectListAdapter.ViewHolder>() {
        override fun getItemCount(): Int {
            return items.size
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])

            val drawId = items[position].fullDraw.toString()
            val objId = items[position].objId.toString()

            // 객체의 삭제 버튼
            holder.itemView.delete_object.setOnClickListener {
                if (!dbHelper.deleteObject(drawId, objId))
                    dbHelper.deleteObject("$username@$drawId", objId)
                items.removeAt(position)
                objectListAdapter?.notifyDataSetChanged()
                objectListAdapter?.notifyItemRemoved(position)
            }

            // 객체 인식
            holder.itemView.objectViewOnly.setOnClickListener {
                // ClassifyClient 객체
                val socket = ClassifyClient()

                // 서버에 전송할 이미지 설정
                val image = holder.itemView.objectViewOnly.drawable.toBitmap()
                val resizedBitmap = Bitmap.createScaledBitmap(image, 224, 224, false)
                socket.setClassifyImage(resizedBitmap)

                // 인식 결과 보여주기
                CoroutineScope(Dispatchers.Default).launch {
                    val result = socket.client()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding =
                ItemChosenObjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        inner class ViewHolder(private val binding: ItemChosenObjectBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(item: ObjectDTO) {
                val bitmapWhole =
                    BitmapFactory.decodeByteArray(item.drawObjWhole, 0, item.drawObjWhole!!.size)
                val bitmapOnly =
                    BitmapFactory.decodeByteArray(item.drawObjOnly, 0, item.drawObjOnly!!.size)
                binding.objectParentId.text = item.fullDraw
                binding.objectId.text = item.objId.toString()
                binding.objectView.setImageBitmap(bitmapWhole)
                binding.objectViewOnly.setImageBitmap(bitmapOnly)
                binding.objectMotion.text = item.motion
            }
        }
    }


    //    * ───────→ +x축
    //    │
    //    │
    //    ↓ + y축

    // 가장 위에 있는 좌표 (y 값이 가장 작은 좌표)
    private fun getTopMost(pointArray: ArrayList<Point?>): Point {
        var targetY = crop_view.height.toFloat()
        var topMost = Point()

        for (point in pointArray) {
            val pointY = point!!.y
            if (pointY < targetY) {
                targetY = pointY
                topMost = point
            }
        }
        return topMost
    }

    // 가장 밑에 있는 좌표 (y 값이 가장 큰 좌표)
    private fun getBottomMost(pointArray: ArrayList<Point?>): Point {
        var targetY = 0f
        var bottomMost = Point()

        for (point in pointArray) {
            val pointY = point!!.y
            if (pointY > targetY) {
                targetY = pointY
                bottomMost = point
            }
        }
        return bottomMost
    }

    // 가장 왼쪽에 있는 좌표 (x 값이 가장 작은 좌표)
    private fun getLeftMost(pointArray: ArrayList<Point?>): Point {
        var targetX = crop_view.width.toFloat()
        var leftMost = Point()

        for (point in pointArray) {
            val pointX = point!!.x
            if (pointX < targetX) {
                targetX = pointX
                leftMost = point
            }
        }
        return leftMost
    }

    // 가장 오른쪽에 있는 좌표 (x 값이 가장 큰 좌표)
    private fun getRightMost(pointArray: ArrayList<Point?>): Point {
        var targetX = 0f
        var rightMost = Point()

        for (point in pointArray) {
            val pointX = point!!.x
            if (pointX > targetX) {
                targetX = pointX
                rightMost = point
            }
        }
        return rightMost
    }
}