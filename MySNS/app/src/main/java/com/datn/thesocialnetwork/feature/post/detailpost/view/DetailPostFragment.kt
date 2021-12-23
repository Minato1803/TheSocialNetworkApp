package com.datn.thesocialnetwork.feature.post.detailpost.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.SystemUtils.formatWithSpaces
import com.datn.thesocialnetwork.core.util.TimeUtils.getDateTimeFormatFromMillis
import com.datn.thesocialnetwork.core.util.ViewUtils.setActionBarTitle
import com.datn.thesocialnetwork.core.util.ViewUtils.showSnackbarGravity
import com.datn.thesocialnetwork.core.util.ViewUtils.viewBinding
import com.datn.thesocialnetwork.data.repository.model.PostsImage
import com.datn.thesocialnetwork.data.repository.model.PostsModel
import com.datn.thesocialnetwork.data.repository.model.TagModel
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.databinding.FragmentDetailPostBinding
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.post.comment.view.CommentFragment
import com.datn.thesocialnetwork.feature.post.detailpost.viewmodel.DetailPostViewModel
import com.datn.thesocialnetwork.feature.post.editpost.view.EditPostFragment
import com.datn.thesocialnetwork.feature.post.editpost.viewmodel.EditPostViewModel
import com.datn.thesocialnetwork.feature.post.view.AbstractFragmentPost
import com.datn.thesocialnetwork.feature.post.viewholder.DetailPostPhotosApdapter
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.datn.thesocialnetwork.feature.profile.view.ProfileFragment
import com.datn.thesocialnetwork.feature.profile.view.UserFragment
import com.datn.thesocialnetwork.feature.search.view.TagFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class DetailPostFragment : AbstractFragmentPost(R.layout.fragment_detail_post) {

    companion object {
        private const val POST_ID = "POST_ID"

        fun newInstance(
            postId: String,
        ): DetailPostFragment {
            val detailPostFragment = DetailPostFragment()
            val arg = Bundle()
            arg.putString(POST_ID, postId)
            detailPostFragment.arguments = arg
            return detailPostFragment
        }
    }

    @Inject
    lateinit var imageLoader: ImageLoader

    override val viewModel: DetailPostViewModel by activityViewModels()
    override val binding by viewBinding(FragmentDetailPostBinding::bind)
    private val editPostViewModel: EditPostViewModel by activityViewModels()
    lateinit var mMainActivity: MainActivity

    private var postModel: PostsModel? = null
    private var postId: String = ""
    private var isPostLiked = false
    private var isPostMarked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        extractData()
        setObserveData()
        setInit()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.ac_edit -> {

                (viewModel.post.value as? GetStatus.Success<PostWithId>)?.let { status ->
                    if(GlobalValue.USER!!.uidUser == status.data.second.ownerId) {
                        editPostViewModel.postWithId.postValue(status.data)
                        val editPostFragment = EditPostFragment()
                        navigateFragment(editPostFragment, "editPostFragment")
                    } else {
                       binding.root.showSnackbarGravity(
                           message = getString(R.string.not_owner_post),
                           length = Snackbar.LENGTH_SHORT,
                           buttonText = getString(R.string.ok)
                        )
                    }

                }
                true
            }
            R.id.ac_report -> {
                //Todo: show dialog report
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setInit() {
        setActionBarTitle("Chi tiết bài viết")
        mMainActivity.bd.bottomAppBar.visibility = View.GONE
        mMainActivity.bd.fabAdd.visibility = View.GONE
        mMainActivity.bd.toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_24)
    }

    private fun extractData() {
        postId = arguments?.getString(POST_ID).toString()
        viewModel.initPost(postId)
    }

    private fun setEvent(post: PostWithId) {
        with(binding)
        {
            btnCmt.setOnClickListener {
                commentClick(post.first)
            }

            txtCmt.setOnClickListener {
                commentClick(post.first)
            }

            btnLike.setOnClickListener {
                viewModel.setLikeStatus(post.first, !isPostLiked)
            }

            txtOwner.setOnClickListener {
                profileClick(post.second.ownerId)
            }

            imgAvatar.setOnClickListener {
                profileClick(post.second.ownerId)
            }

            btnShow.setOnClickListener {
                viewModel.changeCollapse()
            }

            txtDesc.setOnHashtagClickListener { _, text -> tagClick(text.toString()) }
            txtDesc.setOnHyperlinkClickListener { _, text -> linkClick(text.toString()) }
            txtDesc.setOnMentionClickListener { _, text -> mentionClick(text.toString()) }

            btnMark.setOnClickListener {
                viewModel.setMarkStatus(post.first, !isPostMarked)
            }

            linlayLikeCounter.setOnClickListener {
                likeCounterClick(post.first)
            }
            txtSeen.setOnClickListener {
                seenCounterClick(post.first)
            }
        }

        mMainActivity.bd.toolbar.setNavigationOnClickListener {
            mMainActivity.onBackPressed()
        }
    }

    private fun seenCounterClick(first: String) {
        openDialogWithListOfUsers(
            statusFlow = viewModel.getUsersThatSeenPost(postId),
            title = R.string.users_that_seen_post,
            emptyText = R.string.empty_users_Seen_post,
            errorText = R.string.something_went_wrong_loading_users_that_seen_post
        )
    }

    private fun setObserveData() {

        lifecycleScope.launchWhenStarted {
            viewModel.likeStatus.collectLatest {
                when (it) {
                    GetStatus.Sleep -> Unit
                    is GetStatus.Failed -> {
                        Log.d("failed", "loading like")
                    }
                    GetStatus.Loading -> {
                        binding.txtLikeCounter.text = getString(R.string.str_loading_dot)
                    }
                    is GetStatus.Success -> {
                        isPostLiked = it.data.isPostLikeByLoggedUser
                        binding.txtLikeCounter.text = it.data.likeCounter.formatWithSpaces()
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.markStatus.collectLatest {
                when (it) {
                    GetStatus.Sleep -> Unit
                    is GetStatus.Failed -> {
                        Log.d("failed", "loading mark")
                    }
                    GetStatus.Loading -> {
                    }
                    is GetStatus.Success -> {
                        isPostMarked = it.data.isPostMarkByLoggedUser
                        Log.d("Mark", "mark it")
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.userStatus.collectLatest {
                when (it) {
                    GetStatus.Sleep -> Unit
                    is GetStatus.Failed -> {
                        Log.d("failed", "loading user data")
                    }
                    GetStatus.Loading -> {
                        mMainActivity.bd.toolbar.title = ""
                    }
                    is GetStatus.Success -> {
                        mMainActivity.bd.toolbar.title = it.data.userName
                        intInfoUser(it.data)
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.commentStatus.collectLatest {
                when (it) {
                    GetStatus.Sleep -> Unit
                    is GetStatus.Failed -> {

                    }
                    GetStatus.Loading -> {
                        binding.txtCmt.text = getString(
                            R.string.comments,
                            getString(R.string.str_loading_dot)
                        )
                    }
                    is GetStatus.Success -> {
                        binding.txtCmt.text = binding.root.context.getString(
                            R.string.comments,
                            it.data.formatWithSpaces()
                        )
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.isInfoShown.collectLatest {
                setInfoViewsVisibility(it)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.post.collectLatest {
                when (it) {
                    GetStatus.Sleep -> Unit
                    GetStatus.Loading -> Unit
                    is GetStatus.Success -> {
                        setupView(it.data)
                    }
                    is GetStatus.Failed -> {
                        Log.d("failed", "loading post ")
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.seenStatus.collectLatest {
                when (it) {
                    GetStatus.Sleep -> Unit
                    is GetStatus.Failed -> {
                        Log.d("failed", "loading seen")
                    }
                    GetStatus.Loading -> {
                        binding.txtSeen.text = getString(R.string.str_loading_dot)
                    }
                    is GetStatus.Success -> {
                        binding.txtLikeCounter.text = it.data.seenCounter.formatWithSpaces()
                    }
                }
            }
        }
    }

    private fun intInfoUser(data: UserModel) {
        with(binding) {
            txtOwner.text = data.userName
            val request = ImageRequest.Builder(binding.root.context)
                .data(data.avatarUrl)
                .target { drawable ->
                    imgAvatar.setImageDrawable(drawable)
                }
                .build()

            imageLoader.enqueue(request)
        }
    }

    override fun profileClick(postOwner: String) {
        if (viewModel.isOwnAccountId(postOwner)) // user clicked on own profile
        {
            val profileFragment = ProfileFragment()
            navigateFragment(profileFragment, "profileFragment")
        } else {
            val userFragment =
                UserFragment.newInstance(UserModel(uidUser = postOwner), isLoadFromDb = true)
            navigateFragment(userFragment, "userFragment")
        }
    }

    override fun commentClick(postId: String) {
        //Todo: Comment fragment
        val commentFragment = CommentFragment.newInstance(postId)
        navigateFragment(commentFragment, "commentFragment")
    }

    override fun imageClick(postWithId: PostWithId) {
        // nothing click
    }

    override fun tagClick(tag: String) {
        val tags = TagModel(tag, -1)
        val tagFragment = TagFragment.newInstance(tags)
        navigateFragment(tagFragment, "tagFragment")
    }

    override fun mentionClick(mention: String) {
        if (viewModel.isOwnAccountUsername(mention)) // user clicked on own profile
        {
            val profileFragment = ProfileFragment()
            navigateFragment(profileFragment, "profileFragment")
        } else {
            val userFragment =
                UserFragment.newInstance(UserModel(userName = mention), isLoadFromDb = true)
            navigateFragment(userFragment, "userFragment")
        }
    }

    override fun menuEditClick(post: PostWithId) {
        //todo: edit post fragment
        editPostViewModel.postWithId.postValue(post)
        val editPostFragment = EditPostFragment.newInstance()
        navigateFragment(editPostFragment, "editPostFragment")
    }

    private fun setInfoViewsVisibility(isVisible: Boolean) {
        with(binding)
        {
            infoPost.isVisible = isVisible
            imgLikeCount.isVisible = isVisible
            txtLikeCounter.isVisible = isVisible
            txtCmt.isVisible = isVisible

            /**
             * btn show
             */
            ContextCompat.getDrawable(
                requireContext(),
                if (isVisible) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up_24
            )?.let {
                (binding.btnShow as MaterialButton).icon = it
            }

            val bc = if (isVisible) ContextCompat.getColor(
                requireContext(),
                R.color.bar_background
            )
            else
                Color.TRANSPARENT

            binding.bottomBar.setBackgroundColor(bc)
        }
    }

    private fun setupView(post: PostWithId) {
        setEvent(post)
        loadImage(post.third)
        binding.txtDesc.text = post.second.content
        binding.txtTime.text = post.second.createdTime.getDateTimeFormatFromMillis()
        if(post.second.ownerId == GlobalValue.USER!!.uidUser) {
            binding.txtSeen.visibility = View.VISIBLE
        } else {
            binding.txtSeen.visibility = View.GONE
        }
    }

    private fun loadImage(listImage: List<Pair<String, PostsImage>>?) {
        binding.itemFeedPhotos.adapter = DetailPostPhotosApdapter(listImage)
        binding.itemFeedPhotoIndicator.setViewPager2(binding.itemFeedPhotos)
        if (listImage?.size == 1) {
            binding.itemFeedPhotoIndicator.visibility = View.GONE
        } else {
            binding.itemFeedPhotoIndicator.visibility = View.VISIBLE
        }
    }

    private fun navigateFragment(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(id, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        extractData()
        setInit()
        setObserveData()
    }
}