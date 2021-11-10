package com.datn.thesocialnetwork.data.repository.model

import java.io.Serializable

data class PostsModel(
    var id: String = "",
    var createdTime: Long = 0L,
    var ownerId: String = "",
    var content: String = "",
    var reactCount: Int = 0,
    var commentCount: Int = 0,
    var shareCount: Int = 0,
) : Serializable
