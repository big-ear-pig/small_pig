package org.big_ear_pig.small_pig.module.table

import androidx.room.*

@Dao
interface MyColumnMetaDao {

    @Insert
    suspend fun insert(columnMeta: MyColumnMeta): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(columnMeta: MyColumnMeta): Long

    @Update
    suspend fun update(columnMeta: MyColumnMeta)

    @Delete
    suspend fun delete(columnMeta: MyColumnMeta)

    // 根据 ID 查询
    @Query("SELECT * FROM my_column_meta WHERE c_id = :id")
    suspend fun getById(id: Long): MyColumnMeta?

    // 根据表元数据 ID 查询该表的所有列（按 order_index 排序）
    @Query("SELECT * FROM my_column_meta WHERE c_table_meta_id = :tableMetaId ORDER BY c_order_index ASC")
    suspend fun getColumnsByTableMetaId(tableMetaId: Long): List<MyColumnMeta>

    // 查询所有列
    @Query("SELECT * FROM my_column_meta")
    suspend fun getAll(): List<MyColumnMeta>

    // 根据列名模糊查询
    @Query("SELECT * FROM my_column_meta WHERE c_col_name LIKE '%' || :keyword || '%'")
    suspend fun searchByColName(keyword: String): List<MyColumnMeta>

    // 删除指定表的所有列
    @Query("DELETE FROM my_column_meta WHERE c_table_meta_id = :tableMetaId")
    suspend fun deleteByTableMetaId(tableMetaId: Long)

    @Query("DELETE FROM my_column_meta")
    suspend fun deleteAll()
}