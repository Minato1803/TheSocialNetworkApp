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
import com.datn.thesocialnetwork.data.repository.UserRepository
import com.datn.thesocialnetwork.data.repository.model.ChatMessage
import com.datn.thesocialnetwork.data.repository.model.MessageModel
import com.datn.thesocialnetwork.data.repository.model.getTypeFromSenders
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRespository
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

    private lateinit var selectedUser: UserResponse
    private lateinit var loggedUserId: String


    @ExperimentalCoroutinesApi
    fun initViewModel(user: UserResponse)
    {
        this.selectedUser = user
        this.loggedUserId = GlobalValue.USER!!.uidUser

        viewModelScope.launch {
            repository.getMessages(user.uidUser).collectLatest { getStatus ->
                _allMassages.value = when (getStatus)
                {
                    GetStatus.Sleep ->
                    {
                        GetStatus.Sleep
                    }
                    GetStatus.Loading ->
                    {
                        GetStatus.Loading
                    }
                    is GetStatus.Success ->
                    {
                        val m: List<MessageModel> = getStatus.data.mapNeighbours { previous, current, next ->

                            val type = getTypeFromSenders(
                                previous?.sender,
                                current.sender,
                                next?.sender
                            )

                            if (current.sender == loggedUserId)
                            {
                                MessageModel.OwnMessage(current, type)
                            }
                            else
                            {
                                MessageModel.OtherMessage(current, type, selectedUser)
                            }
                        }

                        GetStatus.Success(m)
                    }
                    is GetStatus.Failed ->
                    {
                        getStatus
                    }
                }

            }
        }
    }

    @ExperimentalCoroutinesApi
    fun sendMessage()
    {
        messageText.value?.let {
            if (it.isNotBlank())
            {
                val msg = ChatMessage(
                    textContent = it,
                    time = System.currentTimeMillis(),
                    imageUrl = null,
                    sender = GlobalValue.USER!!.uidUser
                )

                viewModelScope.launch {
                    repository.sendMessage(selectedUser.uidUser, msg).collectLatest { status ->
                        _sendingMessageStatus.value = status
                    }
                }
            }
        }
    }
}