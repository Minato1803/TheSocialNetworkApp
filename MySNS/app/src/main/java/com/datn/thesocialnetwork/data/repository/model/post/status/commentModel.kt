package com.datn.thesocialnetwork.data.repository.model.post.status

data class CommentModel(
    val ownerId: String = "",
    val content: String = "",
    val time: Long = 0L
)