package org.big_ear_pig.small_pig.module.file

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MyBlobDao {
    @Insert
    suspend fun insertBlob(blob: MyBlob)

    @Query("SELECT * FROM my_blob WHERE id = :id")
    suspend fun getBlob(id: Long): MyBlob?
}