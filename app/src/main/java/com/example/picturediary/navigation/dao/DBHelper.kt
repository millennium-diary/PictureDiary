package com.example.picturediary.navigation.dao

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
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

    fun insertDrawing(drawId: String, username:String, image: ByteArray): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put("drawId", drawId)
        cv.put("user", username)
        cv.put("image", image)

        return db.insert("drawing", null, cv) > 0
    }

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
    fun readObject(fullDraw: String): ArrayList<ObjectDTO> {
        val objectArrayList = arrayListOf<ObjectDTO>()
        val sql = "SELECT * FROM object WHERE fullDraw = ?"
        val cursor = readableDatabase.rawQuery(sql, arrayOf(fullDraw))

        while (cursor.moveToNext()) {
            val drawId = cursor.getString(0)
            val objId = cursor.getInt(1)
            val drawObj = cursor.getBlob(2)
            val motion = cursor.getString(3)

            objectArrayList.add(ObjectDTO(drawId, objId, drawObj, motion))
        }
        cursor.close()
        return objectArrayList
    }

//
//    fun getAllImages(): ArrayList<Image> {
//        val arr = arrayListOf<Image>()
//        val cursor = readableDatabase.rawQuery("SELECT * FROM drawing", null)
//
//        if (cursor.count > 0) {
//            cursor.moveToFirst()
//            while (!cursor.isAfterLast) {
//                val id = cursor.getString(0)
//                val user = cursor.getString(1)
//                val byteArray = cursor.getBlob(2)
//                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
//            }
//        }
//
//        cursor.close()
//        return arr
//    }
}