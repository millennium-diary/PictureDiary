package com.example.picturediary.navigation.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.*
import java.io.ByteArrayOutputStream

@Entity(tableName = "DrawingDTO")
data class DrawingDTO(
    @PrimaryKey(autoGenerate = true) val drawId: Int? = null,
    @ColumnInfo(name = "user") var user: String? = null,
    @ColumnInfo(name = "image") var image: Bitmap? = null,
    @ColumnInfo(name = "group") var group: String? = null
)

class Converters {
    // Bitmap -> ByteArray 변환
    @TypeConverter
    fun toByteArray(bitmap : Bitmap) : ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    // ByteArray -> Bitmap 변환
    @TypeConverter
    fun toBitmap(bytes : ByteArray) : Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

//    // List
//    @TypeConverter
//    fun listToJson(value: List<String>) : String? {
//        return gson.toJson(value)
//    }
//
//    @TypeConverter fun jsonToList(value: String): List<String> {
//        return gson.fromJson(value, Array<String>::class.java).toList()
//    }
}