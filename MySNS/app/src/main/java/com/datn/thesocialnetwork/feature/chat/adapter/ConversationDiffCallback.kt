package com.datn.thesocialnetwork.feature.chat.adapter

import androidx.recyclerview.widget.DiffUtil
import com.datn.thesocialnetwork.data.repository.model.ConversationItem

object ConversationDiffCallback : DiffUtil.ItemCallback<ConversationItem>()
{
    override fun areItemsTheSame(
        oldItem: ConversationItem,
        newItem: ConversationItem
    ): Boolean =
        oldItem.userId == newItem.userId

    override fun areContentsTheSame(
        oldItem: ConversationItem,
        newItem: ConversationItem
    ): Boolean =
        oldItem == newItem

}