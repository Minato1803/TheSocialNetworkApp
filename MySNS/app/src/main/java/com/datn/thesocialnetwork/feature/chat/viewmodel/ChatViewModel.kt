package com.datn.thesocialnetwork.feature.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datn.thesocialnetwork.core.api.status.FirebaseStatus
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.mapNeighbours
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.ChatRespository
import com.datn.thesocialnetwork.data.repository.FirebaseRepository
import com.datn.thesocialnetwork.data.repository.UserRepository
import com.datn.thesocialnetwork.data.repository.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class ChatViewModel @Inject constructor(
    private val repository: ChatRespository
) : ViewModel()
{

    private val _conversations: MutableStateFlow<GetStatus<List<ConversationItem>>> = MutableStateFlow(
        GetStatus.Sleep
    )
    val conversation = _conversations.asStateFlow()

    @ExperimentalCoroutinesApi
    private fun getConversations()
    {
        viewModelScope.launch {
            repository.getAllConversations().collectLatest { status ->
                when (status)
                {
                    GetStatus.Sleep ->
                    {
                        _conversations.value = GetStatus.Sleep
                    }
                    GetStatus.Loading ->
                    {
                        _conversations.value = GetStatus.Loading
                    }
                    is GetStatus.Failed ->
                    {
                        _conversations.value = GetStatus.Failed(status.message)
                    }
                    is GetStatus.Success ->
                    {
                        _conversations.value = GetStatus.Success(status.data.sortedByDescending { it.lastMessage.time })
                    }
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun updateConversations()
    {
        viewModelScope.launch {
            repository.getAllConversations().collectLatest { status ->
                if (status is GetStatus.Success)
                {
                    _conversations.value = GetStatus.Success(status.data.sortedByDescending { it.lastMessage.time })
                }
            }
        }
    }

    init
    {
        getConversations()
    }
}