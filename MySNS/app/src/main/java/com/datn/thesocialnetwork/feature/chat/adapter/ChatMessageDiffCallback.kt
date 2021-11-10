package com.datn.thesocialnetwork.feature.chat.adapter

import androidx.recyclerview.widget.DiffUtil
import com.datn.thesocialnetwork.data.repository.model.MessageModel

object ChatMessageDiffCallback : DiffUtil.ItemCallback<MessageModel>()
{
    override fun areItemsTheSame(oldItem: MessageModel, newItem: MessageModel): Boolean =
        oldItem.chatMessage.id == newItem.chatMessage.id

    override fun areContentsTheSame(oldItem: MessageModel, newItem: MessageModel): Boolean =
        oldItem == newItem

}