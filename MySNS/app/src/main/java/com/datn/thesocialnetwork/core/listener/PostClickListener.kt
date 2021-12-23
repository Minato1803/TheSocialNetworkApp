package com.datn.thesocialnetwork.core.listener

import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId

interface PostClickListener {

    fun profileClick(postOwner: String)

    fun likeClick(postId: String, status: Boolean)

    fun commentClick(postId: String)

    fun markClick(postId: String, status: Boolean)

    fun likeCounterClick(postId: String)

    fun imageClick(postWithId: PostWithId)

    fun tagClick(tag: String)

    fun linkClick(link: String)

    fun mentionClick(mention: String)

    fun deletePostClick(post: PostWithId)

    fun menuEditClick(post: PostWithId)
}