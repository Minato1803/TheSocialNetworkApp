package com.datn.thesocialnetwork.feature.chat.adapter

import com.datn.thesocialnetwork.data.repository.model.ChatMessage

data class MessageClickListener(
    val copyText: (ChatMessage) -> Unit = {},
    val deleteMessage: (ChatMessage) -> Unit = {},
)
