package com.datn.thesocialnetwork.core.api.status

import com.datn.thesocialnetwork.core.api.Message
import com.datn.thesocialnetwork.data.repository.model.MessageModel

sealed class GetStatus<out T>
{
    object Sleep : GetStatus<Nothing>()
    object Loading : GetStatus<Nothing>()
    data class Success<T>(val data: T) : GetStatus<T>()
    data class Failed(val message: Message) : GetStatus<Nothing>()
}