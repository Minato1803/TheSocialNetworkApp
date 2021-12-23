package com.datn.thesocialnetwork.data.repository.model.post.status

data class SeenStatus(
    val isPostSeenByLoggedUser: Boolean = false,
    val seenCounter: Long = 0L
)
