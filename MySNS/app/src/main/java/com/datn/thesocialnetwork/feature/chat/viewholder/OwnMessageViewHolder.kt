package com.datn.thesocialnetwork.feature.chat.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.datn.thesocialnetwork.data.repository.model.MessageModel
import com.datn.thesocialnetwork.data.repository.model.MessageType
import com.datn.thesocialnetwork.databinding.MessageOwnItemBinding
import com.datn.thesocialnetwork.feature.chat.adapter.MessageClickListener
import com.google.android.material.shape.CornerFamily

class OwnMessageViewHolder private constructor(
    private val binding: MessageOwnItemBinding,
) : MessageModelViewHolder<MessageOwnItemBinding>(binding) {
    companion object {
        fun create(parent: ViewGroup): OwnMessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = MessageOwnItemBinding.inflate(layoutInflater, parent, false)
            return OwnMessageViewHolder(
                binding
            ).apply {
                initViewHolder()
            }
        }
    }

    fun bind(
        message: MessageModel.OwnMessage,
        messageClickListener: MessageClickListener,
    ) {
        bindRoutine(message, messageClickListener)

        with(binding)
        {

            val b = cardView.shapeAppearanceModel.toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)

            when (message.type) {
                MessageType.FIRST -> {
                    b.setTopRightCornerSize(0f)
                }
                MessageType.MIDDLE -> {
                    b.setTopRightCornerSize(0f)
                    b.setBottomRightCornerSize(0f)
                }
                MessageType.LAST -> {
                    b.setBottomRightCornerSize(0f)
                }
                MessageType.SINGLE -> {

                }
            }

            cardView.shapeAppearanceModel = b.build()

        }

    }
}