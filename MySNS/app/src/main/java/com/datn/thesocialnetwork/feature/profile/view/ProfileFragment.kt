package com.datn.thesocialnetwork.feature.profile.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.api.status.SearchFollowStatus
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.ModelMapping
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.core.util.ViewUtils.showSnackbarGravity
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.databinding.FragmentProfileBinding
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.profile.editprofile.view.EditProfileFragment
import com.datn.thesocialnetwork.feature.profile.viewmodel.ProfileViewModel
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
//    private var binding: FragmentProfileBinding? = null
    lateinit var mMainActivity: MainActivity

    private val mProfileViewModel : ProfileViewModel by viewModels()
    lateinit var actionBarDrawerToggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
    }

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?,
//    ): View {
//        // Inflate the layout for this fragment
//        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
//
//        return binding!!.root
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setEvent()
        setInit()
        setObserveData()
        initRecyclers(true)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId)
        {
            R.id.miSignOut ->
            {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.str_sign_out))
                    .setMessage(resources.getString(R.string.log_out_confirmation))
                    .setNeutralButton(resources.getString(R.string.cancel)) { _, _ ->
                    }
                    .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                        SystemUtils.signOut(mGoogleSignInClient, requireContext())
                        sendToMainActivity()
                    }
                    .show()
                true
            }
            R.id.miEdit ->
            {
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
        }
        else
        {
            val userFragment = UserFragment.newInstance(UserModel(uidUser = postOwner), true)
            navigateFragment(userFragment, "userFragment")
        }
    }

    override fun likeClick(postId: String, status: Boolean) {
        //TODO("Not yet implemented")
    }

    override fun commentClick(postId: String) {
        //Todo: navigate to comment
    }

    override fun shareClick(postId: String) {
        //TODO("Not yet implemented")
    }

    override fun likeCounterClick(postId: String) {
        //TODO("Not yet implemented")
    }

    override fun tagClick(tag: String) {
        //TODO("Not yet implemented")
    }

    override fun linkClick(link: String) {
        //TODO("Not yet implemented")
    }

    override fun mentionClick(mention: String) {
        //TODO("Not yet implemented")
    }

    override fun menuReportClick(postId: String) {
        //TODO("Not yet implemented")
    }

    private fun setObserveData() {
        mProfileViewModel.updateFollowList()
        /**
         * Collect user data
         */
        lifecycleScope.launchWhenStarted {
            mProfileViewModel.selectedUser.collectLatest {
                if (it != null)
                {}
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
                        profileBinding.txtCounterFollowers.text = status.result.size.toString()
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
            mProfileViewModel.uploadedPosts.collectLatest {
                if (it is GetStatus.Success) {
                    profileBinding.txtCounterPosts.text = it.data.size.toString()
                }
            }
        }

        /**
         * Collect selected category
         */
        lifecycleScope.launchWhenStarted {
            mProfileViewModel.category.collectLatest { selected ->
                profileBinding.tabsPostType.getTabAt(
                    categories.filterValues {
                        it == selected
                    }.keys.elementAt(0)
                )?.select()

            }
        }
    }

    private fun setInit() {
        if (!mProfileViewModel.isInitialized.value)
        {
            mProfileViewModel.initWithLoggedUser()
        }
        else
        {
            mProfileViewModel.refreshUser()
        }
        mProfileViewModel.initUser(ModelMapping.mapToUserModel(GlobalValue.USER!!))
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
        //init data
        initDataUser()
    }

    private fun initDataUser() {
        profileBinding.tvFullName.text = GlobalValue.USER!!.userDetail.userName
        if(GlobalValue.USER!!.userDetail.description.trim().isNotEmpty())
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
    }

    private fun openFollowing() {
        openDialogWithListOfUsers(
            statusFlow = mProfileViewModel.getFollowing(),
            title = R.string.following,
            emptyText = R.string.user_have_no_following,
            errorText = R.string.something_went_wrong
        )
    }

    private fun openFollowers() {
        openDialogWithListOfUsers(
            statusFlow = mProfileViewModel.getFollowers(),
            title = R.string.followers,
            emptyText = R.string.user_have_no_followers,
            errorText = R.string.something_went_wrong
        )
    }

    fun navigateFragment(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(id, fragment, "tag")
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

}