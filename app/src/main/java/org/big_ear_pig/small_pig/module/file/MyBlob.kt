package org.big_ear_pig.small_pig.module.file

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_blob")
data class MyBlob(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0,

    // 用于存储较小的二进制数据，例如大小 ≤ 100KB
    @ColumnInfo(name = "small_blob")
    var smallBlob: ByteArray? = null,

    // 用于存储中等大小的二进制数据，例如 100KB < 大小 ≤ 1MB
    @ColumnInfo(name = "medium_blob")
    var mediumBlob: ByteArray? = null,

    // 用于存储较大的二进制数据，例如大小 > 1MB
    @ColumnInfo(name = "large_blob")
    var largeBlob: ByteArray? = null
) {
    // 为了正确比较 ByteArray，需要重写 equals/hashCode（data class 默认基于内容比较，但 Room 建议显式处理）
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MyBlob

        if (id != other.id) return false
        if (smallBlob != null) {
            if (!smallBlob.contentEquals(other.smallBlob)) return false
        } else if (other.smallBlob != null) return false
        if (mediumBlob != null) {
            if (!mediumBlob.contentEquals(other.mediumBlob)) return false
        } else if (other.mediumBlob != null) return false
        if (largeBlob != null) {
            if (!largeBlob.contentEquals(other.largeBlob)) return false
        } else if (other.largeBlob != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (smallBlob?.contentHashCode() ?: 0)
        result = 31 * result + (mediumBlob?.contentHashCode() ?: 0)
        result = 31 * result + (largeBlob?.contentHashCode() ?: 0)
        return result
    }
}