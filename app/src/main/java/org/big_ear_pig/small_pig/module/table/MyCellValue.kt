package org.big_ear_pig.small_pig.module.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "my_cell_value",
    indices = [
        Index("c_row_id"),
        Index("c_col_meta_id"),
        Index(value = ["c_row_id", "c_col_meta_id"], unique = true)  // 联合唯一索引，防止重复
    ]
)
data class MyCellValue(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "c_id")
    var id: Long = 0,

    @ColumnInfo(name = "c_row_id")
    var rowId: Long = 0,

    @ColumnInfo(name = "c_col_meta_id")
    var colMetaId: Long = 0,

    @ColumnInfo(name = "c_value_text")
    var valueText: String? = null
)