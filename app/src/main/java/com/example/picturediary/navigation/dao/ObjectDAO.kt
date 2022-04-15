//package com.example.picturediary.navigation.dao
//
//import androidx.lifecycle.LiveData
//import androidx.room.*
//import com.example.picturediary.navigation.model.ObjectDTO
//
//@Dao
//interface ObjectDAO {
//    @Query("SELECT * FROM ObjectDTO")
//    fun getAll(): LiveData<List<ObjectDTO>>
//
//    @Insert
//    fun insertAll(vararg ObjectDTO: ObjectDTO)
//
//    @Delete
//    fun delete(ObjectDTO: ObjectDTO)
//}