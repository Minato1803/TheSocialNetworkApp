package com.datn.thesocialnetwork.feature.post.detailpost.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.data.repository.FirebaseRepository
import com.datn.thesocialnetwork.data.repository.PostRepository
import com.datn.thesocialnetwork.data.repository.model.PostsModel
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.data.repository.model.post.status.LikeStatus
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.datn.thesocialnetwork.feature.post.viewmodel.ViewModelPost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class DetailPostViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    private val postRepository: PostRepository,
) : ViewModelPost(repository, postRepository)
{
    val requireUser = repository.requireUser

    private val _isInfoShown: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isInfoShown = _isInfoShown.asStateFlow()

    fun changeCollapse()
    {
        _isInfoShown.value = !_isInfoShown.value
    }

    private val _likeStatus: MutableStateFlow<GetStatus<LikeStatus>> = MutableStateFlow(GetStatus.Loading)
    val likeStatus = _likeStatus.asStateFlow()

    private val _userStatus: MutableStateFlow<GetStatus<UserModel>> = MutableStateFlow(GetStatus.Loading)
    val userStatus = _userStatus.asStateFlow()

    private val _commentStatus: MutableStateFlow<GetStatus<Long>> = MutableStateFlow(GetStatus.Loading)
    val commentStatus = _commentStatus.asStateFlow()

    private var userListenerId: Int = -1
    private var likeListenerId: Int = -1
    private var commentListenerId: Int = -1


    private val _post: MutableStateFlow<GetStatus<PostWithId>> = MutableStateFlow(GetStatus.Sleep)
    val post = _post.asStateFlow()


    fun initPost(postId: String)
    {
        Log.d("postItem","Get post $postId $post")
            viewModelScope.launch {
                postRepository.getPost(postId).collectLatest {
                    _post.value = it
                    if (it is GetStatus.Success)
                    {
                        getData(it.data)
                    }
                }
            }
    }

    private fun getData(post: PostWithId)
    {
        viewModelScope.launch {
            likeListenerId = FirebaseRepository.likeListenerId
            postRepository.getPostLikes(likeListenerId, post.first).collectLatest {
                _likeStatus.value = it
            }
        }

        viewModelScope.launch {
            userListenerId = FirebaseRepository.userListenerId
            repository.getUser(userListenerId, post.second.ownerId).collectLatest {
                _userStatus.value = it
            }
        }

        viewModelScope.launch {
            commentListenerId = FirebaseRepository.commentCounterListenerId
            postRepository.getCommentsCounter(commentListenerId, post.first).collectLatest {
                _commentStatus.value = it
            }
        }
    }

    override fun onCleared()
    {
        super.onCleared()
        repository.removeUserListener(userListenerId)
        postRepository.removeLikeListener(likeListenerId)
        postRepository.removeCommentCounterListener(commentListenerId)
    }
}