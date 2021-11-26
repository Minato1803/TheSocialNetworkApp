package com.datn.thesocialnetwork.feature.search.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.databinding.UserItemBinding

class UserViewHolder private constructor(
    private val binding: UserItemBinding
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup): UserViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = UserItemBinding.inflate(layoutInflater, parent, false)
            return UserViewHolder(
                binding
            )
        }
    }


    fun bind(
        user: UserModel,
        clickListener: ((UserModel) -> Unit)?,
        glide: RequestManager
    )
    {
        with(binding)
        {
            glide
                .load(user.avatarUrl)
                .fitCenter()
                .centerCrop()
                .into(imgAvatar)
            clickListener?.let { click ->
                root.setOnClickListener {
                    click(user)
                }
            }

            txtFullName.text = "${user.firstName} ${user.lastName}"
            txtUsername.text = user.userName
        }
    }
}