package com.kipliz.dr.entity

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

/**
 * @author hidayat
 * @since 04/08/18.
 */
@Database(entities = [MessageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageEntityDao(): MessageEntityDao

    companion object {

        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room
                        .databaseBuilder(context.applicationContext, AppDatabase::class.java, "salma_db")
                        .build()
            }
            return INSTANCE!!
        }
    }
}
