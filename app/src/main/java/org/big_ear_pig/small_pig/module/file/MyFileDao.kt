package org.big_ear_pig.small_pig.module.file

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MyFileDao {

    // 插入单个文件
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: MyFile): Long

    // 插入多个文件
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<MyFile>)

    // 更新文件
    @Update
    suspend fun update(file: MyFile)

    // 删除文件
    @Delete
    suspend fun delete(file: MyFile)

    // 根据 ID 删除
    @Query("DELETE FROM my_file WHERE id = :id")
    suspend fun deleteById(id: Long)

    // 根据文件路径删除
    @Query("DELETE FROM my_file WHERE file_path = :filePath")
    suspend fun deleteByPath(filePath: String)

    // 清空所有文件
    @Query("DELETE FROM my_file")
    suspend fun deleteAll()

    // 根据 ID 查询单个文件
    @Query("SELECT * FROM my_file WHERE id = :id")
    suspend fun getFileById(id: Long): MyFile?

    // 根据文件路径查询
    @Query("SELECT * FROM my_file WHERE file_path = :filePath")
    suspend fun getFileByPath(filePath: String): MyFile?

    // 查询所有文件（按 sort_order 升序，再按 last_modified 降序）
    @Query("SELECT * FROM my_file ORDER BY sort_order ASC, last_modified DESC")
    fun getAllFilesSorted(): Flow<List<MyFile>>

    // 按文件后缀查询（例如 fileExtension = "jpg"）
    @Query("SELECT * FROM my_file WHERE file_extension = :extension ORDER BY sort_order ASC")
    fun getFilesByExtension(extension: String): Flow<List<MyFile>>

    // 批量更新排序字段（用于拖拽排序后更新多个项目的顺序）
    @Query("UPDATE my_file SET sort_order = :newOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, newOrder: Int)

    // 获取当前最大的排序值（用于新增时追加到最后）
    @Query("SELECT MAX(sort_order) FROM my_file")
    suspend fun getMaxSortOrder(): Int?
}