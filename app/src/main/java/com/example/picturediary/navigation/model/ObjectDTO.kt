package com.example.picturediary.navigation.model

import android.graphics.Bitmap
import androidx.room.*

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
    @ColumnInfo(name = "drawId") var drawId: String,
    @ColumnInfo(name = "objId") var objId: Int,
    @ColumnInfo(name = "drawObj") var drawObj: Bitmap? = null,
    @ColumnInfo(name = "motion") var motion: String? = null
)