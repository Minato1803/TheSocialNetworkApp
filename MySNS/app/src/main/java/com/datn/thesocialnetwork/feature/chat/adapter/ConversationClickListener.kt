package com.datn.thesocialnetwork.feature.chat.adapter

import com.datn.thesocialnetwork.data.repository.model.ChatMessage
import com.datn.thesocialnetwork.data.repository.model.Conversation

data class ConversationClickListener(
    val deleteConversation: (Conversation) -> Unit = {},
)
