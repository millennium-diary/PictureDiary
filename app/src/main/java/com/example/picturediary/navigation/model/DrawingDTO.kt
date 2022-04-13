package com.example.picturediary.navigation.model

import androidx.room.*
import android.graphics.drawable.BitmapDrawable

@Entity(tableName = "DrawingDTO")
data class DrawingDTO(
    @PrimaryKey(autoGenerate = true) val drawId: Int,
    @ColumnInfo(name = "user") var user: String,
    @ColumnInfo(name = "image") var image: BitmapDrawable?,
    @ColumnInfo(name = "group") var group: String?
)