package com.datn.thesocialnetwork.data.repository.model

import com.datn.thesocialnetwork.core.util.FirebaseNode

data class PostsModel(
    var commentCount: Int = 0,
    var content: String = "",
    var createdTime: Long = 0L,
    val image: HashMap<String, PostsImage>? = null,
    var ownerId: String = "",
    var reactCount: Int = 0,
    var shareCount: Int = 0,
    var updatedTime: Long = 0L,
)

data class PostsImage(
    val imageUrl: String? = null,
)
{
    var id: String = ""

    val toHashMap: HashMap<String, Any?>
        get() = hashMapOf(
            FirebaseNode.imagePost to imageUrl,
        )
}