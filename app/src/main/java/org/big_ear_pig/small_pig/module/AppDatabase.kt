package org.big_ear_pig.small_pig.module

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import org.big_ear_pig.small_pig.module.article.MyArticleDao
import org.big_ear_pig.small_pig.module.file.MyFileDao
import org.big_ear_pig.small_pig.module.log.MyLogDao
import org.big_ear_pig.small_pig.module.table.MyCellValueDao
import org.big_ear_pig.small_pig.module.table.MyColumnMetaDao
import org.big_ear_pig.small_pig.module.table.MyTableMetaDao
import org.big_ear_pig.small_pig.module.table.MyTableRowDao
import org.big_ear_pig.small_pig.module.user.MyUserDao
import org.big_ear_pig.small_pig.module.user.MyUser
import org.big_ear_pig.small_pig.module.log.MyLog
import org.big_ear_pig.small_pig.module.table.*
import org.big_ear_pig.small_pig.module.file.*
import org.big_ear_pig.small_pig.module.article.MyArticle
@Database(
    entities = [
        MyUser::class,
        MyLog::class,
        MyCellValue::class,
        MyColumnMeta::class,
        MyTableMeta::class,
        MyTableRow::class,
        MyArticle::class,
        MyFile::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun myUserDao(): MyUserDao
    abstract fun myLogDao(): MyLogDao
    abstract fun myCellValueDao(): MyCellValueDao
    abstract fun myColumnMetaDao(): MyColumnMetaDao
    abstract fun myTableMetaDao(): MyTableMetaDao
    abstract fun myTableRowDao(): MyTableRowDao
    abstract fun myArticleDao(): MyArticleDao
    abstract fun myFileDao(): MyFileDao
    companion object {
        const val NAME = "db"
        const val VERSION = 1

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NAME
                )
                    .addCallback(fun1())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun fun1(): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.beginTransaction()
                    try {
                        // 插入表元数据
                        val sql1 =
                            "INSERT OR IGNORE INTO my_table_meta (c_id, c_display_name) VALUES (1, '通讯录')"
                        db.execSQL(sql1)

                        // 姓名：宽100，表头高48，数据高56
                        val sql2 =
                            "INSERT OR IGNORE INTO my_column_meta (c_table_meta_id, c_col_name, c_data_type, c_default_value, c_order_index, c_common_width, c_header_height, c_data_height) VALUES (1, '姓名', 'TEXT', '', 1, 100, 48, 56)"
                        db.execSQL(sql2)

                        // 电话：宽120
                        val sql3 =
                            "INSERT OR IGNORE INTO my_column_meta (c_table_meta_id, c_col_name, c_data_type, c_default_value, c_order_index, c_common_width, c_header_height, c_data_height) VALUES (1, '电话', 'TEXT', '', 2, 120, 48, 56)"
                        db.execSQL(sql3)

                        // 邮箱：宽150
                        val sql4 =
                            "INSERT OR IGNORE INTO my_column_meta (c_table_meta_id, c_col_name, c_data_type, c_default_value, c_order_index, c_common_width, c_header_height, c_data_height) VALUES (1, '邮箱', 'TEXT', '', 3, 150, 48, 56)"
                        db.execSQL(sql4)

                        // 备注：宽200
                        val sql5 =
                            "INSERT OR IGNORE INTO my_column_meta (c_table_meta_id, c_col_name, c_data_type, c_default_value, c_order_index, c_common_width, c_header_height, c_data_height) VALUES (1, '备注', 'TEXT', '', 4, 200, 48, 56)"
                        db.execSQL(sql5)

                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
            }
        }
    }
}