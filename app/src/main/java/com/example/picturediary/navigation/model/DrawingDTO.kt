package com.example.picturediary.navigation.model

import android.graphics.Bitmap
import androidx.room.*
import android.graphics.drawable.BitmapDrawable

@Entity(tableName = "DrawingDTO")
data class DrawingDTO(
    @PrimaryKey(autoGenerate = true) val drawId: Int? = null,
    @ColumnInfo(name = "user") var user: String? = null,
    @ColumnInfo(name = "image") var image: Bitmap? = null,
    @ColumnInfo(name = "group") var group: String? = null
)