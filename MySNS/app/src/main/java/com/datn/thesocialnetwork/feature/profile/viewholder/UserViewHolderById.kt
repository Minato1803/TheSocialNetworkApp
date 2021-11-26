package com.datn.thesocialnetwork.feature.profile.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.data.repository.FirebaseRepository
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.databinding.UserItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserViewHolderById private constructor(
    private val binding: UserItemBinding,
    private val cancelListeners: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(
            parent: ViewGroup,
            cancelListeners: (Int) -> Unit,
            imageLoader: ImageLoader
        ): UserViewHolderById
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = UserItemBinding.inflate(layoutInflater, parent, false)

            return UserViewHolderById(
                binding,
                cancelListeners,
            ).apply {
                this.imageLoader = imageLoader
            }
        }
    }

    private lateinit var imageLoader: ImageLoader


    private val scope = CoroutineScope(Dispatchers.Main)
    private var userJob: Job? = null
    private var userListenerId: Int = -1

    fun cancelJobs()
    {
        userJob?.cancel()
        cancelListeners(userListenerId)
    }

    private lateinit var userClick: (UserModel) -> Unit

    fun bind(
        userId: String,
        userFlow: (Int, String) -> Flow<GetStatus<UserModel>>,
        userClick: (UserModel) -> Unit
    )
    {
        cancelJobs()

        userJob = scope.launch {
            userListenerId = FirebaseRepository.userListenerId
            userFlow(userListenerId, userId).collectLatest {
                setUserData(it)
            }
        }
        this.userClick = userClick

    }

    private fun setUserData(
        status: GetStatus<UserModel>,
    )
    {
        when (status)
        {
            is GetStatus.Failed ->
            {
                binding.imgAvatar.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_account_circle_24
                    )
                )
                binding.txtUsername.text = binding.root.context.getString(R.string.error)
                binding.txtFullName.text = ""
            }
            GetStatus.Loading ->
            {
                binding.txtUsername.text = binding.root.context.getString(R.string.str_loading_dot)
                binding.txtFullName.text = ""
            }
            is GetStatus.Success ->
            {
                with(binding)
                {
                    val request = ImageRequest.Builder(binding.root.context)
                        .data(status.data.avatarUrl)
                        .target { drawable ->
                            imgAvatar.setImageDrawable(drawable)
                        }
                        .build()

                    imageLoader.enqueue(request)

                    binding.txtUsername.text = status.data.userName
                    binding.txtFullName.text = "${status.data.firstName} ${status.data.lastName}"
                    binding.root.setOnClickListener {
                        userClick(status.data)
                    }
                }
            }
            GetStatus.Sleep -> Unit
        }
    }
}