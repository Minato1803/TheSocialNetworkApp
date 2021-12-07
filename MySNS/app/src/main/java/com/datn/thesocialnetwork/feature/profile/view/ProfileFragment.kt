package com.datn.thesocialnetwork.feature.profile.view

import android.content.Intent
import android.graphics.ColorSpace
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.api.status.SearchFollowStatus
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.ModelMapping
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.core.util.SystemUtils.normalize
import com.datn.thesocialnetwork.core.util.ViewUtils.showSnackbarGravity
import com.datn.thesocialnetwork.core.util.ViewUtils.tryOpenUrl
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.repository.model.TagModel
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.post.comment.view.CommentFragment
import com.datn.thesocialnetwork.feature.post.detailpost.view.DetailPostFragment
import com.datn.thesocialnetwork.feature.post.editpost.view.EditPostFragment
import com.datn.thesocialnetwork.feature.post.editpost.viewmodel.EditPostViewModel
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.datn.thesocialnetwork.feature.profile.editprofile.view.EditProfileFragment
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
class ProfileFragment : AbstractDialog(R.layout.fragment_profile) {

    @Inject
    lateinit var mGoogleSignInClient: GoogleSignInClient

    @Inject
    lateinit var glide: RequestManager
    lateinit var mMainActivity: MainActivity

