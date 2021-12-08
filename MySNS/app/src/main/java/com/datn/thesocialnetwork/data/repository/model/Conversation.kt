package com.datn.thesocialnetwork.data.repository.model

import com.datn.thesocialnetwork.core.util.FirebaseNode

data class Conversation(
    val msg: HashMap<String, ChatMessage>? = null,
    val u1: String = "",
    val u2: String = "",
//    val createdTime: Long = 0,
)

data class ConversationItem(
    val userId: String,
    val lastMessage: ChatMessage,
) // last message

data class ChatMessage(
    val isRead: String = "false",
    val sender: String = "",
    val textContent: String? = null,
    val time: Long = 0L,
) {
    var id: String = ""

    val toHashMap: HashMap<String, Any?>
        get() = hashMapOf(
            FirebaseNode.messageIsRead to isRead,
            FirebaseNode.messageSender to sender,
            FirebaseNode.messageContent to textContent?.trim(),
            FirebaseNode.messageTime to time,
        )
}