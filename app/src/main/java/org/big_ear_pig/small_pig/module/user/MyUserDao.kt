package org.big_ear_pig.small_pig.module.user

import androidx.room.*

@Dao
interface MyUserDao {

    // 插入（如果主键冲突则报错，可设置 onConflict 策略）
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: MyUser)

    // 更新
    @Update
    suspend fun updateUser(user: MyUser)

    // 删除
    @Delete
    suspend fun deleteUser(user: MyUser)

    // 根据 id 查询（注意 id 可为 null，但查询条件会自动处理 null 值）
    @Query("SELECT * FROM my_user WHERE c_id = :id")
    suspend fun getUserById(id: Long?): MyUser?

    // 根据用户名查询
    @Query("SELECT * FROM my_user WHERE c_username = :username")
    suspend fun getUserByUsername(username: String): MyUser?

    // 获取所有用户
    @Query("SELECT * FROM my_user")
    suspend fun getAllUsers(): List<MyUser>

    // 检查用户名是否存在（用于注册时校验）
    @Query("SELECT COUNT(*) > 0 FROM my_user WHERE c_username = :username")
    suspend fun isUsernameExists(username: String): Boolean

    // 根据用户名和密码登录校验
    @Query("SELECT * FROM my_user WHERE c_username = :username AND c_password = :password")
    suspend fun login(username: String, password: String): MyUser?

    // 删除所有用户（清空表）
    @Query("DELETE FROM my_user")
    suspend fun deleteAllUsers()
}