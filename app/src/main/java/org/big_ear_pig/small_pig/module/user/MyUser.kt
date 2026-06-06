package org.big_ear_pig.small_pig.module.user


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_user")
class MyUser  {
    @PrimaryKey
    @ColumnInfo(name = "c_id")
    var id: Long? = null
    @ColumnInfo(name = "c_username")
    var username: String? = null
    @ColumnInfo(name = "c_password")
    var password: String? = null
}