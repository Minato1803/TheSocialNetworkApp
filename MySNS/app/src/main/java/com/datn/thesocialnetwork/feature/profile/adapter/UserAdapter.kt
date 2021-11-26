package com.datn.thesocialnetwork.feature.profile.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import coil.ImageLoader
import com.datn.thesocialnetwork.data.repository.UserRepository
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.feature.profile.viewholder.UserViewHolderById
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class UserAdapter @Inject constructor(
    private val imageLoader: ImageLoader,
    private val repository: UserRepository
) : ListAdapter<String, UserViewHolderById>(UserDiffCallback)
{
    lateinit var userClick: (UserModel) -> Unit

    private val holders: MutableList<() -> Unit> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolderById =
        UserViewHolderById.create(
            parent = parent,
            cancelListeners = repository::removeUserListener,
            imageLoader = imageLoader
        ).apply {
            holders.add(::cancelJobs)
        }

    @ExperimentalCoroutinesApi
    override fun onBindViewHolder(holder: UserViewHolderById, position: Int) = holder.bind(
        userFlow = repository::getUser,
        userId = getItem(position),
        userClick = {
            userClick(it)
        }
    )

    fun cancelScopes()
    {
        holders.forEach { cancelScope ->
            cancelScope()
        }
    }
}