package com.datn.thesocialnetwork.data.repository.model.message

import com.datn.thesocialnetwork.data.repository.model.ChatMessage

data class GroupChat (
    val userId: String,
    val lastMessage: ChatMessage
)