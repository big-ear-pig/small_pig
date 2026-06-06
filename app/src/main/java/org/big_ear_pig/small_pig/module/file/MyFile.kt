package org.big_ear_pig.small_pig.module.file

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * 文件信息实体类，对应数据库表 my_file
 * @property id 自增主键
 * @property fileName 文件名（不含路径）
 * @property filePath 文件绝对路径
 * @property fileSize 文件大小（字节）
 * @property mimeType MIME类型，如 video/mp4，可为空
 * @property lastModified 最后修改时间戳（毫秒）
 * @property fileExtension 文件后缀名，小写不带点，如 "mp4"、"jpg"，可为空
 * @property sortOrder 自定义排序字段，数值越小越靠前
 * @property thumbnailPath 缩略图本地绝对路径，可为空（无缩略图时显示默认图标）
 */
@Entity(
    tableName = "my_file",
    indices = [
        Index(value = ["file_extension"]),   // 为后缀名添加索引，加速按类型查询
        Index(value = ["sort_order"])        // 为排序字段添加索引，加速排序查询
    ]
)
data class MyFile(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0,

    @ColumnInfo(name = "file_name")
    var fileName: String,

    @ColumnInfo(name = "file_path")
    var filePath: String,

    @ColumnInfo(name = "file_size")
    var fileSize: Long,

    @ColumnInfo(name = "mime_type")
    var mimeType: String? = null,

    @ColumnInfo(name = "last_modified")
    var lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "file_extension")
    var fileExtension: String? = null,

    @ColumnInfo(name = "sort_order")
    var sortOrder: Int = 0,

    @ColumnInfo(name = "thumbnail_path")
    var thumbnailPath: String? = null
)