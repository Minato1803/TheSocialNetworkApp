package com.datn.thesocialnetwork.feature.post.comment.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import com.datn.thesocialnetwork.core.util.FirebaseNode
import com.datn.thesocialnetwork.core.util.TimeUtils.showTimeDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.repository.model.post.status.CommentModel
import com.datn.thesocialnetwork.databinding.ItemCommentBinding
import com.google.firebase.database.*

typealias CommentId = Pair<String, CommentModel>

class CommentViewHolder private constructor(
    private val binding: ItemCommentBinding
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {
        fun create(parent: ViewGroup): CommentViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemCommentBinding.inflate(layoutInflater, parent, false)
            return CommentViewHolder(
                binding
            )
        }
    }

    fun bind(
        comment: CommentId,
        imageLoader: ImageLoader
    )
    {
        loadUserData(
            comment, imageLoader
        )
        with(binding)
        {
            txtBody.text = comment.second.content
            txtTime.text = showTimeDetail(comment.second.time)
        }
    }

    private var _userRef: DatabaseReference? = null
    private var _userListener: ValueEventListener? = null
    lateinit var mFirebaseDb: FirebaseDatabase

    private fun loadUserData(
        comment: CommentId,
        imageLoader: ImageLoader
    )
    {
        // remove old listener

        _userRef?.let { ref ->
            _userListener?.let { listener ->
                ref.removeEventListener(listener)
            }
        }
        mFirebaseDb = FirebaseDatabase.getInstance()
        _userRef = mFirebaseDb.getReference(FirebaseNode.user).child(comment.second.ownerId)

        _userListener = object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                snapshot.getValue(UserDetail::class.java)?.let { user ->
                    with(binding)
                    {
                        txtOwner.text = user.userName

                        val request = ImageRequest.Builder(root.context)
                            .data(user.avatarUrl)
                            .target { drawable ->
                                imgAvatar.setImageDrawable(drawable)
                            }
                            .build()
                        imageLoader.enqueue(request)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError)
            {
            }

        }
        _userRef!!.addListenerForSingleValueEvent(_userListener!!)
    }
}