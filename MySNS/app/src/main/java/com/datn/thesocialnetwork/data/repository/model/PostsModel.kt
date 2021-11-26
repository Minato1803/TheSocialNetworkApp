package com.datn.thesocialnetwork.data.repository.model

import com.datn.thesocialnetwork.core.util.FirebaseNode

data class PostsModel(
    var createdTime: Long = 0L,
    var updatedTime: Long = 0L,
    var ownerId: String = "",
    var content: String = "",
    var reactCount: Int = 0,
    var commentCount: Int = 0,
    var shareCount: Int = 0,
    val image: HashMap<String, PostsImage>? = null,
)

data class PostsImage(
    val imageUrl: String = "",
)
{
    var id: String = ""

    val toHashMap: HashMap<String, Any?>
        get() = hashMapOf(
            FirebaseNode.imagePost to imageUrl,
        )
}