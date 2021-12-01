package com.datn.thesocialnetwork.core.api.status

import com.datn.thesocialnetwork.core.api.Message

sealed class DataStatus<out T>
{
    object Loading : DataStatus<Nothing>()
    data class Success<T>(val data: HashMap<String, T>) : DataStatus<T>()
    data class Failed(val message: Message) : DataStatus<Nothing>()
}