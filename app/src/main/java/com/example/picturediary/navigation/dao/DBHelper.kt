package com.example.picturediary.navigation.dao

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.picturediary.Point
import com.example.picturediary.navigation.model.DrawingDTO
import com.example.picturediary.navigation.model.ObjectDTO


class DBHelper(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    // 내장 데이터베이스 생성
    override fun onCreate(db: SQLiteDatabase) {
        val createDrawingTable = "CREATE TABLE IF NOT EXISTS drawing (" +
                "drawId TEXT PRIMARY KEY," +
                "user TEXT," +
                "content TEXT," +
                "image BLOB );"

        val createObjectTable = "CREATE TABLE IF NOT EXISTS object (" +
                "fullDraw TEXT," +
                "objId INTEGER," +
                "leftX REAL," +
                "rightX REAL," +
                "topY REAL," +
                "bottomY REAL," +
                "drawObjWhole BLOB," +
                "originalDraw BLOB," +
                "replaceDraw BLOB," +
                "motion TEXT," +
                "FOREIGN KEY (fullDraw) REFERENCES drawing(drawId)," +
                "PRIMARY KEY (fullDraw, objId) );"

        val createObjectPath = "CREATE TABLE IF NOT EXISTS path (" +
                "pathId INTEGER PRIMARY KEY AUTOINCREMENT," +
                "fullDraw TEXT," +
                "objId INTEGER," +
                "pointX REAL," +
                "pointY REAL," +
                "FOREIGN KEY (fullDraw) REFERENCES drawing(drawId)," +
                "FOREIGN KEY (objId) REFERENCES object(objId) );"

        val createShareTable = "CREATE TABLE IF NOT EXISTS share (" +
                "fullDraw TEXT," +
                "groupId TEXT," +
                "FOREIGN KEY (fullDraw) REFERENCES drawing(drawId)," +
                "PRIMARY KEY (fullDraw, groupId) );"

        db.execSQL(createDrawingTable)
        db.execSQL(createObjectTable)
        db.execSQL(createObjectPath)
        db.execSQL(createShareTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val dropDrawingTable = "DROP TABLE IF EXISTS drawing"
        val dropObjectTable = "DROP TABLE IF EXISTS object"
        val dropShareTable = "DROP TABLE IF EXISTS share"

        db.execSQL(dropDrawingTable)
        db.execSQL(dropObjectTable)
        db.execSQL(dropShareTable)

        onCreate(db)
    }

    // DRAWING 테이블 ===============================================================================
    // 사용자의 전체 그림 추가
    fun insertDrawing(
        drawDate: String,
        username: String,
        image: ByteArray
    ): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        val drawId = "$username@$drawDate"
        cv.put("drawId", drawId)
        cv.put("user", username)
        cv.put("content", "")
        cv.put("image", image)

        val result: Boolean = try {
            db.insert("drawing", null, cv)
            true
        } catch (e: SQLiteException) {
            false
        }
        return result
    }

    // 사용자의 전체 그림 읽기
    @SuppressLint("Recycle")
    fun readDrawing(drawDate: String, username: String): DrawingDTO? {
        var fullDrawingDTO: DrawingDTO? = null
        val sql = "SELECT * FROM drawing WHERE drawId = ?"
        val drawId = "$username@$drawDate"
        val cursor = readableDatabase.rawQuery(sql, arrayOf(drawId))

        while (cursor.moveToNext()) {
            val drawingId = cursor.getString(0)
            val user = cursor.getString(1)
            val content = cursor.getString(2)
            val image = cursor.getBlob(3)

            fullDrawingDTO = DrawingDTO(drawingId, user, content, image)
        }
        cursor.close()
        return fullDrawingDTO
    }

    // 사용자의 전체 그림 업데이트
    fun updateDrawing(drawDate: String, username: String, content: String, image: ByteArray): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        val drawId = "$username@$drawDate"
        cv.put("drawId", drawId)
        cv.put("user", username)
        cv.put("content", content)
        cv.put("image", image)

        return db.update("drawing", cv, "drawId = ?", arrayOf(drawId)) > 0
    }

    // 사용자의 그림 삭제
    fun deleteUsersDrawing(username: String) {
        val db = writableDatabase
        val sqlDelObject = "DELETE FROM object WHERE fullDraw = " +
                "(SELECT drawId FROM drawing WHERE user = '$username')"
        val sqlDelDrawing = "DELETE FROM drawing where user = '$username'"

        db.execSQL(sqlDelObject)
        db.execSQL(sqlDelDrawing)
    }


    // OBJECT 테이블 ================================================================================
    // 사용자가 선택한 객체 추가
    fun insertObject(
        fullDraw: String,
        objId: Int,
        leftX: Float,
        rightX: Float,
        topY: Float,
        bottomY: Float,
        drawObjWhole: ByteArray,
        originalDraw: ByteArray,
    ): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put("fullDraw", fullDraw)
        cv.put("objId", objId)
        cv.put("leftX", leftX)
        cv.put("rightX", rightX)
        cv.put("topY", topY)
        cv.put("bottomY", bottomY)
        cv.put("drawObjWhole", drawObjWhole)
        cv.put("originalDraw", originalDraw)

        return db.insert("object", null, cv) > 0
    }

    // 특정 그림의 모든 객체 읽기
    @SuppressLint("Recycle")
    fun readObjects(drawDate: String, username: String): ArrayList<ObjectDTO> {
        val drawId = "$username@$drawDate"
        val objectArrayList = arrayListOf<ObjectDTO>()
        val sql = "SELECT * FROM object WHERE fullDraw = ?"
        val cursor = readableDatabase.rawQuery(sql, arrayOf(drawId))

        while (cursor.moveToNext()) {
            val drawingId = cursor.getString(0)
            val objId = cursor.getInt(1)
            val leftX = cursor.getFloat(2)
            val rightX = cursor.getFloat(3)
            val topY = cursor.getFloat(4)
            val bottomY = cursor.getFloat(5)
            val drawObjWhole = cursor.getBlob(6)
            val originalDraw = cursor.getBlob(7)
            val replaceDraw = cursor.getBlob(8)
            val motion = cursor.getString(9)

            objectArrayList.add(
                ObjectDTO(
                    drawingId, objId, leftX, rightX, topY, bottomY,
                    drawObjWhole, originalDraw, replaceDraw, motion
                )
            )
        }
        cursor.close()
        return objectArrayList
    }

    // 그림의 마지막 객체 아이디 읽기
    fun readLastObjectIndex(drawDate: String, username: String): Int {
        var objectId: Int? = null
        val drawId = "$username@$drawDate"
        val sql = "SELECT * FROM object WHERE fullDraw = ? ORDER BY objId DESC LIMIT 1"
        val cursor = readableDatabase.rawQuery(sql, arrayOf(drawId))

        while (cursor.moveToNext()) {
            objectId = cursor.getInt(1)
        }
        cursor.close()

        if (objectId == null) objectId = 0
        else objectId += 1

        return objectId
    }

    // 특정 객체 하나 읽기
    fun readSingleObject(drawId: String, objId: String): ObjectDTO {
        var objectDTO = ObjectDTO()
        val sql = "SELECT * FROM object WHERE fullDraw = ? AND objId = ?"
        val cursor = readableDatabase.rawQuery(sql, arrayOf(drawId, objId))

        while (cursor.moveToNext()) {
            val drawingId = cursor.getString(0)
            val objectId = cursor.getInt(1)
            val leftX = cursor.getFloat(2)
            val rightX = cursor.getFloat(3)
            val topY = cursor.getFloat(4)
            val bottomY = cursor.getFloat(5)
            val drawObjWhole = cursor.getBlob(6)
            val originalDraw = cursor.getBlob(7)
            val replaceDraw = cursor.getBlob(8)
            val motion = cursor.getString(9)

            objectDTO = ObjectDTO(
                drawingId, objectId, leftX, rightX, topY, bottomY,
                drawObjWhole, originalDraw, replaceDraw, motion
            )
        }
        cursor.close()
        return objectDTO
    }

    // 객체의 대체 이미지 업데이트하기
    fun updateObjectReplaceDraw(drawId: String, objId: String, replaceDraw: ByteArray, wholeDraw: ByteArray): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put("fullDraw", drawId)
        cv.put("objId", objId)
        cv.put("drawObjWhole", wholeDraw)
        cv.put("replaceDraw", replaceDraw)

        return db.update("object", cv, "fullDraw = ? AND objId = ?", arrayOf(drawId, objId)) > 0
    }

    // 객체의 모션 업데이트하기
    fun updateObjectMotion(drawId: String, objId: String, motion: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put("fullDraw", drawId)
        cv.put("objId", objId)
        cv.put("motion", motion)

        return db.update("object", cv, "fullDraw = ? AND objId = ?", arrayOf(drawId, objId)) > 0
    }

    // 특정 객체 삭제
    fun deleteObject(drawId: String, objId: String): Boolean {
        val db = writableDatabase
        return db.delete("object", "fullDraw = ? AND objId = ?", arrayOf(drawId, objId)) > 0
    }

    // 모든 객체 삭제
    fun deleteAllObject(drawDate: String, username: String): Boolean {
        val db = writableDatabase
        val drawId = "$username@$drawDate"
        return db.delete("object", "fullDraw = ?", arrayOf(drawId)) > 0
    }

    // OBJECT PATH 테이블 ===========================================================================
    // 객체의 경로 추가
    fun insertObjectPath(fullDraw: String, objId: Int, pointX: Float, pointY: Float): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put("fullDraw", fullDraw)
        cv.put("objId", objId)
        cv.put("pointX", pointX)
        cv.put("pointY", pointY)

        return db.insert("path", null, cv) > 0
    }

    // 경로 읽기
    fun readObjectPath(drawId: String, objId: String): ArrayList<Point> {
        val pointArrayList = arrayListOf<Point>()
        val sql = "SELECT pointX, pointY FROM path WHERE fullDraw = ? AND objId = ?"
        val cursor = readableDatabase.rawQuery(sql, arrayOf(drawId, objId))

        while (cursor.moveToNext()) {
            val point = Point()
            point.x = cursor.getFloat(0)
            point.y = cursor.getFloat(1)

            pointArrayList.add(point)
        }
        cursor.close()
        return pointArrayList
    }

    // 경로 삭제
    fun deleteObjectPath(drawId: String, objId: String): Boolean {
        val db = writableDatabase
        return db.delete("path", "fullDraw = ? AND objId = ?", arrayOf(drawId, objId)) > 0
    }
}