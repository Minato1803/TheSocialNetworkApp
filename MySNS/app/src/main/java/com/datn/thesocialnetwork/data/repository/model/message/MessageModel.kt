package com.datn.thesocialnetwork.data.repository.model

import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse

sealed class MessageModel(val chatMessage: ChatMessage, val type: MessageType)
{
    data class OwnMessage(val message: ChatMessage, val t: MessageType) : MessageModel(message, t)

    data class OtherMessage(
        val message: ChatMessage,
        val t: MessageType,
        val user: UserModel,
    ) : MessageModel(message, t)
}

enum class MessageType
{
    FIRST,
    MIDDLE,
    LAST,
    SINGLE
}

fun getTypeFromSenders(
    previous: String?,
    current: String,
    next: String?,
): MessageType
{
    when
    {
        previous == null ->
        {
            return when
            {
                next == null ->
                {
                    MessageType.SINGLE
                }
                current == next ->
                {
                    MessageType.FIRST
                }
                else ->
                {
                    MessageType.SINGLE
                }
            }
        }
        next == null ->
        {
            return when (previous)
            {
                current ->
                {
                    MessageType.LAST
                }
                else ->
                {
                    MessageType.SINGLE
                }
            }
        }
        else ->
        {
            return if (previous == current && current == next)
            {
                MessageType.MIDDLE
            }
            else if (previous != current && current == next)
            {
                MessageType.FIRST
            }
            else if (previous == current && current != next)
            {
                MessageType.LAST
            }
            else
            {
                MessageType.SINGLE
            }
        }
    }
}


fun View.setMessageMargins(
    messageType: MessageType,
    messageDefMargin: Int,
    messageSeparator: Int,
)
{

    when (messageType)
    {
        MessageType.FIRST ->
        {
            setPadding(
                messageDefMargin,
                messageDefMargin,
                messageDefMargin,
                messageSeparator
            )
        }
        MessageType.MIDDLE ->
        {
            setPadding(
                messageDefMargin,
                messageDefMargin,
                messageDefMargin,
                messageDefMargin
            )
        }
        MessageType.LAST ->
        {
            setPadding(
                messageDefMargin,
                messageSeparator,
                messageDefMargin,
                messageDefMargin
            )
        }
        MessageType.SINGLE ->
        {
            setPadding(
                messageDefMargin,
                messageSeparator,
                messageDefMargin,
                messageSeparator
            )
        }
    }
}