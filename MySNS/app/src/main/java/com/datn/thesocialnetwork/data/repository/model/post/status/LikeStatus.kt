package com.datn.thesocialnetwork.data.repository.model.post.status

data class LikeStatus(
    val isPostLikeByLoggedUser: Boolean = false,
    val likeCounter: Long = 0L
)