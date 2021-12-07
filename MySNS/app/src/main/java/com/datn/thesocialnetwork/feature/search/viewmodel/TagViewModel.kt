package com.datn.thesocialnetwork.feature.search.viewmodel

import androidx.lifecycle.viewModelScope
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.SystemUtils.normalize
import com.datn.thesocialnetwork.data.repository.FirebaseRepository
import com.datn.thesocialnetwork.data.repository.PostRepository
import com.datn.thesocialnetwork.data.repository.SearchRespository
import com.datn.thesocialnetwork.data.repository.model.TagModel
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.datn.thesocialnetwork.feature.post.viewmodel.ViewModelStateRcv
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class TagViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    private val postRepository: PostRepository,
    private val searchRespository: SearchRespository,
) : ViewModelStateRcv(repository, postRepository) {

    private val _tag: MutableStateFlow<GetStatus<TagModel>> = MutableStateFlow(GetStatus.Loading)
    val tag: StateFlow<GetStatus<TagModel>> = _tag
    private val _postToDisplay: MutableStateFlow<GetStatus<List<PostWithId>>> = MutableStateFlow(
        GetStatus.Loading
    )
    override val postToDisplay = _postToDisplay.asStateFlow()

    fun initTag(tagModel: TagModel) {

        val tags = tagModel.copy(title = tagModel.title.normalize())

        if (tags.count == -1L) {
            viewModelScope.launch {
                searchRespository.getTag(tags.title).collectLatest {
                    _tag.value = it
                }
            }
        } else {
            _tag.value = GetStatus.Success(tags)
        }

        viewModelScope.launch {
            searchRespository.getAllPostsFromTag(tags.title).collectLatest {
                _postToDisplay.value = it
            }
        }

    }
}