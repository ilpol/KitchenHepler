package com.orangeskystorm.kithenhelper.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [StoredNotesItem::class], version = 3)
abstract class StoredNotesItemsDataBase : RoomDatabase() {
    abstract fun storedNotesItemDao(): StoredNotesItemDAO?
}