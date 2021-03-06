package com.datn.thesocialnetwork.feature.home.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.activityViewModels
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.ViewUtils.setActionBarTitle
import com.datn.thesocialnetwork.core.util.ViewUtils.showSnackbarGravity
import com.datn.thesocialnetwork.core.util.ViewUtils.viewBinding
import com.datn.thesocialnetwork.data.repository.model.TagModel
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.data.repository.model.post.StateData
import com.datn.thesocialnetwork.databinding.FragmentHomeBinding
import com.datn.thesocialnetwork.feature.home.viewmodel.HomeViewModel
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.post.comment.view.CommentFragment
import com.datn.thesocialnetwork.feature.post.detailpost.view.DetailPostFragment
import com.datn.thesocialnetwork.feature.post.editpost.view.EditPostFragment
import com.datn.thesocialnetwork.feature.post.editpost.viewmodel.EditPostViewModel
import com.datn.thesocialnetwork.feature.post.view.AbstractFragmentRcv
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.datn.thesocialnetwork.feature.post.viewmodel.ViewModelStateRcv
import com.datn.thesocialnetwork.feature.profile.view.UserFragment
import com.datn.thesocialnetwork.feature.search.view.TagFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class HomeFragment() : AbstractFragmentRcv(
    R.layout.fragment_home,
    StateData(
        emptyStateIcon = R.drawable.ic_dynamic_feed_24,
        emptyStateText = R.string.nothing_to_show_home
    )
) {

    override val viewModel: HomeViewModel by activityViewModels()
    private val editPostViewModel: EditPostViewModel by activityViewModels()
    override val binding by viewBinding(FragmentHomeBinding::bind)
    lateinit var mMainActivity: MainActivity
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
    }

    override fun profileClick(postOwner: String) {
        if (viewModel.isOwnAccountId(postOwner)) // user  clicked on own profile
        {
            mMainActivity.clickNavigateSubScreen(
                mMainActivity.bd.bottomNavMain.menu.findItem(
                    R.id.navProfile
                )
            )
            mMainActivity.bd.bottomNavMain.selectedItemId = R.id.navProfile
        }
        else
        {
            val userFragment = UserFragment.newInstance(UserModel(uidUser = postOwner), true)
            navigateFragment(userFragment, "userFragment")
        }
    }

    override fun commentClick(postId: String) {
        //todo: comment fragment
        val commentFragment = CommentFragment.newInstance(postId)
        navigateFragment(commentFragment, "commentFragment")
    }

    override fun imageClick(postWithId: PostWithId) {
        //todo: detail post
        if(postWithId.second.ownerId != GlobalValue.USER!!.uidUser) {
            viewModel.setSeenStatus(postWithId.first)
        }
        val detailPostFragment = DetailPostFragment.newInstance(postWithId.first)
        navigateFragment(detailPostFragment,"detailPostFragment")
    }

    override fun tagClick(tag: String) {
        val tags = TagModel(tag,-1)
        val tagFragment = TagFragment.newInstance(tags)
        navigateFragment(tagFragment, "tagFragment")
    }

    override fun mentionClick(mention: String) {
        if (viewModel.isOwnAccountUsername(mention)) // user  clicked on own profile
        {
            mMainActivity.clickNavigateSubScreen(
                mMainActivity.bd.bottomNavMain.menu.findItem(
                    R.id.navProfile
                )
            )
            mMainActivity.bd.bottomNavMain.selectedItemId = R.id.navProfile
        }
        else
        {
            val userFragment = UserFragment.newInstance(UserModel(userName = mention), true)
            navigateFragment(userFragment, "userFragment")
        }
    }

    override fun menuEditClick(post: PostWithId) {
        if(post.second.ownerId == GlobalValue.USER!!.uidUser) {
            editPostViewModel.postWithId.postValue(post)
            val editPostFragment = EditPostFragment.newInstance()
            navigateFragment(editPostFragment, "editPostFragment")
        } else {
            binding.homeLayout.showSnackbarGravity(
                message = getString(R.string.not_allow)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Inflate the layout for this fragment
        super.onViewCreated(view, savedInstanceState)
        setInit()
        setEvent()
    }

    private fun setEvent() {
        binding.rootSwipe.setOnRefreshListener {
            viewModel.loadPosts()
            initView()
            binding.rootSwipe.isRefreshing = false
        }
    }

    private fun setInit() {
        setActionBarTitle("Trang ch???")
        mMainActivity.enableLayoutBehaviour()
        actionBarDrawerToggle = ActionBarDrawerToggle(
            requireActivity(),
            mMainActivity.bd.drawerLayout,
            mMainActivity.bd.toolbar,
            R.string.open, R.string.close)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        actionBarDrawerToggle.syncState()
    }

    override fun onResume() {
        super.onResume()
        Log.d("TAG", "reload home")
        viewModel.loadPosts()
        initView()
    }

    fun navigateFragment(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(id, fragment, "tag")
            .addToBackStack(null)
            .commit()
    }

}