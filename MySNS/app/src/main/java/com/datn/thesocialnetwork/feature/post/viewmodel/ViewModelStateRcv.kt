package com.datn.thesocialnetwork.feature.post.viewmodel

import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.data.repository.FirebaseRepository
import com.datn.thesocialnetwork.data.repository.PostRepository
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
abstract class ViewModelStateRcv constructor(
    repository: FirebaseRepository,
    postRepository: PostRepository
) : ViewModelPost(repository, postRepository)
{
    abstract val postToDisplay: Flow<GetStatus<List<PostWithId>>>

    open val tryAgain: (() -> Unit)? = null
}