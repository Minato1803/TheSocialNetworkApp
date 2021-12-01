package com.datn.thesocialnetwork.feature.post.comment.viewmodel

import androidx.lifecycle.ViewModel
import com.datn.thesocialnetwork.data.repository.FirebaseRepository
import com.datn.thesocialnetwork.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class CommentViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel()
{
    @ExperimentalCoroutinesApi
    fun addComment(
        postId: String,
        comment: String
    ) = repository.addComment(postId, comment)

    @ExperimentalCoroutinesApi
    fun getComments(
        postId: String
    ) = repository.getComments(postId)

    override fun onCleared()
    {
        super.onCleared()
        repository.removeCommentListener()
    }
}