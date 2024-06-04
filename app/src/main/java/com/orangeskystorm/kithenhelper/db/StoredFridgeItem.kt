package com.orangeskystorm.kithenhelper.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File
import java.nio.file.Path

@Entity
class StoredFridgeItem {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var name: String? = null
    var description: String? = null
    var imgUrl: String? = null
    var itemUri: String? = null
    var alarmTime: Long? = null
}