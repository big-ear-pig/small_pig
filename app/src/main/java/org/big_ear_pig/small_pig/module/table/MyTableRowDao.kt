package org.big_ear_pig.small_pig.module.table

import androidx.room.*

@Dao
interface MyTableRowDao {

    @Insert
    suspend fun insert(row: MyTableRow): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(row: MyTableRow): Long

    @Update
    suspend fun update(row: MyTableRow)

    @Delete
    suspend fun delete(row: MyTableRow)

    @Query("SELECT * FROM my_table_row WHERE c_id = :id")
    suspend fun getById(id: Long): MyTableRow?

    // 获取指定表的所有行
    @Query("SELECT * FROM my_table_row WHERE c_table_id = :tableId")
    suspend fun getRowsByTableId(tableId: Long): List<MyTableRow>

    // 获取所有行
    @Query("SELECT * FROM my_table_row")
    suspend fun getAll(): List<MyTableRow>

    // 删除指定表的所有行
    @Query("DELETE FROM my_table_row WHERE c_table_id = :tableId")
    suspend fun deleteByTableId(tableId: Long)

    @Query("DELETE FROM my_table_row")
    suspend fun deleteAll()

}