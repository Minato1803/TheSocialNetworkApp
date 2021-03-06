package com.datn.thesocialnetwork.feature.post.viewholder

import androidx.recyclerview.widget.DiffUtil

object PostDiffCallback: DiffUtil.ItemCallback<PostWithId>()
{
    override fun areItemsTheSame(
        oldItem: PostWithId,
        newItem: PostWithId
    ): Boolean =
        oldItem.first == newItem.first

    override fun areContentsTheSame(
        oldItem: PostWithId,
        newItem: PostWithId
    ): Boolean =
        oldItem.second == newItem.second

}