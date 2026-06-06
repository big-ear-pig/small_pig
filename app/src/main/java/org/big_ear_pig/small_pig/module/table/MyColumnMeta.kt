package org.big_ear_pig.small_pig.module.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "my_column_meta",
    indices = [Index("c_table_meta_id")]
)
data class MyColumnMeta(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "c_id")
    var id: Long = 0,

    @ColumnInfo(name = "c_table_meta_id")
    var tableMetaId: Long = 0,

    @ColumnInfo(name = "c_col_name")
    var colName: String? = null,

    @ColumnInfo(name = "c_data_type")
    var dataType: String? = null,

    @ColumnInfo(name = "c_default_value")
    var defaultValue: String? = null,

    @ColumnInfo(name = "c_order_index")
    var orderIndex: Int = 0,

    // 表头和数据字段共用的宽度，单位为dp
    @ColumnInfo(name = "c_common_width")
    var commonWidth: Float = 0f,

    // 表头字段高度，单位为dp
    @ColumnInfo(name = "c_header_height")
    var headerHeight: Float = 0f,

    // 数据字段高度，单位为dp
    @ColumnInfo(name = "c_data_height")
    var dataHeight: Float = 0f
)