package com.example.picturediary.navigation.model

import android.graphics.Bitmap
import androidx.room.*
import android.graphics.drawable.BitmapDrawable

@Entity(
    tableName = "ObjectDTO",
    primaryKeys = ["drawId", "objId"],
    foreignKeys = [
        ForeignKey(
            entity = DrawingDTO::class,
            parentColumns = ["drawId"],
            childColumns = ["drawId"]
        )
    ]
)
data class ObjectDTO(
    @ColumnInfo(name = "drawId") var drawId: Int? = null,
    @ColumnInfo(name = "objId") var objId: Int? = null,
    @ColumnInfo(name = "drawObj") var drawObj: Bitmap? = null,
    @ColumnInfo(name = "motion") var motion: String? = null
)
