package com.example.picturediary.navigation.model

import androidx.room.*
import android.graphics.drawable.BitmapDrawable

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
    @ColumnInfo(name = "drawObj") var drawObj: BitmapDrawable?,
    @ColumnInfo(name = "motion") var motion: String?
)
