package com.example.picturediary.navigation.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.picturediary.CropView
import com.example.picturediary.DrawingActivity
import com.example.picturediary.navigation.model.Converters
import com.example.picturediary.navigation.model.DrawingDTO
import com.example.picturediary.navigation.model.ObjectDTO

@Database(entities = [DrawingDTO::class, ObjectDTO::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drawingDAO(): DrawingDAO
    abstract fun objectDAO(): ObjectDAO

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            if(INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "PictureDiaryDB")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

}