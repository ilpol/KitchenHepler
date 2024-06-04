package com.orangeskystorm.kithenhelper.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [StoredFridgeItem::class], version = 4)
abstract class StoredFridgeItemsDataBase : RoomDatabase() {
    abstract fun storedFridgeItemDao(): StoredFridgeItemDAO?
}