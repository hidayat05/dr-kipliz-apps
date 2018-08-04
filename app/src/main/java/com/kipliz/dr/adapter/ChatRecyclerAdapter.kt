package com.kipliz.dr.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.kipliz.dr.R
import com.kipliz.dr.adapter.ChatRecyclerAdapter.MessageViewHolder
import com.kipliz.dr.entity.MessageEntity
import kotlinx.android.synthetic.main.my_message.view.txtMyMessage
import kotlinx.android.synthetic.main.my_message.view.txtMyMessageTime
import kotlinx.android.synthetic.main.other_message.view.txtOtherMessage
import kotlinx.android.synthetic.main.other_message.view.txtOtherMessageTime
import kotlinx.android.synthetic.main.other_message.view.txtOtherUser

/**
 * @author hidayat
 * @since 04/08/18.
 */
class ChatRecyclerAdapter(private val context: Context, private var listData: List<MessageEntity>)
    : RecyclerView.Adapter<MessageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_MY_MESSAGE = 1
        private const val VIEW_TYPE_OTHER_MESSAGE = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (listData[position].from == "tx") {
            VIEW_TYPE_OTHER_MESSAGE
        } else {
            VIEW_TYPE_MY_MESSAGE
        }
    }

    fun addData(listData: List<MessageEntity>) {
        this.listData = listData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return if (viewType == VIEW_TYPE_MY_MESSAGE) {
            MyMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.my_message, parent, false))
        } else {
            OtherMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.other_message, parent, false))
        }
    }

    override fun getItemCount(): Int = listData.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(listData[position])
    }


    inner class MyMessageViewHolder(view: View) : MessageViewHolder(view) {
        private var messageText: TextView = view.txtMyMessage
        private var timeText: TextView = view.txtMyMessageTime

        override fun bind(messageEntity: MessageEntity) {
            messageText.text = messageEntity.message
            timeText.text = messageEntity.timeStamp
        }
    }

    inner class OtherMessageViewHolder(view: View) : MessageViewHolder(view) {
        private var messageText: TextView = view.txtOtherMessage
        private var userText: TextView = view.txtOtherUser
        private var timeText: TextView = view.txtOtherMessageTime

        override fun bind(messageEntity: MessageEntity) {
            messageText.text = messageEntity.message
            userText.text = messageEntity.from
            timeText.text = messageEntity.timeStamp
        }
    }

    open class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        open fun bind(messageEntity: MessageEntity) {}
    }

}

