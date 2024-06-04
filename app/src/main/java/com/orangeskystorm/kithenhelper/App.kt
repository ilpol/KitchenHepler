package com.orangeskystorm.kithenhelper

import android.app.Application
import androidx.room.Room
import com.orangeskystorm.kithenhelper.db.StoredFridgeItemsDataBase


class App : Application() {
    private var database: StoredFridgeItemsDataBase? = null
    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(this, StoredFridgeItemsDataBase::class.java, "database")
            .build()
    }

    fun getDatabase(): StoredFridgeItemsDataBase? {
        return database
    }

    companion object {
        var instance: App? = null
    }
}