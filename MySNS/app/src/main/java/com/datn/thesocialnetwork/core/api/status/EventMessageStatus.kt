package com.datn.thesocialnetwork.core.api.status

import com.datn.thesocialnetwork.core.api.Event
import com.datn.thesocialnetwork.core.api.Message

sealed class EventMessageStatus
{
    object Sleep : EventMessageStatus()
    object Loading : EventMessageStatus()
    data class Success(val eventMessage: Event<Message>) : EventMessageStatus()
    data class Failed(val eventMessage: Event<Message>) : EventMessageStatus()
}