package com.datn.thesocialnetwork.feature.post.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.listener.PostClickListener
import com.datn.thesocialnetwork.core.util.SystemUtils.formatWithSpaces
import com.datn.thesocialnetwork.core.util.TimeUtils.getDateTimeFormat
import com.datn.thesocialnetwork.core.util.TimeUtils.showTimeDetail
import com.datn.thesocialnetwork.data.repository.FirebaseRepository
import com.datn.thesocialnetwork.data.repository.model.PostsImage
import com.datn.thesocialnetwork.data.repository.model.PostsModel
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.data.repository.model.post.status.LikeStatus
import com.datn.thesocialnetwork.databinding.PostItemBinding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

typealias PostWithId = Triple<String, PostsModel,List<Pair<String,PostsImage>>?>

class PostViewHolder private constructor(
    private val binding: PostItemBinding,
    private val cancelListeners: (Int, Int, Int) -> Unit
) : RecyclerView.ViewHolder(binding.root)
{
    companion object
    {

        fun create(parent: ViewGroup, cancelListeners: (Int, Int, Int) -> Unit): PostViewHolder
        {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = PostItemBinding.inflate(layoutInflater, parent, false)

            return PostViewHolder(
                binding,
                cancelListeners
            ).apply {
                baseDescLengthLines = binding.root.context.resources.getInteger(R.integer.max_lines_post_desc)

                binding.txtDesc.setOnClickListener {
                    isCollapsed = !isCollapsed
                }

                binding.butMore.setOnClickListener {
                    showPopupMenu(it)
                }
            }
        }
    }

    private fun setOnClickListeners(
        post: PostWithId,
        postClickListener: PostClickListener
    )
    {
        with(binding)
        {
            btnLike.setOnClickListener {
                postClickListener.likeClick(post.first, !isPostLiked)
            }

            btnShare.setOnClickListener {
                postClickListener.shareClick(post.first)
            }

            btnComment.setOnClickListener {
                postClickListener.commentClick(post.first)
            }

            txtComments.setOnClickListener {
                postClickListener.commentClick(post.first)
            }

            linLayLikeCounter.setOnClickListener {
                postClickListener.likeCounterClick(post.first)
            }

            imgAvatar.setOnClickListener {
                postClickListener.profileClick(post.second.ownerId)
            }

            txtOwner.setOnClickListener {
                postClickListener.profileClick(post.second.ownerId)
            }

            itemFeedPhotos.setOnClickListener {
                postClickListener.imageClick(post)
            }
            txtTime.setOnClickListener {
                postClickListener.imageClick(post)
            }

            txtDesc.setOnHashtagClickListener { _, text -> postClickListener.tagClick(text.toString()) }
            txtDesc.setOnHyperlinkClickListener { _, text -> postClickListener.linkClick(text.toString()) }
            txtDesc.setOnMentionClickListener { _, text -> postClickListener.mentionClick(text.toString()) }
        }
    }

    private lateinit var imageLoader: ImageLoader

    private var baseDescLengthLines = -1
    private var isCollapsed: Boolean = true
        set(value)
        {
            field = value
            binding.txtDesc.maxLines = if (value)
            {
                baseDescLengthLines
            }
            else
            {
                Int.MAX_VALUE
            }
        }

    private var isPostLiked = false
        set(value)
        {
            field = value

            if (value) // Post is liked
            {
                with(binding)
                {
                    (btnLike as MaterialButton).apply {
                        icon = ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_baseline_favorite_24
                        )
                        setIconTintResource(R.color.red_border_line)
                    }
                }
            }
            else // post is not liked
            {
                with(binding)
                {
                    (btnLike as MaterialButton).apply {
                        icon = ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_heart
                        )
                        setIconTintResource(R.color.colorFF954CFB)
                    }
                }
            }
        }


    private val scope = CoroutineScope(Dispatchers.Main)

    private var userJob: Job? = null
    private var userListenerId: Int = -1

    private var likeJob: Job? = null
    private var likeListenerId: Int = -1

    private var commentCounterJob: Job? = null
    private var commentCounterListenerId: Int = -1

    fun cancelJobs()
    {
        userJob?.cancel()
        likeJob?.cancel()

        cancelListeners(userListenerId, likeListenerId, commentCounterListenerId)
    }

    private lateinit var postClickListener: PostClickListener
    private lateinit var post: PostWithId

    private var loggedUserId: String? = null

    fun bind(
        post: PostWithId,
        glide: RequestManager,
        imageLoader: ImageLoader,
        postClickListener: PostClickListener,
        userFlow: (Int, String) -> Flow<GetStatus<UserModel>>,
        likeFlow: (Int, String) -> Flow<GetStatus<LikeStatus>>,
        commentCounterFlow: (Int, String) -> Flow<GetStatus<Long>>,
        loggedUserId: String?
    )
    {
        this.loggedUserId = loggedUserId
        this.postClickListener = postClickListener
        this.post = post
        isCollapsed = true

        this.imageLoader = imageLoader
        cancelJobs()

        userJob = scope.launch {
            userListenerId = FirebaseRepository.userListenerId
            userFlow(userListenerId, post.second.ownerId).collectLatest {
                setUserData(it)
            }
        }

        likeJob = scope.launch {
            likeListenerId = FirebaseRepository.likeListenerId
            likeFlow(likeListenerId, post.first).collectLatest {
                setLikeStatus(it)
            }
        }

        commentCounterJob = scope.launch {
            commentCounterListenerId = FirebaseRepository.commentCounterListenerId
            commentCounterFlow(commentCounterListenerId, post.first).collectLatest {
                setCommentStatus(it)
            }
        }

        with(binding)
        {
            //load list image
            /** bind photo to view pager*/
            itemFeedPhotos.adapter = DetailPostPhotosApdapter(post.third)
            itemFeedPhotoIndicator.setViewPager2(itemFeedPhotos)
            if (post.third?.size  == 1) {
                itemFeedPhotoIndicator.visibility = View.GONE
            } else {
                itemFeedPhotoIndicator.visibility = View.VISIBLE
            }
                txtDesc.text = post.second.content
            val timeDetail = showTimeDetail(post.second.updatedTime)
            txtTime.text = timeDetail
        }

        setOnClickListeners(post, postClickListener)
    }

    private fun showPopupMenu(view: View)
    {
        val popupMenu = PopupMenu(view.context, view)

        popupMenu.inflate(R.menu.menu_post_dropdown_collapse)

        // desc can be collapsed
        if (binding.txtDesc.lineCount > baseDescLengthLines)
        {
            popupMenu.menu.findItem(R.id.mi_collapse).title = binding.root.context.getString(
                if (isCollapsed) R.string.show_description
                else R.string.collapse_description
            )
        }
        else // desc cannot be collapsed (too short)
        {
            popupMenu.menu.findItem(R.id.mi_collapse).isVisible = false
        }

        popupMenu.menu.findItem(R.id.mi_edit).isVisible = post.second.ownerId == loggedUserId

        popupMenu.setOnMenuItemClickListener { menuItem ->

            return@setOnMenuItemClickListener when (menuItem.itemId)
            {
                R.id.mi_report ->
                {
                    postClickListener.menuReportClick(post.first)
                    true
                }
                R.id.mi_collapse ->
                {
                    isCollapsed = !isCollapsed
                    true
                }
                R.id.mi_edit ->
                {
                    postClickListener.menuEditClick(post)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun setLikeStatus(status: GetStatus<LikeStatus>)
    {
        when (status)
        {
            GetStatus.Sleep -> Unit
            is GetStatus.Failed ->
            {

            }
            GetStatus.Loading ->
            {
                binding.txtLikesCounter.text = binding.root.context.getString(R.string.str_loading_dot)
            }
            is GetStatus.Success ->
            {
                isPostLiked = status.data.isPostLikeByLoggedUser
                binding.txtLikesCounter.text = status.data.likeCounter.toString()
            }
        }
    }


    private fun setCommentStatus(commentStatus: GetStatus<Long>)
    {
        when (commentStatus)
        {
            is GetStatus.Failed ->
            {

            }
            GetStatus.Loading ->
            {

            }
            is GetStatus.Success ->
            {
                binding.txtComments.text = binding.root.context.getString(
                    R.string.comments,
                    commentStatus.data.formatWithSpaces()
                )
            }
            GetStatus.Sleep -> Unit
        }
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
            }
            GetStatus.Loading ->
            {
                binding.txtOwner.text = binding.root.context.getString(R.string.str_loading_dot)
            }
            is GetStatus.Success ->
            {
                with(binding)
                {
                    txtOwner.text = status.data.userName

                    val request = ImageRequest.Builder(root.context)
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