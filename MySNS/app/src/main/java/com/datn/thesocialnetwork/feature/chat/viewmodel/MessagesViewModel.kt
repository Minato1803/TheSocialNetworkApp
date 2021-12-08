package com.datn.thesocialnetwork.feature.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datn.thesocialnetwork.core.api.status.FirebaseStatus
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.mapNeighbours
import com.datn.thesocialnetwork.data.repository.ChatRespository
import com.datn.thesocialnetwork.data.repository.FirebaseRepository
import com.datn.thesocialnetwork.data.repository.UserRepository
import com.datn.thesocialnetwork.data.repository.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val repository: ChatRespository,
    private val userRepository: UserRepository,
    private val firebaseRepository: FirebaseRepository,
) : ViewModel() {
    val messageText: MutableLiveData<String> = MutableLiveData()

    private val _allMassages: MutableStateFlow<GetStatus<List<MessageModel>>> = MutableStateFlow(
        GetStatus.Sleep
    )
    val allMassages = _allMassages.asStateFlow()

    private val _sendingMessageStatus: MutableStateFlow<FirebaseStatus> = MutableStateFlow(
        FirebaseStatus.Sleep
    )
    val sendingMessageStatus = _sendingMessageStatus.asStateFlow()

    lateinit var selectedUser: UserModel
    private lateinit var loggedUserId: String


    @ExperimentalCoroutinesApi
    fun initViewModel(user: UserModel) {
        this.selectedUser = user
        this.loggedUserId = firebaseRepository.requireUser.uid

        viewModelScope.launch {
            repository.getMessages(user.uidUser).collectLatest { getStatus ->
                _allMassages.value = when (getStatus) {
                    GetStatus.Sleep -> {
                        GetStatus.Sleep
                    }
                    GetStatus.Loading -> {
                        GetStatus.Loading
                    }
                    is GetStatus.Success -> {
                        val m: List<MessageModel> =
                            getStatus.data.mapNeighbours { previous, current, next ->

                                val type = getTypeFromSenders(
                                    previous?.sender,
                                    current.sender,
                                    next?.sender
                                )

                                if (current.sender == loggedUserId) {
                                    MessageModel.OwnMessage(current, type)
                                } else {
                                    MessageModel.OtherMessage(current, type, selectedUser)
                                }
                            }

                        GetStatus.Success(m)
                    }
                    is GetStatus.Failed -> {
                        getStatus
                    }
                }

            }
        }
    }

    @ExperimentalCoroutinesApi
    fun sendMessage() {
        messageText.value?.let {
            if (it.isNotBlank()) {
                val msg = ChatMessage(
                    textContent = it,
                    time = System.currentTimeMillis(),
                    sender = firebaseRepository.requireUser.uid,
                    isRead = "false"
                )

                viewModelScope.launch {
                    repository.sendMessage(selectedUser.uidUser, msg).collectLatest { status ->
                        _sendingMessageStatus.value = status
                    }
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun deleteMessage(message: ChatMessage) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMessage(selectedUser.uidUser, message)
        }
    }
}