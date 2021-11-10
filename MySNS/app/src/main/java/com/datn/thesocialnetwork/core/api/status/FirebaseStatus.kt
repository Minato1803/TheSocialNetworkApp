package com.datn.thesocialnetwork.core.api.status

import com.datn.thesocialnetwork.core.api.Message

sealed class FirebaseStatus
{
    object Loading : FirebaseStatus()
    data class Success(val message: Message) : FirebaseStatus()
    data class Failed(val message: Message) : FirebaseStatus()
    object Sleep : FirebaseStatus()
}