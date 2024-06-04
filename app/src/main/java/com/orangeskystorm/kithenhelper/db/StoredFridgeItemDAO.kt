package com.orangeskystorm.kithenhelper.db

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface StoredFridgeItemDAO {

   @Query("SELECT * FROM storedFridgeItem")
   fun getAll(): List<StoredFridgeItem?>?


   @Query("SELECT * FROM storedFridgeItem WHERE id = :id")
   suspend fun getById(id: Long): StoredFridgeItem?


   @Insert
   suspend fun insert(employee: StoredFridgeItem?)

   @Update
   suspend fun update(employee: StoredFridgeItem?)

   @Delete
   suspend fun delete(employee: StoredFridgeItem?)

}