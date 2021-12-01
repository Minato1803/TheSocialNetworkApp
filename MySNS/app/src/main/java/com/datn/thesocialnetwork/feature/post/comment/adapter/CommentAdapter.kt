package com.datn.thesocialnetwork.feature.post.comment.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import coil.ImageLoader
import com.datn.thesocialnetwork.feature.post.comment.viewholder.CommentDiffCallback
import com.datn.thesocialnetwork.feature.post.comment.viewholder.CommentId
import com.datn.thesocialnetwork.feature.post.comment.viewholder.CommentViewHolder
import javax.inject.Inject

class CommentAdapter @Inject constructor(
    private val imageLoader: ImageLoader,
) : ListAdapter<CommentId, CommentViewHolder>(CommentDiffCallback)
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder =
        CommentViewHolder.create(parent)

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) =
        holder.bind(
            comment = getItem(position),
            imageLoader = imageLoader
        )
}