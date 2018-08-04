package com.kipliz.dr.entity

import android.arch.persistence.room.OnConflictStrategy.REPLACE

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

/**
 * @author hidayat
 * @since 04/08/18.
 */
@Dao
interface MessageEntityDao {

    @get:Query("select * from MessageEntity")
    val allMessageEntity: LiveData<List<MessageEntity>>

    @Query("select * from MessageEntity where id = :id")
    fun getItemById(id: String): MessageEntity

    @Insert(onConflict = REPLACE)
    fun addMessageEntity(messageEntity: MessageEntity)

    @Delete
    fun deleteMessageEntity(messageEntity: MessageEntity)

}
