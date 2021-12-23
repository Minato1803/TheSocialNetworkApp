package com.datn.thesocialnetwork.feature.profile.view

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.api.status.SearchFollowStatus
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.SystemUtils.normalize
import com.datn.thesocialnetwork.core.util.SystemUtils.showMessage
import com.datn.thesocialnetwork.core.util.ViewUtils.showSnackbarGravity
import com.datn.thesocialnetwork.core.util.ViewUtils.tryOpenUrl
import com.datn.thesocialnetwork.data.repository.model.TagModel
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.databinding.FragmentChatBinding
import com.datn.thesocialnetwork.feature.chat.view.MessageFragment
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.post.detailpost.view.DetailPostFragment
import com.datn.thesocialnetwork.feature.post.comment.view.CommentFragment
import com.datn.thesocialnetwork.feature.post.editpost.view.EditPostFragment
import com.datn.thesocialnetwork.feature.post.editpost.viewmodel.EditPostViewModel
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.datn.thesocialnetwork.feature.profile.viewmodel.ProfileViewModel
import com.datn.thesocialnetwork.feature.search.view.TagFragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class UserFragment : AbstractDialog(R.layout.fragment_user) {
    companion object {
        private const val USER_DATA = "USER_DATA"
        private const val IS_LOAD_FROM_DB = "IS_LOAD_FROM_DB"
        fun newInstance(
            userModel: UserModel,
            isLoadFromDb: Boolean,
        ): UserFragment {
            val userFragment = UserFragment()
            val arg = Bundle()
            arg.putParcelable(USER_DATA, userModel)
            arg.putBoolean(IS_LOAD_FROM_DB, isLoadFromDb)
            userFragment.arguments = arg
            return userFragment
        }
    }

    @Inject
    lateinit var mGoogleSignInClient: GoogleSignInClient

    @Inject
    lateinit var glide: RequestManager
    lateinit var mMainActivity: MainActivity

    //    private val mProfileViewModel: ProfileViewModel by viewModels()
    private val editPostViewModel: EditPostViewModel by activityViewModels()
    private var userModel: UserModel? = null
    private var isLoadFromDb: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        extractData()
        setInit()
        setEvent()
        setObserveData()
    }

    private fun setObserveData() {
        viewModel.updateFollowList()
        lifecycleScope.launchWhenStarted {
            viewModel.isSelectedUserFollowedByLoggedUser.collectLatest { isFollowedStatus ->
                when (isFollowedStatus) {
                    ProfileViewModel.IsUserFollowed.UNKNOWN -> {
                        userBinding.btnFollow.text = getString(R.string.follow)
                        userBinding.btnFollow.isEnabled = false
                    }
                    ProfileViewModel.IsUserFollowed.YES -> {
                        userBinding.btnFollow.text = getString(R.string.unfollow)
                        userBinding.btnFollow.isEnabled = true
                    }
                    ProfileViewModel.IsUserFollowed.NO -> {
                        userBinding.btnFollow.text = getString(R.string.follow)
                        userBinding.btnFollow.isEnabled = true
                    }
                }
            }
        }

        /**
         * change btn state
         */
        lifecycleScope.launchWhenStarted {
            viewModel.canDoFollowUnfollowOperation.collectLatest { canBeClicked ->
                userBinding.btnFollow.isEnabled = canBeClicked
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.userFollowersFlow.collectLatest { status ->

                when (status) {
                    SearchFollowStatus.Loading, SearchFollowStatus.Sleep -> {
                        //loading
                    }
                    is SearchFollowStatus.Success -> {
                        userBinding.txtCounterFollowers.text = status.result.size.toString()
                    }
                }
            }
        }

        /**
         * Collect selected user following users
         */
        lifecycleScope.launchWhenStarted {
            viewModel.userFollowingFlow.collectLatest { status ->
                when (status) {
                    SearchFollowStatus.Loading, SearchFollowStatus.Sleep -> {
                        //loading
                    }
                    is SearchFollowStatus.Success -> {
                        userBinding.txtCounterFollowing.text = status.result.size.toString()
                    }
                }
            }
        }

        /**
         * Collect number of posts
         */
        lifecycleScope.launchWhenStarted {
            viewModel.uploadedPosts.collectLatest {
                if (it is GetStatus.Success) {
                    userBinding.txtCounterPosts.text = it.data.size.toString()
                }
            }
        }
        /**
         * Collect selected category
         */
        lifecycleScope.launchWhenStarted {
            viewModel.category.collectLatest { selected ->
                userBinding.tabsPostType.getTabAt(
                    categories.filterValues {
                        it == selected
                    }.keys.elementAt(0)
                )?.select()

            }
        }

        //Todo: not found user
    }

    private fun setInit() {
        lifecycleScope.launchWhenStarted {
            viewModel.selectedUser.collectLatest { userModel ->
                if (userModel != null) {
                    userBinding.tvFullName.text = userModel.userName
                    if (userModel.description.trim().isNotEmpty())
                        userBinding.tvDesc.text = userModel.description
                    glide
                        .load(userModel.avatarUrl)
                        .fitCenter()
                        .centerCrop()
                        .into(userBinding.imgAvatar)
                    mMainActivity.bd.toolbar.title = userModel.userName ?: ""
                }
            }
        }
        mMainActivity.bd.toolbar.navigationIcon =
            resources.getDrawable(R.drawable.ic_arrow_back_24)
        mMainActivity.bd.bottomAppBar.visibility = View.GONE
        mMainActivity.bd.fabAdd.visibility = View.GONE
        initRecyclers(false)
    }


    private fun initDataUser() {
        //todo: notfound
        Log.d("receiveUser", "${isLoadFromDb.toString()} ${userModel.toString()}")
        if (isLoadFromDb) {
            if (!userModel!!.userName.isNullOrEmpty())
                viewModel.initWithUsername(userModel!!.userName)
            else
                viewModel.initWithUserId(userModel!!.uidUser)
        } else {
            viewModel.initUser(userModel!!)
        }
        setObserveData()
    }

    private fun extractData() {
        userModel = arguments?.getParcelable(USER_DATA)
        isLoadFromDb = arguments?.getBoolean(IS_LOAD_FROM_DB) ?: false
        Log.d("receiveUser", "${userModel.toString()} ${isLoadFromDb.toString()}")
        viewModel.liveUserModel.postValue(userModel)
        initDataUser()
    }

    private fun setEvent() {
        mMainActivity.bd.toolbar.setNavigationOnClickListener {
            mMainActivity.onBackPressed()
        }
        userBinding.btnFollow.setOnClickListener {
            viewModel.followUnfollow()
            setObserveData()
        }
        userBinding.btnMessage.setOnClickListener {
            MessageFragment.newInstance(userModel!!).show(childFragmentManager, "messageFragment")
//            navigateFragment(messageFragment, "messageFragment")
        }
        //ToDo: open dialog list
        userBinding.linLayFollowers.setOnClickListener { openFollowers() }
        userBinding.linLayFollowing.setOnClickListener { openFollowing() }
    }

    private fun openFollowing() {
        openDialogWithListOfUsers(
            statusFlow = viewModel.getFollowing(),
            title = R.string.following,
            emptyText = R.string.user_have_no_following,
            errorText = R.string.something_went_wrong
        )
    }

    private fun openFollowers() {
        openDialogWithListOfUsers(
            statusFlow = viewModel.getFollowers(),
            title = R.string.followers,
            emptyText = R.string.user_have_no_followers,
            errorText = R.string.something_went_wrong
        )
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_profile, menu)
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun profileClick(postOwner: String) {
        if (viewModel.isOwnAccountId(postOwner)) // user clicked on own profile
        {
            val profileFragment = ProfileFragment()
            navigateFragment(profileFragment, "profileFragment")
        } else {
            //current user != user clicked
            if (viewModel.selectedUser.value?.uidUser != postOwner) {
                val userFragment =
                    UserFragment.newInstance(UserModel(uidUser = postOwner),
                        isLoadFromDb = true)
                navigateFragment(userFragment, "userFragment")
            } else {
                userBinding.userLayout.showSnackbarGravity(
                    message = getString(R.string.currently_on_this_profile)
                )
            }
        }
    }

    override fun likeClick(postId: String, status: Boolean) {
        viewModel.setLikeStatus(postId, status)
    }

    override fun commentClick(postId: String) {
        //todo: cmt fragment
        val commentFragment = CommentFragment.newInstance(postId)
        navigateFragment(commentFragment, "commentFragment")
    }

    override fun markClick(postId: String, status: Boolean) {
        viewModel.setMarkStatus(postId, status)
    }

    override fun likeCounterClick(postId: String) {
        openDialogWithListOfUsers(
            statusFlow = viewModel.getUsersThatLikePost(postId),
            title = R.string.users_that_like_post,
            emptyText = R.string.empty_users_liking_post,
            errorText = R.string.something_went_wrong_loading_users_that_liked_post
        )
    }

    override fun imageClick(postWithId: PostWithId) {
        if(postWithId.second.ownerId != GlobalValue.USER!!.uidUser) {
            viewModel.setSeenStatus(postWithId.first)
        }
        val detailPostFragment = DetailPostFragment.newInstance(postWithId.first)
        navigateFragment(detailPostFragment, "detailPostFragment")
    }

    override fun tagClick(tag: String) {
        //todo: tag fragment
        val tags = TagModel(tag, -1)
        val tagFragment = TagFragment.newInstance(tags)
        navigateFragment(tagFragment, "tagFragment")
    }

    override fun linkClick(link: String) {
        requireContext().tryOpenUrl(link) {
            showMessage(requireContext(), getString(R.string.could_not_open_browser))
        }
    }

    override fun mentionClick(mention: String) {
        if (viewModel.isOwnAccountUsername(mention)) // user clicked on own profile
        {
            val profileFragment = ProfileFragment()
            navigateFragment(profileFragment, "profileFragment")
        } else {
            if (viewModel.selectedUser.value?.userName!!.lowercase() != mention.normalize()) {
                val userFragment =
                    UserFragment.newInstance(UserModel(userName = mention), isLoadFromDb = true)
                navigateFragment(userFragment, "userFragment")
            } else {
                userBinding.userLayout.showSnackbarGravity(
                    message = getString(R.string.currently_on_this_profile)
                )
            }
        }
    }

    override fun deletePostClick(post: PostWithId) {
        //todo: report dialog
        if (post.second.ownerId == GlobalValue.USER!!.uidUser) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.str_delete_post))
                .setMessage(resources.getString(R.string.delete_confirmation))
                .setNeutralButton(resources.getString(R.string.cancel)) { _, _ ->
                }
                .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                    //delete post
                    viewModel.deletePost(post.first)
                    setObserveData()
                }
                .show()
        } else {
            userBinding.userLayout.showSnackbarGravity(
                message = getString(R.string.not_allow)
            )
        }
    }

    override fun menuEditClick(post: PostWithId) {
        Log.d("editPost", "${post.toString()}")
        if (post.second.ownerId == GlobalValue.USER!!.uidUser) {
            editPostViewModel.postWithId.postValue(post)
            val editPostFragment = EditPostFragment.newInstance()
            navigateFragment(editPostFragment, "editPostFragment")
        } else {
            userBinding.userLayout.showSnackbarGravity(
                message = getString(R.string.not_allow)
            )
        }
    }

    private fun navigateFragment(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(id, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    private val categories = hashMapOf(
        0 to ProfileViewModel.DisplayPostCategory.UPLOADED,
        1 to ProfileViewModel.DisplayPostCategory.MENTIONS,
        2 to ProfileViewModel.DisplayPostCategory.MARKED
    )

    override fun onResume() {
        super.onResume()
        extractData()
        setInit()
    }
}