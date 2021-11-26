package com.datn.thesocialnetwork.feature.chat.group_chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.datn.thesocialnetwork.data.repository.ChatRespository
import com.datn.thesocialnetwork.data.repository.model.chat_room.RoomItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi
class CreateGroupViewModel @Inject constructor(
    private val repository: ChatRespository
) : ViewModel() {

}