package com.datn.thesocialnetwork.feature.post.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import coil.ImageLoader
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.core.listener.PostClickListener
import com.datn.thesocialnetwork.data.repository.FirebaseRepository
import com.datn.thesocialnetwork.data.repository.PostRepository
import com.datn.thesocialnetwork.data.repository.UserRepository
import com.datn.thesocialnetwork.feature.post.viewholder.PostDiffCallback
import com.datn.thesocialnetwork.feature.post.viewholder.PostViewHolder
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
@ExperimentalCoroutinesApi
class PostAdapter @Inject constructor(
    private val imageLoader: ImageLoader,
    private val glide: RequestManager,
    private val repository: FirebaseRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
) : ListAdapter<PostWithId, PostViewHolder>(PostDiffCallback)
{
    lateinit var postClickListener: PostClickListener

    private fun cancelListeners(
        userListenerId: Int,
        likeListenerId: Int,
        commentListenerId: Int
    )
    {
        userRepository.removeUserListener(userListenerId)
        postRepository.removeLikeListener(likeListenerId)
        postRepository.removeCommentCounterListener(commentListenerId)
    }

    private val holders: MutableList<() -> Unit> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder =
        PostViewHolder.create(parent, ::cancelListeners).apply {
            holders.add(::cancelJobs)
        }

    @ExperimentalCoroutinesApi
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) = holder.bind(
        post = getItem(position),
        glide = glide,
        imageLoader = imageLoader,
        postClickListener = postClickListener,
        userFlow = userRepository::getUser,
        likeFlow = postRepository::getPostLikes,
        commentCounterFlow = postRepository::getCommentsCounter,
        loggedUserId = repository.loggedUser.value?.uid
    )

    fun cancelScopes()
    {
        holders.forEach { cancelScope ->
            cancelScope()
        }
    }
}