package com.kipliz.dr.model

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.os.AsyncTask
import com.kipliz.dr.entity.AppDatabase
import com.kipliz.dr.entity.MessageEntity

/**
 * @author hidayat
 * @since 04/08/18.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var itemAndPersonList: LiveData<List<MessageEntity>>
    private var appDatabase : AppDatabase = AppDatabase.getDatabase(this.getApplication())

    init {
        itemAndPersonList = appDatabase.messageEntityDao().allMessageEntity
    }

    fun getAllMessageEntity(): LiveData<List<MessageEntity>> {
        return itemAndPersonList
    }

    fun addData(messageEntity: MessageEntity) {
        Execute(appDatabase).execute(messageEntity)
    }

    private class Execute internal constructor(private val db: AppDatabase) : AsyncTask<MessageEntity, Void, Void>() {

        override fun doInBackground(vararg params: MessageEntity): Void? {
            db.messageEntityDao().addMessageEntity(params[0])
            return null
        }
    }
}