package com.datn.thesocialnetwork.data.repository.model

import com.datn.thesocialnetwork.core.util.FirebaseNode

data class Conversation(
    val msg: HashMap<String, ChatMessage>? = null,
    val u1: String = "",
    val u2: String = "",
)

data class ConversationItem(
    val userId: String,
    val lastMessage: ChatMessage
) // last message

data class ChatMessage(
    val textContent: String? = null,
    val time: Long = 0L,
    val imageUrl: String? = null,
    val sender: String = ""
)
{
    var id: String = ""

    val toHashMap: HashMap<String, Any?>
        get() = hashMapOf(
            FirebaseNode.messageContent to textContent?.trim(),
            FirebaseNode.messageImgUrl to imageUrl,
            FirebaseNode.messageTime to time,
            FirebaseNode.messageSender to sender,
        )
}