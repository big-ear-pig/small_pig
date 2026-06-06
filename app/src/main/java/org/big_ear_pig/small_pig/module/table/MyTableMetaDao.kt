package org.big_ear_pig.small_pig.module.table

import androidx.room.*

@Dao
interface MyTableMetaDao {

    // 插入
    @Insert
    suspend fun insert(tableMeta: MyTableMeta): Long

    // 插入或替换（冲突时替换）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(tableMeta: MyTableMeta): Long

    // 更新
    @Update
    suspend fun update(tableMeta: MyTableMeta)

    // 删除
    @Delete
    suspend fun delete(tableMeta: MyTableMeta)

    // 根据 ID 查询单个
    @Query("SELECT * FROM my_table_meta WHERE c_id = :id")
    suspend fun getById(id: Long): MyTableMeta?

    // 查询所有
    @Query("SELECT * FROM my_table_meta")
    suspend fun getAll(): List<MyTableMeta>

    // 根据显示名称模糊查询
    @Query("SELECT * FROM my_table_meta WHERE c_display_name LIKE '%' || :keyword || '%'")
    suspend fun searchByDisplayName(keyword: String): List<MyTableMeta>

    // 删除所有
    @Query("DELETE FROM my_table_meta")
    suspend fun deleteAll()

    @Query("SELECT * FROM my_table_meta LIMIT 1")
    suspend fun getFirstTableMeta(): MyTableMeta?
}