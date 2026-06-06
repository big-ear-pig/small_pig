package org.big_ear_pig.small_pig.module.log

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_log")
class MyLog {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "c_id")
    var id: Long? = null

    @ColumnInfo(name = "c_message")
    var message: String? = null

    @ColumnInfo(name = "c_timestamp")
    var timestamp: Long? = null
}