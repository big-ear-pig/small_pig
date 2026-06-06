package org.big_ear_pig.small_pig.module.table

import androidx.room.*

@Dao
interface MyCellValueDao {

    @Insert
    suspend fun insert(cellValue: MyCellValue): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(cellValue: MyCellValue): Long

    @Update
    suspend fun update(cellValue: MyCellValue)

    @Delete
    suspend fun delete(cellValue: MyCellValue)

    @Query("SELECT * FROM my_cell_value WHERE c_id = :id")
    suspend fun getById(id: Long): MyCellValue?

    // 获取某一行所有单元格值
    @Query("SELECT * FROM my_cell_value WHERE c_row_id = :rowId")
    suspend fun getByRowId(rowId: Long): List<MyCellValue>

    // 获取某一列（通过列元数据ID）的所有值
    @Query("SELECT * FROM my_cell_value WHERE c_col_meta_id = :colMetaId")
    suspend fun getByColMetaId(colMetaId: Long): List<MyCellValue>

    // 获取指定行、指定列的值（联合唯一索引查询）
    @Query("SELECT * FROM my_cell_value WHERE c_row_id = :rowId AND c_col_meta_id = :colMetaId")
    suspend fun getCellValue(rowId: Long, colMetaId: Long): MyCellValue?

    // 删除某一行所有单元格
    @Query("DELETE FROM my_cell_value WHERE c_row_id = :rowId")
    suspend fun deleteByRowId(rowId: Long)

    // 删除某一列的所有单元格
    @Query("DELETE FROM my_cell_value WHERE c_col_meta_id = :colMetaId")
    suspend fun deleteByColMetaId(colMetaId: Long)

    @Query("DELETE FROM my_cell_value")
    suspend fun deleteAll()
}