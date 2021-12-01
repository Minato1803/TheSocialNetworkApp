package com.datn.thesocialnetwork.feature.post.detailpost.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.SystemUtils.formatWithSpaces
import com.datn.thesocialnetwork.core.util.ViewUtils.viewBinding
import com.datn.thesocialnetwork.data.repository.model.PostsModel
import com.datn.thesocialnetwork.databinding.FragmentDetailPostBinding
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.post.detailpost.viewmodel.DetailPostViewModel
import com.datn.thesocialnetwork.feature.post.view.AbstractFragmentPost
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.post_item.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import android.graphics.Color
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.datn.thesocialnetwork.data.repository.model.PostsImage
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.feature.post.editpost.view.EditPostFragment
import com.datn.thesocialnetwork.feature.post.editpost.viewmodel.EditPostViewModel
import com.datn.thesocialnetwork.feature.post.viewholder.DetailPostPhotosApdapter
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.datn.thesocialnetwork.feature.profile.view.ProfileFragment
import com.datn.thesocialnetwork.feature.profile.view.UserFragment

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

    override val viewModel: DetailPostViewModel by viewModels()
    override val binding by viewBinding(FragmentDetailPostBinding::bind)
    private val editPostViewModel: EditPostViewModel by viewModels()
    lateinit var mMainActivity: MainActivity

    private var postModel: PostsModel? = null
    private var postId: String = ""
    private var isPostLiked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEvent()
        setInit()
        setObserveData()
    }

    private fun setInit() {
        extractData()
        mMainActivity.bd.bottomAppBar.visibility = View.GONE
        mMainActivity.bd.fabAdd.visibility = View.GONE
    }

    private fun extractData() {
        postId = arguments?.getString(POST_ID).toString()
        viewModel.initPost(postId)
    }

    private fun setEvent() {
        TODO("Not yet implemented")
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
            viewModel.userStatus.collectLatest {
                when (it) {
                    GetStatus.Sleep -> Unit
                    is GetStatus.Failed -> {
                        Log.d("failed", "loading user data")
                    }
                    GetStatus.Loading -> {
                        binding.txtUsername.text = getString(R.string.str_loading_dot)
                    }
                    is GetStatus.Success -> {
                        binding.txtUsername.text = it.data.userName
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
    }

    override fun imageClick(postWithId: PostWithId) {
        // nothing click
    }

    override fun tagClick(tag: String) {
        //todo: Tag fragment
    }

    override fun mentionClick(mention: String) {
        if (viewModel.isOwnAccountUsername(mention)) // user clicked on own profile
        {
            val profileFragment = ProfileFragment()
            navigateFragment(profileFragment, "profileFragment")
        }
        else
        {
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
            txtDesc.isVisible = isVisible
            imgLikedCounter.isVisible = isVisible
            txtLikesCounter.isVisible = isVisible
            txtComments.isVisible = isVisible

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

            binding.topBar.setBackgroundColor(bc)
            binding.bottomBar.setBackgroundColor(bc)
        }
    }

    private fun setupView(post: PostWithId) {
        setClickListeners(post)
        loadImage(post.third)
        binding.txtDesc.text = post.second.content
    }

    private fun setClickListeners(post: PostWithId) {
        with(binding)
        {
            btnComment.setOnClickListener {
                commentClick(post.first)
            }

            txtComments.setOnClickListener {
                commentClick(post.first)
            }

            btnBack.setOnClickListener {
                mMainActivity.onBackPressed()
            }

            btnLike.setOnClickListener {
                viewModel.setLikeStatus(post.first, !isPostLiked)
            }

            txtUsername.setOnClickListener {
                profileClick(post.second.ownerId)
            }

            btnShow.setOnClickListener {
                viewModel.changeCollapse()
            }

            txtDesc.setOnHashtagClickListener { _, text -> tagClick(text.toString()) }
            txtDesc.setOnHyperlinkClickListener { _, text -> linkClick(text.toString()) }
            txtDesc.setOnMentionClickListener { _, text -> mentionClick(text.toString()) }

            btnOptions.setOnClickListener { view ->
                (viewModel.post.value as? GetStatus.Success<PostWithId>)?.let { status ->
                    showPopupMenu(view, status.data.second)
                }
            }

            btnShare.setOnClickListener {
                shareClick(post.first)
            }

            linlayLikeCounter.setOnClickListener {
                likeCounterClick(post.first)
            }
        }
    }

    private fun showPopupMenu(view: View, post: PostsModel) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.menu_post_dropdown_collapse)

        popupMenu.menu.findItem(R.id.mi_collapse).isVisible = false
        popupMenu.menu.findItem(R.id.mi_edit).isVisible = post.ownerId == viewModel.requireUser.uid

        popupMenu.setOnMenuItemClickListener { menuItem ->

            return@setOnMenuItemClickListener when (menuItem.itemId) {
                R.id.mi_report -> {
                    //Todo: show dialog report
                    true
                }
                R.id.mi_edit -> {
                    //Todo: navigate edit post
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
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

}