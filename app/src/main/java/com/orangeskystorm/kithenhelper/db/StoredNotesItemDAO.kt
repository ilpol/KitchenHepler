package com.orangeskystorm.kithenhelper.db

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface StoredNotesItemDAO {

    @Query("SELECT * FROM storedNotesItem")
    fun getAll(): List<StoredNotesItem?>?


    @Query("SELECT * FROM storedNotesItem WHERE id = :id")
    suspend fun getById(id: Long): StoredNotesItem?


    @Insert
    suspend fun insert(employee: StoredNotesItem?)

    @Update
    suspend fun update(employee: StoredNotesItem?)

    @Delete
    suspend fun delete(employee: StoredNotesItem?)

}