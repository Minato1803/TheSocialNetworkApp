package com.datn.thesocialnetwork.data.repository.model.post.status

data class MarkStatus(
    val isPostMarkByLoggedUser: Boolean = false,
    val markCounter: Long = 0L,
)
