package com.datn.thesocialnetwork.feature.post.comment.viewholder

import androidx.recyclerview.widget.DiffUtil

object CommentDiffCallback : DiffUtil.ItemCallback<CommentId>()
{
    override fun areItemsTheSame(
        oldItem: CommentId,
        newItem: CommentId
    ): Boolean =
        oldItem.first == newItem.first

    override fun areContentsTheSame(
        oldItem: CommentId,
        newItem: CommentId
    ): Boolean =
        oldItem.second == newItem.second
}