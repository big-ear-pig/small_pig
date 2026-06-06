package org.big_ear_pig.small_pig.module.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_table_meta")
data class MyTableMeta(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "c_id")
    var id: Long = 0,

    @ColumnInfo(name = "c_display_name")
    var displayName: String? = null
)