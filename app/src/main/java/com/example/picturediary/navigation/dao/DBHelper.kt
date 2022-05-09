package com.example.picturediary.navigation.dao

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.picturediary.navigation.model.DrawingDTO
import com.example.picturediary.navigation.model.ObjectDTO

class DBHelper(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
): SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db: SQLiteDatabase) {
        val createDrawingTable = "CREATE TABLE IF NOT EXISTS drawing (" +
                "drawId TEXT PRIMARY KEY," +
                "user TEXT," +
                "content TEXT," +
                "image BLOB );"

        val createObjectTable = "CREATE TABLE IF NOT EXISTS object (" +
                "fullDraw TEXT," +
                "objId INTEGER," +
                "drawObj BLOB," +
                "motion TEXT," +
                "FOREIGN KEY (fullDraw) REFERENCES drawing(drawId)," +
                "PRIMARY KEY (fullDraw, objId) );"

        val createShareTable = "CREATE TABLE IF NOT EXISTS share (" +
                "fullDraw TEXT," +
                "groupId TEXT," +
                "FOREIGN KEY (fullDraw) REFERENCES drawing(drawId)," +
                "PRIMARY KEY (fullDraw, groupId) );"

        db.execSQL(createDrawingTable)
        db.execSQL(createObjectTable)
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
    fun insertDrawing(drawDate: String, username:String, content: String, image: ByteArray): Boolean {
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

    fun deleteUsersDrawing(username: String) {
        val db = writableDatabase
        val sqlDelObject = "DELETE FROM object WHERE fullDraw = " +
                "(SELECT drawId FROM drawing WHERE user = '$username')"
        val sqlDelDrawing = "DELETE FROM drawing where user = '$username'"

        db.execSQL(sqlDelObject)
        db.execSQL(sqlDelDrawing)
    }


    // OBJECT 테이블 ================================================================================
    fun insertObject(fullDraw: String, objId: Int, drawObj: ByteArray, motion: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put("fullDraw", fullDraw)
        cv.put("objId", objId)
        cv.put("drawObj", drawObj)
        cv.put("motion", motion)

        return db.insert("object", null, cv) > 0
    }

    @SuppressLint("Recycle")
    fun readObjects(drawDate: String, username: String): ArrayList<ObjectDTO> {
        val drawId = "$username@$drawDate"
        val objectArrayList = arrayListOf<ObjectDTO>()
        val sql = "SELECT * FROM object WHERE fullDraw = ?"
        val cursor = readableDatabase.rawQuery(sql, arrayOf(drawId))

        while (cursor.moveToNext()) {
            val drawingId = cursor.getString(0)
            val objId = cursor.getInt(1)
            val drawObj = cursor.getBlob(2)
            val motion = cursor.getString(3)

            objectArrayList.add(ObjectDTO(drawingId, objId, drawObj, motion))
        }
        cursor.close()
        return objectArrayList
    }

    fun readLastIndex(drawDate: String, username: String): Int {
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

    fun deleteObject(drawId: String, objId: String):Boolean {
        val db = writableDatabase
        return db.delete("object", "fullDraw = ? AND objId = ?", arrayOf(drawId, objId)) > 0
    }

    fun deleteAllObject(drawDate: String, username: String): Boolean {
        val db = writableDatabase
        val drawId = "$username@$drawDate"
        return db.delete("object", "fullDraw = ?", arrayOf(drawId)) > 0
    }
}