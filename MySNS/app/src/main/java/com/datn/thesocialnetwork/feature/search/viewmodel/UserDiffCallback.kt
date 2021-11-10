package com.datn.thesocialnetwork.feature.search.viewmodel

import androidx.recyclerview.widget.DiffUtil
import com.datn.thesocialnetwork.data.repository.model.SearchModel

object UserDiffCallback : DiffUtil.ItemCallback<SearchModel>()
{
    override fun areItemsTheSame(oldItem: SearchModel, newItem: SearchModel): Boolean =
        if (oldItem is SearchModel.UserItem && newItem is SearchModel.UserItem)
        {
            oldItem.user.uidUser == newItem.user.uidUser
        }
        else if (oldItem is SearchModel.TagItem && newItem is SearchModel.TagItem)
        {
            oldItem.tag.title == newItem.tag.title
        }
        else
        {
            false
        }

    override fun areContentsTheSame(oldItem: SearchModel, newItem: SearchModel): Boolean =
        oldItem == newItem

}