package com.example.picturediary.navigation.model

import android.graphics.Bitmap
import androidx.room.*

@Entity(
    tableName = "ObjectDTO",
    foreignKeys = [
        ForeignKey(
            entity = DrawingDTO::class,
            parentColumns = ["drawId"],
            childColumns = ["drawId"]
        )
    ]
)
data class ObjectDTO(
    @PrimaryKey(autoGenerate = true) val objId: Int,
    @ColumnInfo(name = "drawId") var drawId: Int,
    @ColumnInfo(name = "drawObj") var drawObj: Bitmap?,
    @ColumnInfo(name = "motion") var motion: String?
)
