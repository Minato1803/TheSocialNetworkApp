package com.datn.thesocialnetwork.feature.post.editpost.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.datn.thesocialnetwork.core.api.status.EventMessageStatus
import com.datn.thesocialnetwork.data.repository.FirebaseRepository
import com.datn.thesocialnetwork.data.repository.PostRepository
import com.datn.thesocialnetwork.data.repository.model.PostsImage
import com.datn.thesocialnetwork.data.repository.model.PostsModel
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class EditPostViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    private val postRepository: PostRepository
) : ViewModel() {
    val postWithId: MutableLiveData<PostWithId> = MutableLiveData()

    private val _basePost: MutableStateFlow<PostsModel> = MutableStateFlow(PostsModel())
    val basePost = _basePost.asStateFlow()

    private val _basePostImage: MutableStateFlow<List<Pair<String, PostsImage>>?> = MutableStateFlow(listOf())
    val basePostImage = _basePostImage.asStateFlow()

    private val _updatedPost: MutableStateFlow<PostsModel> = MutableStateFlow(PostsModel())
    val updatedPost = _updatedPost.asStateFlow()

    private val _isAnythingChanged = MutableStateFlow(false)
    val isAnythingChanged = _isAnythingChanged.asStateFlow()

    private lateinit var postId: String

    fun updateContent(content: String)
    {
        _updatedPost.value = _updatedPost.value.copy(content = content)
        _isAnythingChanged.value = _updatedPost.value != _basePost.value
    }

    fun initPost(post: PostWithId)
    {
        _basePost.value = post.second
        _basePostImage.value = post.third
        _updatedPost.value = post.second
        postId = post.first
    }

    @ExperimentalCoroutinesApi
    fun save(
        newHashtags: List<String>,
        newMentions: List<String>,
        oldHashtags: List<String>,
        oldMentions: List<String>,
    ): Flow<EventMessageStatus>
    {
        _basePost.value = _updatedPost.value
        _isAnythingChanged.value = _updatedPost.value != _basePost.value

        return editPost(
            postId,
            _updatedPost.value.content,
            newHashtags,
            newMentions,
            oldHashtags,
            oldMentions
        )
    }

    @ExperimentalCoroutinesApi
    fun editPost(
        postId: String,
        newDesc: String,
        newHashtags: List<String>,
        newMentions: List<String>,
        oldHashtags: List<String>,
        oldMentions: List<String>,
    ) = postRepository.editPost(postId, newDesc, newHashtags, newMentions, oldHashtags, oldMentions)
}