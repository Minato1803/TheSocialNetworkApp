package com.datn.thesocialnetwork.data.repository.model.post.status

data class CommentModel(
    val ownerId: String = "",
    val content: String = "",
    val createdTime: Long = 0L
)