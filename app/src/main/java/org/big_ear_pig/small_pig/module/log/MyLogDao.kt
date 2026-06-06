package org.big_ear_pig.small_pig.module.log

import androidx.room.*

@Dao
interface MyLogDao {
    @Insert
    suspend fun insertLog(log: MyLog)

    @Update
    suspend fun updateLog(log: MyLog)

    @Delete
    suspend fun deleteLog(log: MyLog)

    @Query("SELECT * FROM my_log WHERE c_id = :id")
    suspend fun getLogById(id: Long): MyLog?

    @Query("SELECT * FROM my_log ORDER BY c_timestamp DESC")
    suspend fun getAllLogs(): List<MyLog>
}