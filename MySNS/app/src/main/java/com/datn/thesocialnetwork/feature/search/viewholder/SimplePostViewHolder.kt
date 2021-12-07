package com.datn.thesocialnetwork.feature.search.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.databinding.SimplePostItemBinding
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId

class SimplePostViewHolder private constructor(
    private val binding: SimplePostItemBinding
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup): SimplePostViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = SimplePostItemBinding.inflate(layoutInflater, parent, false)
            return SimplePostViewHolder(
                binding
            )
        }
    }

    fun bind(
        post: PostWithId,
        glide: RequestManager,
        postListener: (PostWithId) -> Unit,
    )
    {
        with(binding)
        {
            glide
                .load(post.third?.get(0)?.second?.imageUrl)
                .into(imgPost)

            imgPost.setOnClickListener {
                postListener(post)
            }
        }
    }

}