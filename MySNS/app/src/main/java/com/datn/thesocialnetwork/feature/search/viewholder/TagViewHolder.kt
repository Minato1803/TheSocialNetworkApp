package com.datn.thesocialnetwork.feature.search.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.data.repository.model.TagModel
import com.datn.thesocialnetwork.databinding.TagItemBinding

class TagViewHolder private constructor(
    private val binding: TagItemBinding
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup): TagViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = TagItemBinding.inflate(layoutInflater, parent, false)
            return TagViewHolder(
                binding
            )
        }
    }


    fun bind(
        tag: TagModel,
        clickListener: ((TagModel) -> Unit)?
    )
    {
        with(binding)
        {
            txtTitle.text = binding.root.context.getString(R.string.tag_title_format, tag.title)
            txtCount.text = binding.root.context.getString(R.string.tag_counter_format, tag.count)

            clickListener?.let { click ->
                root.setOnClickListener {
                    click(tag)
                }
            }
        }
    }
}