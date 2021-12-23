package com.datn.thesocialnetwork.feature.chat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.data.repository.ChatRespository
import com.datn.thesocialnetwork.data.repository.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class ChatViewModel @Inject constructor(
    private val repository: ChatRespository,
) : ViewModel() {

    private val _conversations: MutableStateFlow<GetStatus<MutableList<ConversationItem>>> =
        MutableStateFlow(
            GetStatus.Sleep
        )
    val conversation = _conversations.asStateFlow()

    fun getConversations() {
        viewModelScope.launch {
            repository.getAllConversations().collectLatest { status ->
                when (status) {
                    GetStatus.Sleep -> {
                        _conversations.value = GetStatus.Sleep
                    }
                    GetStatus.Loading -> {
                        _conversations.value = GetStatus.Loading
                    }
                    is GetStatus.Failed -> {
                        _conversations.value = GetStatus.Failed(status.message)
                    }
                    is GetStatus.Success -> {
                        _conversations.value =
                            GetStatus.Success(status.data.sortedByDescending { it.lastMessage.time }
                                .toMutableList())
                    }
                }
            }
        }
    }

    fun updateConversations() {
        viewModelScope.launch {
            repository.getAllConversations().collectLatest { status ->
                if (status is GetStatus.Success) {
                    _conversations.value =
                        GetStatus.Success(status.data.sortedByDescending { it.lastMessage.time }
                            .toMutableList())
                    Log.d("listConversation", "${_conversations.value.toString()}")
                }
            }
        }
    }

    fun seenLastMessage(user: UserModel, conversationItem: ConversationItem) {
        repository.seenLastMessage(user, conversationItem)
    }

    init {
        getConversations()
    }
}