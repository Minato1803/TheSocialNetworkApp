package com.datn.thesocialnetwork.feature.search.view

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.SearchFollowStatus
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.core.util.exhaustive
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.databinding.FragmentEditProfileBinding
import com.datn.thesocialnetwork.databinding.FragmentUserBinding
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.profile.view.AbstractDialog
import com.datn.thesocialnetwork.feature.profile.viewmodel.ProfileViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class UserFragment : AbstractDialog(R.layout.fragment_user) {
    companion object {
        private const val USER_DATA = "USER_DATA"
        fun newInstance(
            userDetail: UserDetail
        ): UserFragment {
            val userFragment = UserFragment()
            val arg = Bundle()
            arg.putSerializable(USER_DATA, userDetail)
            userFragment.arguments = arg
            return userFragment
        }
    }

    @Inject
    lateinit var mGoogleSignInClient: GoogleSignInClient
    @Inject
    lateinit var glide: RequestManager
    private var _bd: FragmentUserBinding? = null
    lateinit var binding: FragmentUserBinding
    lateinit var mMainActivity: MainActivity
    private val mProfileViewModel : ProfileViewModel by viewModels()
    private lateinit var userDetail : UserDetail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _bd = FragmentUserBinding.bind(view)
        binding = _bd!!
        setEvent()
        setInit()
        setObserveData()
    }

    private fun setObserveData() {
        mProfileViewModel.updateFollowList()
        lifecycleScope.launchWhenStarted {
            mProfileViewModel.isSelectedUserFollowedByLoggedUser.collectLatest { isFollowedStatus ->
                when (isFollowedStatus)
                {
                    ProfileViewModel.IsUserFollowed.UNKNOWN ->
                    {
                        /**
                         * Displayed text: [R.string.follow]
                         */
                        binding.btnFollow.text = getString(R.string.follow)
                        binding.btnFollow.isEnabled = false
                    }
                    ProfileViewModel.IsUserFollowed.YES ->
                    {
                        binding.btnFollow.text = getString(R.string.unfollow)
                        binding.btnFollow.isEnabled = true
                    }
                    ProfileViewModel.IsUserFollowed.NO ->
                    {
                        binding.btnFollow.text = getString(R.string.follow)
                        binding.btnFollow.isEnabled = true
                    }
                }.exhaustive
            }
        }

        /**
         * change btn state
         */
        lifecycleScope.launchWhenStarted {
            mProfileViewModel.canDoFollowUnfollowOperation.collectLatest { canBeClicked ->
                binding!!.btnFollow.isEnabled = canBeClicked
            }
        }

        lifecycleScope.launchWhenStarted {
            mProfileViewModel.userFollowersFlow.collectLatest { status ->

                when (status)
                {
                    SearchFollowStatus.Loading, SearchFollowStatus.Sleep ->
                    {
                        //loading
                    }
                    is SearchFollowStatus.Success ->
                    {
                        binding.txtCounterFollowers.text = status.result.size.toString()
                    }
                }
            }
        }

        /**
         * Collect selected user following users
         */
        lifecycleScope.launchWhenStarted {
            mProfileViewModel.userFollowingFlow.collectLatest { status ->
                when (status)
                {
                    SearchFollowStatus.Loading, SearchFollowStatus.Sleep ->
                    {
                        //loading
                    }
                    is SearchFollowStatus.Success ->
                    {
                        binding.txtCounterFollowing.text = status.result.size.toString()
                    }
                }
            }
        }

        //Todo: not found user
    }

    private fun setInit() {
        extractData()
        mMainActivity.bd.toolbar.title = userDetail.userName
        mMainActivity.bd.toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_24)
        mMainActivity.bd.bottomAppBar.visibility = View.GONE
        mMainActivity.bd.fabAdd.visibility = View.GONE
        initDataUser()
    }


    private fun initDataUser() {
        mProfileViewModel.initUser(userDetail)
        binding.tvFullName.text = userDetail.userName
        if(userDetail.description.trim().isNotEmpty())
            binding.tvDesc.text = userDetail.description
        glide
            .load(userDetail.avatarUrl)
            .fitCenter()
            .centerCrop()
            .into(binding.imgAvatar)
    }

    private fun extractData() {
        userDetail = (arguments?.getSerializable(USER_DATA)) as UserDetail
    }

    private fun setEvent() {
        mMainActivity.bd.toolbar.setNavigationOnClickListener {
            mMainActivity.onBackPressed()
        }
        binding.btnFollow.setOnClickListener {
            mProfileViewModel.followUnfollow()
            setObserveData()
        }
        binding.btnMessage.setOnClickListener {
            SystemUtils.showMessage(requireContext(),"TODO:message")
        }
        //ToDo: open dialog list
        binding.linLayFollowers.setOnClickListener { openFollowers() }
        binding.linLayFollowing.setOnClickListener { openFollowing() }
    }

    private fun openFollowing() {
        openDialogWithListOfUsers(
            statusFlow = mProfileViewModel.getFollowing(),
            title = R.string.following,
            emptyText = R.string.user_have_no_followers,
            errorText = R.string.something_went_wrong
        )
    }

    private fun openFollowers() {
        openDialogWithListOfUsers(
            statusFlow = mProfileViewModel.getFollowers(),
            title = R.string.followers,
            emptyText = R.string.user_have_no_following,
            errorText = R.string.something_went_wrong
        )
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_profile, menu)
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         return super.onOptionsItemSelected(item)
    }
}