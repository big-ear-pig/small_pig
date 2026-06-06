package org.big_ear_pig.small_pig.module.file

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MyTextDao {
    @Insert
    suspend fun insertText(text: MyText)

    @Query("SELECT * FROM my_text WHERE id = :id")
    suspend fun getText(id: Long): MyText?
}