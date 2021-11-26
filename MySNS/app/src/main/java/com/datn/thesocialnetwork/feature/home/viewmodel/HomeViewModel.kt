package com.datn.thesocialnetwork.feature.home.viewmodel

import androidx.lifecycle.viewModelScope
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.data.repository.FirebaseRepository
import com.datn.thesocialnetwork.data.repository.PostRepository
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.datn.thesocialnetwork.feature.post.viewmodel.ViewModelStateRcv
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class HomeViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    private val postRepository: PostRepository
) : ViewModelStateRcv(repository, postRepository)
{
    private val _postToDisplay: MutableStateFlow<GetStatus<List<PostWithId>>> = MutableStateFlow(
        GetStatus.Sleep
    )
    override val postToDisplay = _postToDisplay.asStateFlow()


    fun loadPosts()
    {
//        viewModelScope.launch {
//            postRepository.getPostsFromFollowers(GlobalValue.USER!!.uidUser).collectLatest {
//                _postToDisplay.value = it
//            }
//        }
    }

    init
    {
//        loadPosts()
    }

    override val tryAgain: (() -> Unit)
        get() = {

//            loadPosts()
        }
}