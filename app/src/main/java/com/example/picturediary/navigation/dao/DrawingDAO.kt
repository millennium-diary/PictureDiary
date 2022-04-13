package com.example.picturediary.navigation.dao

import androidx.room.*
import com.example.picturediary.navigation.model.DrawingDTO

@Dao
interface DrawingDAO {
    @Query("SELECT * FROM DrawingDTO")
    fun getAll(): ArrayList<DrawingDTO>

    @Insert
    fun insertAll(vararg drawingDTO: DrawingDTO)

    @Delete
    fun delete(drawingDTO: DrawingDTO)
}
