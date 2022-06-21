package com.example.picturediary

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.picturediary.databinding.ItemChosenObjectBinding
import com.example.picturediary.navigation.model.ObjectDTO
import com.example.picturediary.navigation.model.ObjectFeatures
import kotlinx.android.synthetic.main.activity_crop.view.*
import kotlinx.android.synthetic.main.item_chosen_object.view.*
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.net.ConnectException
import kotlin.math.ceil
import kotlin.math.floor


@DelicateCoroutinesApi
class CropView(context: Context, attrs: AttributeSet) : View(context, attrs), OnTouchListener {
    private var canvasWidth: Int? = null
    private var canvasHeight: Int? = null
    private var currentObject: Int? = null
    private var objPath: Path? = null
    private var objLeft: Float? = null
    private var objRight: Float? = null
    private var objTop: Float? = null
    private var objBottom: Float? = null

    private var flgPathDraw = true
    private var points = arrayListOf<Point>()
    private var bitmap: Bitmap? = null
    private var paint: Paint? = null
    private var firstPointExist = false     // 첫 좌표 존재 여부
    private var mfirstpoint: Point? = null
    private var mlastpoint: Point? = null
    private var isNewObject = false

    val utils = Utils()
    val dbHelper = utils.createDBHelper(context)
    private var classifiedResult: String? = null
    private var pickedDate: String? = null
    private val loggedInUser = PrefApplication.prefs.getString("loggedInUser", "")
    private val username = loggedInUser.split("★")[0]

    private var objectArrayList = arrayListOf<ObjectDTO>()
    private var objectListAdapter: ObjectListAdapter? = null

    private val ani_rotate = AnimationUtils.loadAnimation(context, R.anim.rotate)
    private val ani_bounce = AnimationUtils.loadAnimation(context, R.anim.bounce)
    private val ani_shake = AnimationUtils.loadAnimation(context, R.anim.shake)


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

    // drawId 필드가 설정됨과 동시에 리사이클러뷰도 초기화
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

    fun setCurrentObject(position: Int) {
        currentObject = position
    }

    @SuppressLint("DrawAllocation")
    public override fun onDraw(canvas: Canvas) {
        canvasWidth = this.width
        canvasHeight = this.height

        canvas.drawBitmap(bitmap!!, 0f, 0f, null)

        val path = Path()
        var first = true
        var i = 0

        while (i < points.size) {
            val point: Point = points[i]
            when {
                first -> {
                    first = false
                    path.moveTo(point.x, point.y)     // 기준점 이동
                }
                i < points.size - 1 -> {
                    val next: Point = points[i + 1]
                    path.quadTo(point.x, point.y, next.x, next.y)   // 곡선
                }
                else -> {
                    mlastpoint = points[i]
                    path.lineTo(point.x, point.y)     // 경로 추가
                }
            }
            i += 2
        }
        canvas.drawPath(path, paint!!)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        var path: Path
        val point = Point()
        val constraintLayout = this.parent.parent as ConstraintLayout
        point.x = event.x
        point.y = event.y

        if (isNewObject) {
            firstPointExist = false
            isNewObject = false
        }

        if (flgPathDraw) {
            if (firstPointExist) {
                if (mfirstpoint == point) {
                    points.add(mfirstpoint!!)
                    flgPathDraw = false

                    path = getPath(points)
                    val objectFeatures = getObject(bitmap!!, path)
                    setObject(objectFeatures)     // 어댑터에 객체 추가
                    constraintLayout.recommendRecycler.visibility = INVISIBLE
                    constraintLayout.motionLinearLayout.visibility = INVISIBLE
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
                    points.add(mfirstpoint!!)

                    path = getPath(points)
                    val objectFeatures = getObject(bitmap!!, path)
                    setObject(objectFeatures)     // 어댑터에 객체 추가
                    constraintLayout.recommendRecycler.visibility = INVISIBLE
                    constraintLayout.motionLinearLayout.visibility = INVISIBLE
                    isNewObject = true
                }
            }
        }
        flgPathDraw = true
        invalidate()    // onDraw() 실행

        return true
    }

    private fun getPath(points: ArrayList<Point>): Path {
        val path = Path()
        for (point in points)
            path.lineTo(point.x, point.y)
        return path
    }

