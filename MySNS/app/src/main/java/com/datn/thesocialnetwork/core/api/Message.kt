package com.datn.thesocialnetwork.core.api

import android.content.Context
import androidx.annotation.StringRes

data class Message(@StringRes val text: Int,
                   val args: List<Any> = listOf(),
) {
    fun getFormattedMessage(context: Context): String =
        context.getString(text, *args.toTypedArray())
}