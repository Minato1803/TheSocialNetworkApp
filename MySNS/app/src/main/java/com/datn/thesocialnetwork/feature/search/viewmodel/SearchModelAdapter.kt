package com.datn.thesocialnetwork.feature.search.viewmodel

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.data.repository.model.SearchModel
import com.datn.thesocialnetwork.data.repository.model.TagModel
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.feature.search.viewholder.TagViewHolder
import com.datn.thesocialnetwork.feature.search.viewholder.UserViewHolder
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SearchModelAdapter @Inject constructor(private val glide: RequestManager) :
    ListAdapter<SearchModel, RecyclerView.ViewHolder>(UserDiffCallback) {

    var userListener: ((UserModel) -> Unit)? = null
    var tagListener: ((TagModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.tag_item -> TagViewHolder.create(parent)
            R.layout.user_item -> UserViewHolder.create(parent)
            else -> throw IllegalArgumentException("Layout cannot be displayed in RecyclerView")
        }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is SearchModel.UserItem -> R.layout.user_item
            is SearchModel.TagItem -> R.layout.tag_item
            null -> throw UnsupportedOperationException("Unknown view")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position).let {
            when (it) {
                is SearchModel.TagItem -> (holder as TagViewHolder).bind(it.tag, tagListener)
                is SearchModel.UserItem -> it.user?.let { user ->
                    (holder as UserViewHolder).bind(
                        user,
                        userListener,
                        glide
                    )
                }
            }
        }
    }
}