package com.datn.thesocialnetwork.feature.chat.viewholder

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.util.SystemUtils.isOnlyEmoji
import com.datn.thesocialnetwork.core.util.SystemUtils.px
import com.datn.thesocialnetwork.core.util.TimeUtils
import com.datn.thesocialnetwork.data.repository.model.MessageModel
import com.datn.thesocialnetwork.data.repository.model.setMessageMargins
import com.datn.thesocialnetwork.feature.chat.adapter.MessageClickListener
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

const val normalFontSize: Float = 14F
const val emojiFontSize: Float = 28F

abstract class MessageModelViewHolder<T>(
    private val binding: T
) : RecyclerView.ViewHolder(binding.root) where T : ViewBinding
{
    private lateinit var cardView: MaterialCardView
    private lateinit var txtTime: MaterialTextView
    private lateinit var txtBody: MaterialTextView

    protected fun initViewHolder()
    {
        radius = binding.root.context.resources.getDimension(R.dimen._12dp)
        messageDefMargin = (binding.root.context.resources.getInteger(R.integer.message_default_margin)).px
        messageSeparator = (binding.root.context.resources.getInteger(R.integer.message_separator)).px

        cardView = binding.root.findViewById(R.id.cardView)
        txtTime = binding.root.findViewById(R.id.txtTime)
        txtBody = binding.root.findViewById(R.id.txtBody)

        cardView.setOnClickListener {
            isTimeShown = !isTimeShown
        }

        cardView.setOnLongClickListener {
            return@setOnLongClickListener showPopupMenu(it)
        }
    }

    protected var radius: Float = 0F
    private var messageDefMargin: Int = 0
    private var messageSeparator: Int = 0

    private lateinit var messageClickListener: MessageClickListener
    protected lateinit var message: MessageModel

    private var isTimeShown: Boolean = false
        set(value)
        {
            field = value
            txtTime.isVisible = value
        }

    protected fun bindRoutine(
        message: MessageModel,
        messageClickListener: MessageClickListener,
    )
    {
        this.messageClickListener = messageClickListener
        this.message = message
        isTimeShown = false
        txtTime.text = TimeUtils.showTimeDetail(message.chatMessage.time)
        txtBody.text = message.chatMessage.textContent

        txtBody.textSize =
            if (message.chatMessage.textContent?.isOnlyEmoji == true)
                emojiFontSize
            else normalFontSize

        binding.root.setMessageMargins(message.type, messageDefMargin, messageSeparator)
    }

    fun showPopupMenu(view: View): Boolean
    {
        val popupMenu = PopupMenu(view.context, view)

        popupMenu.inflate(R.menu.message_dropdown)

        popupMenu.setOnMenuItemClickListener { menuItem ->

            return@setOnMenuItemClickListener when (menuItem.itemId)
            {
                R.id.mi_copy ->
                {
                    messageClickListener.copyText(message.chatMessage)
                    true
                }
                R.id.mi_delete ->
                {
                    messageClickListener.deleteMessage(message.chatMessage)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()

        return true
    }
}