    private val editPostViewModel: EditPostViewModel by activityViewModels()
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        setHasOptionsMenu(true)
        Log.d("userDetail","${GlobalValue.USER_DETAIL.toString()}")
        viewModel.initWithLoggedUser()
        viewModel.loadDataInitUser(GlobalValue.USER_DETAIL!!)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("profile frag","created profile frag")
        setEvent()
        setInit()
        setObserveData()
        initRecyclers(true)
        profileBinding.rootSwipe.isRefreshing = false
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.miSignOut -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.str_sign_out))
                    .setMessage(resources.getString(R.string.log_out_confirmation))
                    .setNeutralButton(resources.getString(R.string.cancel)) { _, _ ->
                    }
                    .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                        val userDetail = ModelMapping.createUserDetail(GlobalValue.USER!!.userDetail, onlineStatus = System.currentTimeMillis())
                        viewModel.updateOnlineStatus(GlobalValue.USER!!.uidUser, userDetail)
                        SystemUtils.signOut(mGoogleSignInClient, requireContext())
                        sendToMainActivity()
                    }
                    .show()
                true
            }
            R.id.miEdit -> {
                navigateFragment(EditProfileFragment.newInstance(), "editProfileFragment")
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun profileClick(postOwner: String) {
        if (viewModel.isOwnAccountId(postOwner)) // user  clicked on own profile
        {
            profileBinding.userLayout.showSnackbarGravity(
                message = getString(R.string.you_are_currently_on_your_profile)
            )
        } else {
            val userFragment = UserFragment.newInstance(UserModel(uidUser = postOwner), true)
            navigateFragment(userFragment, "userFragment")
        }
    }

    override fun likeClick(postId: String, status: Boolean) {
        viewModel.setLikeStatus(postId, status)
    }

    override fun commentClick(postId: String) {
        //Todo: navigate to comment
        val commentFragment = CommentFragment.newInstance(postId)
        navigateFragment(commentFragment, "commentFragment")
    }

    override fun shareClick(postId: String) {
        //todo: share
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
        val detailPostFragment = DetailPostFragment.newInstance(postWithId.first)
        navigateFragment(detailPostFragment, "detailPostFragment")
    }

    override fun tagClick(tag: String) {
        //todo:  tag frag
        val tags = TagModel(tag,-1)
        val tagFragment = TagFragment.newInstance(tags)
        navigateFragment(tagFragment, "tagFragment")
    }

    override fun linkClick(link: String) {
        requireContext().tryOpenUrl(link) {
            SystemUtils.showMessage(requireContext(), getString(R.string.could_not_open_browser))
        }
    }

    override fun mentionClick(mention: String) {
        if (viewModel.isOwnAccountUsername(mention)) // user clicked on own profile
        {
            profileBinding.userLayout.showSnackbarGravity(
                message = getString(R.string.you_are_currently_on_your_profile)
            )
        } else {
            // check if mention is not the same as current user
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

    override fun menuReportClick(postId: String) {
        //TODO("Not yet implemented")
    }

    override fun menuEditClick(post: PostWithId) {
        //todo: editpost
        Log.d("editPost", "${post.toString()}")
        editPostViewModel.postWithId.postValue(post)
        val editPostFragment = EditPostFragment.newInstance()
        navigateFragment(editPostFragment, "editPostFragment")
    }

    private fun setObserveData() {
        viewModel.updateFollowList()
        /**
         * Collect user data
         */
        lifecycleScope.launchWhenStarted {
            viewModel.selectedUser.collectLatest {
                if (it != null) {
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.userFollowersFlow.collectLatest { status ->

                when (status) {
                    SearchFollowStatus.Loading, SearchFollowStatus.Sleep -> {
                    }
                    is SearchFollowStatus.Success -> {
                        profileBinding.txtCounterFollowers.text = status.result.size.toString()
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
                    }
                    is SearchFollowStatus.Success -> {
                        Log.d("Following", status.result.toString())
                        profileBinding.txtCounterFollowing.text = status.result.size.toString()
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
                    profileBinding.txtCounterPosts.text = it.data.size.toString()
                }
            }
        }
        /**
         * Collect selected category
         */
        lifecycleScope.launchWhenStarted {
            viewModel.category.collectLatest { selected ->
                profileBinding.tabsPostType.getTabAt(
                    categories.filterValues {
                        it == selected
                    }.keys.elementAt(0)
                )?.select()

            }
        }

    }

    private fun setInit() {

        //init data
        initDataUser()
        // setting main view
        mMainActivity.bd.toolbar.title = "thông tin cá nhân"
        mMainActivity.bd.appBarLayout.isVisible = true
        mMainActivity.bd.bottomAppBar.isVisible = true
        mMainActivity.bd.fabAdd.isVisible = true
        actionBarDrawerToggle = ActionBarDrawerToggle(
            requireActivity(),
            mMainActivity.bd.drawerLayout,
            mMainActivity.bd.toolbar,
            R.string.open, R.string.close)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        actionBarDrawerToggle.syncState()
    }

    private fun initDataUser() {
        profileBinding.tvFullName.text = GlobalValue.USER!!.userDetail.userName
        if (GlobalValue.USER!!.userDetail.description.trim().isNotEmpty())
            profileBinding.tvDesc.text = GlobalValue.USER!!.userDetail.description
        glide
            .load(GlobalValue.USER!!.userDetail.avatarUrl)
            .fitCenter()
            .centerCrop()
            .into(profileBinding.imgAvatar)
    }

    private fun setEvent() {
        profileBinding.linLayFollowers.setOnClickListener { openFollowers() }
        profileBinding.linLayFollowing.setOnClickListener { openFollowing() }
        profileBinding.rootSwipe.setOnRefreshListener {
            setInit()
            viewModel.loadDataInitUser(GlobalValue.USER_DETAIL!!)
            setObserveData()
            initRecyclers(true)
            profileBinding.rootSwipe.isRefreshing = false
        }
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

    fun navigateFragment(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(id, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    private fun sendToMainActivity() {
        startActivity(Intent(context, MainActivity::class.java))
    }

    private val categories = hashMapOf(
        0 to ProfileViewModel.DisplayPostCategory.UPLOADED,
        1 to ProfileViewModel.DisplayPostCategory.MENTIONS,
        2 to ProfileViewModel.DisplayPostCategory.LIKED
    )



//    override fun onResume() {
//        super.onResume()
//        Log.d("TAG", "reload profile")
//        setInit()
//        viewModel.loadDataInitUser(GlobalValue.USER_DETAIL!!)
//        setObserveData()
//        initRecyclers(true)
//    }
}