package com.datn.thesocialnetwork.feature.post.viewmodel

import androidx.lifecycle.ViewModel
import com.datn.thesocialnetwork.data.repository.FirebaseRepository
import com.datn.thesocialnetwork.data.repository.PostRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
@ExperimentalCoroutinesApi
abstract class ViewModelPost(
    private val repository: FirebaseRepository,
    private val postRepository: PostRepository
) : ViewModel()
{
    fun isOwnAccountId(userId: String): Boolean = repository.isOwnAccountId(userId)
    fun isOwnAccountUsername(username: String): Boolean = repository.isOwnAccountName(username)
    fun setLikeStatus(postId: String, status: Boolean) = postRepository.likeDislikePost(postId, status)
    fun setMarkStatus(postId: String, status: Boolean) = postRepository.markUnmarkPost(postId, status)
    fun reportPost(postId: String, reportMessage: String) = postRepository.reportPost(postId, reportMessage)
    fun getUsersThatLikePost(postId: String) = postRepository.getUsersWhoLikePost(postId)
    fun getUsersThatSeenPost(postId: String) = postRepository.getUsersWhoSeenPost(postId)
    fun deletePost(postId: String) = postRepository.deletePost(postId)
    fun setSeenStatus(postId: String) = postRepository.seenPost(postId)
}