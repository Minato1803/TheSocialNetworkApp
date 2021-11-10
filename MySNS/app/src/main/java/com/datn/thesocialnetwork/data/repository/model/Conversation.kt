package com.datn.thesocialnetwork.data.repository.model

data class Conversation(
    val msg: HashMap<String, ChatMessage>? = null,
    val u1: String = "",
    val u2: String = "",
)