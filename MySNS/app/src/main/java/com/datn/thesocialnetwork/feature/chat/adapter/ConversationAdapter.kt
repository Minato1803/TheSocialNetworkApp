package com.datn.thesocialnetwork.feature.chat.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import coil.ImageLoader
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.ChatRespository
import com.datn.thesocialnetwork.data.repository.UserRepository
import com.datn.thesocialnetwork.data.repository.model.ConversationItem
import com.datn.thesocialnetwork.feature.chat.viewholder.ConversationViewHolder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class ConversationAdapter @Inject constructor(
    private val imageLoader: ImageLoader,
    private val repository: ChatRespository
) : ListAdapter<ConversationItem, ConversationViewHolder>(ConversationDiffCallback)
{
    var actionMessageClick: (UserDetail) -> Unit = {}

    private fun cancelListeners(
        userListenerId: Int,
    )
    {
        repository.removeUserListener(userListenerId)
    }

    private val holders: MutableList<() -> Unit> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder =
        ConversationViewHolder.create(parent, ::cancelListeners, imageLoader = imageLoader).apply {
            holders.add(::cancelJobs)
        }

    @ExperimentalCoroutinesApi
    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) = holder.bind(
        conversationItem = getItem(position),
        actionConversationClick = actionMessageClick,
        userFlow = repository::getUser,
    )

    fun cancelScopes()
    {
        holders.forEach { cancelScope ->
            cancelScope()
        }
    }
}