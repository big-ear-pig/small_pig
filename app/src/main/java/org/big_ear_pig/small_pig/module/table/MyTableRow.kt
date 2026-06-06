package org.big_ear_pig.small_pig.module.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "my_table_row",
    indices = [Index("c_table_id")]   // 仅为外键列建立索引
)
data class MyTableRow(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "c_id")
    var id: Long = 0,

    @ColumnInfo(name = "c_table_id")
    var tableId: Long = 0
)