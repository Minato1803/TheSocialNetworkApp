package com.datn.thesocialnetwork.feature.search.viewmodel

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.feature.post.viewholder.PostDiffCallback
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.datn.thesocialnetwork.feature.search.viewholder.SimplePostViewHolder
import javax.inject.Inject

class SimplePostAdapter @Inject constructor(
    private val glide: RequestManager,
) : ListAdapter<PostWithId, SimplePostViewHolder>(PostDiffCallback)
{
    var postListener: (PostWithId) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimplePostViewHolder =
        SimplePostViewHolder.create(parent)

    override fun onBindViewHolder(holder: SimplePostViewHolder, position: Int) =
        holder.bind(
            post = getItem(position),
            glide = glide,
            postListener = postListener
        )
}