    // 선택한 객체만 추출
    private fun getObject(bitmap: Bitmap, path: Path): ObjectFeatures {
        // 전체 그림판에 선택된 객체
        val drawingInFullCanvas = Bitmap.createBitmap(crop_view.width, crop_view.height, bitmap.config)
        val paint = Paint()
        paint.isAntiAlias = true
        val canvas = Canvas(drawingInFullCanvas)     // 어댑터 캔버스

        canvas.drawPath(path, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        val leftMost = getLeftMost(points)
        val rightMost = getRightMost(points)
        val topMost = getTopMost(points)
        val bottomMost = getBottomMost(points)

        // 크롭한 이미지의 시작 좌표
        val startX = leftMost.x
        val startY = topMost.y

        // 크롭한 이미지의 크기
        val width = rightMost.x - leftMost.x
        val height = bottomMost.y - topMost.y

        // 선택된 객체 only
        val drawingOnly =
            Bitmap.createBitmap(
                drawingInFullCanvas,
                floor(startX).toInt(),
                floor(startY).toInt(),
                ceil(width).toInt(),
                ceil(height).toInt()
            )

        return ObjectFeatures(leftMost.x, rightMost.x, topMost.y, bottomMost.y, drawingInFullCanvas, drawingOnly)
    }

    // 선택한 객체를 어댑터에 추가
    @SuppressLint("NotifyDataSetChanged")
    private fun setObject(objectFeatures: ObjectFeatures) {
        val left = objectFeatures.left
        val right = objectFeatures.right
        val top = objectFeatures.top
        val bottom = objectFeatures.bottom
        val croppedBitmap = objectFeatures.drawObjWhole
        val croppedBitmapOnly = objectFeatures.originalDraw

        // 전체 그림판 기준에서의 객체
        val stream = ByteArrayOutputStream()
        croppedBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        // 크롭된 범위 기준에서의 객체
        val stream2 = ByteArrayOutputStream()
        croppedBitmapOnly!!.compress(Bitmap.CompressFormat.PNG, 100, stream2)
        val byteArray2 = stream2.toByteArray()

        val emptyBitmap =
            Bitmap.createBitmap(croppedBitmap.width, croppedBitmap.height, croppedBitmap.config)

        val objId = dbHelper.readLastObjectIndex(pickedDate!!, username)

        // 선택된 객체가 비어있지 않으면 내장 DB에 정보 저장
        if (!croppedBitmap.sameAs(emptyBitmap)) {
            val objectDTO = ObjectDTO()
            objectDTO.fullDraw = pickedDate
            objectDTO.objId = objId
            objectDTO.left = objLeft
            objectDTO.right = objRight
            objectDTO.top = objTop
            objectDTO.bottom = objBottom
            objectDTO.drawObjWhole = byteArray
            objectDTO.originalDraw = byteArray2

            objectArrayList.add(objectDTO)
            objectListAdapter?.notifyDataSetChanged()

            dbHelper.insertObject(
                "$username@$pickedDate", objId, left!!, right!!,
                top!!, bottom!!, byteArray, byteArray2
            )

            for (point in points)
                dbHelper.insertObjectPath("$username@$pickedDate", objId, point.x, point.y)
        }
        points.removeAll(points)
    }

    private fun hideRecommendRecycler() {
        val view = this.parent.parent as ConstraintLayout
        view.recommendRecycler.visibility = INVISIBLE
        view.motionLinearLayout.visibility = VISIBLE
    }

    private fun showRecommendRecycler() {
        val view = this.parent.parent as ConstraintLayout
        view.recommendRecycler.visibility = VISIBLE
        view.motionLinearLayout.visibility = INVISIBLE
    }

    // 인식된 객체의 다른 이미지 보기 어댑터
    @SuppressLint("NotifyDataSetChanged")
    private fun setRecommendAdapter(classifiedResult: String, original: Bitmap, drawingId: String, objId: String) {
        var drawId = drawingId
        if (!drawId.contains("@")) drawId = "$username@$drawId"

        val view = this.parent.parent as ConstraintLayout
        view.setLayerType(LAYER_TYPE_SOFTWARE, null)
        val objectDTO = dbHelper.readSingleObject(drawId, objId)

        // 어댑터 리스트 설정
        val originalArray = objectDTO.originalDraw
        val originalDraw = BitmapFactory.decodeByteArray(originalArray, 0, originalArray!!.size)
        val recommendListAdapter: RecommendListAdapter?
        val recommendArrayList = arrayListOf(originalDraw)

        val dogArrayList = utils.getBitmapFromDrawable(context, arrayListOf(R.drawable.dog1, R.drawable.dog2, R.drawable.dog3))
        val catArrayList = utils.getBitmapFromDrawable(context, arrayListOf(R.drawable.cat1, R.drawable.cat2, R.drawable.cat3))

        if (classifiedResult == "dog")
            recommendArrayList.addAll(dogArrayList)
        else if (classifiedResult == "cat")
            recommendArrayList.addAll(catArrayList)

        // 어댑터 설정
        recommendListAdapter = RecommendListAdapter(recommendArrayList)
        view.recommendRecycler.apply {
            view.recommendRecycler.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            view.recommendRecycler.adapter = recommendListAdapter
        }
        recommendListAdapter.notifyDataSetChanged()

        // 아이템 선택 시 모션 선택지 보이도록 함
        recommendListAdapter.setRecommendClickListener(object: RecommendListAdapter.RecommendClickListener {
            override fun onItemClick(position: Int) {
                val content = dbHelper.readDrawing(pickedDate!!, username)!!.content
                hideRecommendRecycler()

                // 선택된 객체 (원래 그림)
                objLeft = objectDTO.left
                objRight = objectDTO.right
                objTop = objectDTO.top
                objBottom = objectDTO.bottom
                objPath = getPath(dbHelper.readObjectPath(drawId, objId))

                // 대체할 이미지 --> 원래 그림 크기에 맞춤
                val selectedBitmap = recommendArrayList[position]
                val objWidth = (objRight!! - objLeft!!).toInt()
                val objHeight = (objBottom!! - objTop!!).toInt()
                val resizedBitmap = Bitmap.createScaledBitmap(selectedBitmap, objWidth, objHeight, false)
                val wholeBitmap = utils.placeObject(
                    resizedBitmap, canvasWidth!!, canvasHeight!!, objLeft!!, objTop!!
                )

                // 대체할 이미지 only --> byte array
                val replaceStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, replaceStream)
                val replaceData = replaceStream.toByteArray()
                if (position == 0) {
                    objectArrayList[currentObject!!].originalDraw = replaceData
                    objectArrayList[currentObject!!].replaceDraw = null
                }
                else {
                    objectArrayList[currentObject!!].replaceDraw = replaceData
                }
                objectListAdapter?.notifyDataSetChanged()

                // 대체할 이미지 위치 --> byte array
                val wholeStream = ByteArrayOutputStream()
                wholeBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, wholeStream)
                val wholeData = wholeStream.toByteArray()

                // 객체의 replaceDraw, drawObjWhole 속성 변경
                dbHelper.updateObjectReplaceDraw(drawId, objId, replaceData, wholeData)

                // 전체 이미지 그리기 & 해당 날짜의 그림 업데이트
                val userDrawing = Bitmap.createBitmap(canvasWidth!!, canvasHeight!!, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(userDrawing)
                canvas.drawBitmap(bitmap!!, 0f, 0f, null)

                val erase = Paint()
                erase.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
                erase.color = Color.TRANSPARENT
                erase.isAntiAlias = true
                canvas.drawPath(objPath!!, erase)

                bitmap = utils.overlay(userDrawing, wholeBitmap)
                val drawingStream = ByteArrayOutputStream()
                bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, drawingStream)
                val drawingData = drawingStream.toByteArray()
                dbHelper.updateDrawing(pickedDate!!, username, content!!, drawingData)

                addMotion(resizedBitmap)

                invalidate()
            }
        })
    }

    fun makeDig(objBitmap: Bitmap):ImageView {
        val view = this.parent.parent as  ConstraintLayout
        var customDialog = inflate(context, R.layout.custom_dialog, null)
        // 이미지 넣기
        val digImage = customDialog.findViewById<ImageView>(R.id.pre_image)
        digImage.setImageBitmap(objBitmap)
        val dig = AlertDialog.Builder(context)
        dig.setTitle("미리보기")
        dig.setView(customDialog)

        dig.setPositiveButton("적용",
            DialogInterface.OnClickListener { dialogInterface, i ->
                Toast.makeText(
                    context, "적용완료",
                    Toast.LENGTH_SHORT
                ).show()
            })
        dig.setNegativeButton("취소",
            DialogInterface.OnClickListener { dialogInterface, i ->
                Toast.makeText(
                    context, "취소되었습니다",
                    Toast.LENGTH_SHORT
                ).show()
            })

        dig.show()
        return digImage
    }

    fun addMotion(objBitmap: Bitmap){
        val view = this.parent.parent as  ConstraintLayout
        view.Abtn_bingle.setOnClickListener {
            makeDig(objBitmap).startAnimation(ani_rotate)
        }

        view.Abtn_jump.setOnClickListener {
            makeDig(objBitmap).startAnimation(ani_bounce)
        }

        view.Abtn_shake.setOnClickListener {
            makeDig(objBitmap).startAnimation(ani_shake)
        }
    }


    // CropView 파일에서 사용되는 선택된 객체 리스트 어댑터
    inner class ObjectListAdapter(var items: ArrayList<ObjectDTO>) : RecyclerView.Adapter<ObjectListAdapter.ViewHolder>() {
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
                if (!dbHelper.deleteObjectPath(drawId, objId))
                    dbHelper.deleteObjectPath("$username@$drawId", objId)
                items.removeAt(position)
                objectListAdapter?.notifyDataSetChanged()
                objectListAdapter?.notifyItemRemoved(position)
            }

            // 객체 인식
            holder.itemView.objectViewOnly.setOnClickListener {
                setCurrentObject(position)
                if (utils.checkWifi(context)) {
                    // 서버에 전송할 이미지 지정
                    val image = holder.itemView.objectViewOnly.drawable.toBitmap()
                    val resizedBitmap = Bitmap.createScaledBitmap(image, 224, 224, false)

                    CoroutineScope(Dispatchers.Default).launch {
                        // 서버가 정상 작동할 경우
                        try {
                            // ClassifyClient 객체
                            val socket = ClassifyClient()
                            socket.setClassifyImage(resizedBitmap)

                            // 인식 결과 보여주기
                            classifiedResult = socket.client()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, classifiedResult, Toast.LENGTH_SHORT).show()
                                setRecommendAdapter(classifiedResult!!, image, drawId, objId)
                                showRecommendRecycler()
                            }
                        }

                        // 서버가 꺼져 있을 경우
                        catch (e: ConnectException) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "현재 그림 인식은 할 수 없습니다", Toast.LENGTH_SHORT).show()
                                setRecommendAdapter("", image, drawId, objId)
                                showRecommendRecycler()
                            }
                        }
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding =
                ItemChosenObjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        inner class ViewHolder(private val binding: ItemChosenObjectBinding) : RecyclerView.ViewHolder(binding.root) {
            lateinit var bitmapOnly: Bitmap
            fun bind(item: ObjectDTO) {
                val bitmapWhole = BitmapFactory.decodeByteArray(item.drawObjWhole, 0, item.drawObjWhole!!.size)
                bitmapOnly = if (item.replaceDraw == null)
                    BitmapFactory.decodeByteArray(item.originalDraw, 0, item.originalDraw!!.size)
                else
                    BitmapFactory.decodeByteArray(item.replaceDraw, 0, item.replaceDraw!!.size)

                binding.objectParentId.text = item.fullDraw
                binding.objectId.text = item.objId.toString()
                binding.left.text = item.left.toString()
                binding.right.text = item.right.toString()
                binding.top.text = item.top.toString()
                binding.bottom.text = item.bottom.toString()
                binding.objectView.setImageBitmap(bitmapWhole)
                binding.objectViewOnly.setImageBitmap(bitmapOnly)
                binding.objectMotion.text = item.motion
            }
        }
    }

    //    * ──────→ +x축
    //    │
    //    │
    //    ↓ + y축

    // 가장 위에 있는 좌표 (y 값이 가장 작은 좌표)
    private fun getTopMost(pointArray: ArrayList<Point>): Point {
        var targetY = crop_view.height.toFloat()
        var topMost = Point()

        for (point in pointArray) {
            val pointY = point.y
            if (pointY < targetY) {
                targetY = pointY
                topMost = point
            }
        }
        return topMost
    }

    // 가장 밑에 있는 좌표 (y 값이 가장 큰 좌표)
    private fun getBottomMost(pointArray: ArrayList<Point>): Point {
        var targetY = 0f
        var bottomMost = Point()

        for (point in pointArray) {
            val pointY = point.y
            if (pointY > targetY) {
                targetY = pointY
                bottomMost = point
            }
        }
        return bottomMost
    }

    // 가장 왼쪽에 있는 좌표 (x 값이 가장 작은 좌표)
    private fun getLeftMost(pointArray: ArrayList<Point>): Point {
        var targetX = crop_view.width.toFloat()
        var leftMost = Point()

        for (point in pointArray) {
            val pointX = point.x
            if (pointX < targetX) {
                targetX = pointX
                leftMost = point
            }
        }
        return leftMost
    }

    // 가장 오른쪽에 있는 좌표 (x 값이 가장 큰 좌표)
    private fun getRightMost(pointArray: ArrayList<Point>): Point {
        var targetX = 0f
        var rightMost = Point()

        for (point in pointArray) {
            val pointX = point.x
            if (pointX > targetX) {
                targetX = pointX
                rightMost = point
            }
        }
        return rightMost
    }
}