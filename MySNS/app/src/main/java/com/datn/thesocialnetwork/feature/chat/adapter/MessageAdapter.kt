package com.datn.thesocialnetwork.feature.chat.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.data.repository.model.MessageModel
import com.datn.thesocialnetwork.feature.chat.viewholder.OtherMessageViewHolder
import com.datn.thesocialnetwork.feature.chat.viewholder.OwnMessageViewHolder
import javax.inject.Inject

class MessageAdapter @Inject constructor(
    private val glide: RequestManager,
) : ListAdapter<MessageModel, RecyclerView.ViewHolder>(ChatMessageDiffCallback) {

    lateinit var messageClickListener: MessageClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.message_other_item -> OtherMessageViewHolder.create(parent)
            R.layout.message_own_item -> OwnMessageViewHolder.create(parent)
            else -> throw IllegalArgumentException("Layout cannot be displayed in RecyclerView")
        }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is MessageModel.OwnMessage -> R.layout.message_own_item
            is MessageModel.OtherMessage -> R.layout.message_other_item
            else -> throw UnsupportedOperationException("Unknown view")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position).let {
            when (it) {
                is MessageModel.OtherMessage -> (holder as OtherMessageViewHolder).bind(
                    it,
                    messageClickListener,
                    glide
                )
                is MessageModel.OwnMessage -> (holder as OwnMessageViewHolder).bind(
                    it,
                    messageClickListener
                )
            }
        }
    }


}