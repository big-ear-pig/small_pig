package org.big_ear_pig.small_pig.module.article

import androidx.room.*

@Dao
interface MyArticleDao {

    // ========== 插入 ==========
    @Insert
    suspend fun insert(article: MyArticle): Long

    @Insert
    suspend fun insertAll(vararg articles: MyArticle)

    // ========== 更新 ==========
    @Update
    suspend fun update(article: MyArticle)

    // ========== 删除 ==========
    @Delete
    suspend fun delete(article: MyArticle)

    @Query("DELETE FROM my_article WHERE c_id = :id")
    suspend fun deleteById(id: Long)

    // ========== 查询 ==========
    @Query("SELECT * FROM my_article ORDER BY c_id DESC")
    suspend fun getAllArticles(): List<MyArticle>

    @Query("SELECT * FROM my_article WHERE c_id = :id")
    suspend fun getArticleById(id: Long): MyArticle?

    // 模糊搜索标题、简介或正文（不分页，全量返回）
    @Query("""
        SELECT * FROM my_article 
        WHERE title LIKE '%' || :keyword || '%' 
           OR summary LIKE '%' || :keyword || '%'
           OR content LIKE '%' || :keyword || '%'
        ORDER BY c_id DESC
    """)
    suspend fun searchArticles(keyword: String): List<MyArticle>

    // 分页查询（每页 limit 条，跳过 offset 条）
    @Query("SELECT * FROM my_article ORDER BY c_id DESC LIMIT :limit OFFSET :offset")
    suspend fun getArticlesPaged(limit: Int, offset: Int): List<MyArticle>

    // ========== 新增：分页搜索 ==========
    @Query("""
        SELECT * FROM my_article 
        WHERE title LIKE '%' || :keyword || '%' 
           OR summary LIKE '%' || :keyword || '%'
           OR content LIKE '%' || :keyword || '%'
        ORDER BY c_id DESC 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchArticlesPaged(keyword: String, limit: Int, offset: Int): List<MyArticle>
}