package org.big_ear_pig.small_pig.module.article

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_article")
data class MyArticle(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "c_id")
    var id: Long? = null,

    @ColumnInfo(name = "title")
    var title: String = "",

    @ColumnInfo(name = "summary")
    var summary: String = "",

    @ColumnInfo(name = "content")
    var content: String = "",

    @ColumnInfo(name = "image_path")
    var imagePath: String = ""
)