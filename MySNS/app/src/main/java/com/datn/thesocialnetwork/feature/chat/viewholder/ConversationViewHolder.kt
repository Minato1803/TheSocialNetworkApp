package com.datn.thesocialnetwork.feature.chat.viewholder

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.TimeUtils
import com.datn.thesocialnetwork.data.repository.FirebaseRepository
import com.datn.thesocialnetwork.data.repository.model.ConversationItem
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.databinding.ConversationItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ConversationViewHolder private constructor(
    private val binding: ConversationItemBinding,
    private val cancelListener: (Int) -> Unit,
    private val imageLoader: ImageLoader,
) : RecyclerView.ViewHolder(binding.root) {

    companion object {

        fun create(
            parent: ViewGroup,
            cancelListener: (Int) -> Unit,
            imageLoader: ImageLoader,
        ): ConversationViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemBinding.inflate(layoutInflater, parent, false)

            return ConversationViewHolder(
                binding,
                cancelListener,
                imageLoader
            )
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main)

    private var userJob: Job? = null
    private var userListenerId: Int = -1


    fun cancelJobs() {
        userJob?.cancel()
        cancelListener(userListenerId)
    }


    fun bind(
        conversationItem: ConversationItem,
        actionConversationClick: (UserModel, ConversationItem) -> Unit,
        userFlow: (Int, String) -> Flow<GetStatus<UserModel>>,
    ) {
        cancelJobs()

        userJob = scope.launch {
            userListenerId = FirebaseRepository.userListenerId
            userFlow(userListenerId, conversationItem.userId).collectLatest {
                setUserData(it)
            }
        }
        //if message unread
        if(conversationItem.lastMessage.isRead == 0 && conversationItem.lastMessage.sender != GlobalValue.USER!!.uidUser) {
            binding.imgUnreadDot.visibility = View.VISIBLE
            binding.txtLastMsg.typeface = Typeface.DEFAULT_BOLD
            binding.txtTime.typeface = Typeface.DEFAULT_BOLD
            binding.txtUsername.typeface = Typeface.DEFAULT_BOLD
        } else {
            binding.imgUnreadDot.visibility = View.GONE
            binding.txtLastMsg.typeface = Typeface.DEFAULT
            binding.txtTime.typeface = Typeface.DEFAULT
            binding.txtUsername.typeface = Typeface.DEFAULT
        }
        if(conversationItem.lastMessage.sender != GlobalValue.USER!!.uidUser)
            binding.txtLastMsg.text = conversationItem.lastMessage.textContent
        else
            binding.txtLastMsg.text = "Bạn: ${conversationItem.lastMessage.textContent}"
        binding.txtTime.text = TimeUtils.showTimeDetail(conversationItem.lastMessage.time)

        binding.root.setOnClickListener {
            loadedUser?.let { user ->
                actionConversationClick(user,conversationItem)
            }
        }
    }

    private var loadedUser: UserModel? = null

    private fun setUserData(
        status: GetStatus<UserModel>,
    ) {
        when (status) {
            is GetStatus.Failed -> {
                binding.imgAvatar.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_account_circle_24
                    )
                )
            }
            GetStatus.Loading -> {
                loadedUser = null
                binding.txtUsername.text = binding.root.context.getString(R.string.str_loading_dot)
            }
            is GetStatus.Success -> {
                loadedUser = status.data

                with(binding)
                {
                    txtUsername.text = status.data.userName

                    val request = ImageRequest.Builder(binding.root.context)
                        .data(status.data.avatarUrl)
                        .target { drawable ->
                            imgAvatar.setImageDrawable(drawable)
                        }
                        .build()

                    imageLoader.enqueue(request)
                }
            }
            GetStatus.Sleep -> Unit
        }
    }
}