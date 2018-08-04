package com.kipliz.dr.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.Date

/**
 * @author hidayat
 * @since 04/08/18.
 */
@Entity
data class MessageEntity(
        @PrimaryKey(autoGenerate = true) val id: Int,
        val message: String,
        val from: String,
        val timeStamp: String
)