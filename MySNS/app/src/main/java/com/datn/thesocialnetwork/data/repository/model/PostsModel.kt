package com.datn.thesocialnetwork.data.repository.model

import android.os.Parcelable
import com.datn.thesocialnetwork.core.util.FirebaseNode
import kotlinx.android.parcel.Parcelize

data class PostsModel(
    var content: String = "",
    var createdTime: Long = 0L,
    val image: HashMap<String, PostsImage>? = null,
    var ownerId: String = "",
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