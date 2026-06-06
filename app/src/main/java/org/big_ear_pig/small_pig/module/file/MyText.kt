package org.big_ear_pig.small_pig.module.file

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_text")
data class MyText(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0,

    // 用于存储较短字符串，例如长度 ≤ 200
    @ColumnInfo(name = "small_text")
    var smallText: String? = null,

    // 用于存储中等长度字符串，例如 200 < 长度 ≤ 1000
    @ColumnInfo(name = "medium_text")
    var mediumText: String? = null,

    // 用于存储较长字符串，例如长度 > 1000
    @ColumnInfo(name = "large_text")
    var largeText: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MyText

        if (id != other.id) return false
        if (smallText != other.smallText) return false
        if (mediumText != other.mediumText) return false
        if (largeText != other.largeText) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (smallText?.hashCode() ?: 0)
        result = 31 * result + (mediumText?.hashCode() ?: 0)
        result = 31 * result + (largeText?.hashCode() ?: 0)
        return result
    }
}