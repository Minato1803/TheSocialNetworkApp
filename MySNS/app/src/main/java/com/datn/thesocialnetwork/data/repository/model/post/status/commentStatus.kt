package com.datn.thesocialnetwork.data.repository.model.post.status

data class CommentStatus(
    val body: String = "",
    val owner: String = "",
    val time: Long = 0L
)