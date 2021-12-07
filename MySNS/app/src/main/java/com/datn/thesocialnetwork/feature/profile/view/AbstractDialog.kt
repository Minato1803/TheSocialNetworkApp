package com.datn.thesocialnetwork.feature.profile.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.MarginPageTransformer
import coil.ImageLoader
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.listener.PostClickListener
import com.datn.thesocialnetwork.core.util.SystemUtils.px
import com.datn.thesocialnetwork.core.util.ViewUtils.viewBinding
import com.datn.thesocialnetwork.data.repository.model.post.StateData
import com.datn.thesocialnetwork.databinding.FragmentProfileBinding
import com.datn.thesocialnetwork.databinding.FragmentUserBinding
import com.datn.thesocialnetwork.feature.post.adapter.PostAdapter
import com.datn.thesocialnetwork.feature.profile.adapter.PostCategoryAdapter
import com.datn.thesocialnetwork.feature.profile.adapter.StateRecyclerData
import com.datn.thesocialnetwork.feature.profile.adapter.UserAdapter
import com.datn.thesocialnetwork.feature.profile.viewmodel.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@ExperimentalCoroutinesApi
abstract class AbstractDialog(
    @LayoutRes layout: Int,
) : Fragment(layout), PostClickListener {
    @Inject
    lateinit var userAdapter: UserAdapter

    @Inject
    lateinit var uploadAdapter: PostAdapter

    @Inject
    lateinit var mentionedAdapter: PostAdapter

    @Inject
    lateinit var likedAdapter: PostAdapter

    @Inject
    lateinit var imageLoader: ImageLoader
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    val profileBinding by viewBinding(FragmentProfileBinding::bind)
    val userBinding by viewBinding(FragmentUserBinding::bind)
    val viewModel: ProfileViewModel by viewModels()

    private var alertDialog: AlertDialog? = null
    private var searchUsersJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())

        userAdapter.userClick = {
            alertDialog?.cancel()
            profileClick(it.uidUser)
        }
    }

    @ExperimentalCoroutinesApi
    protected fun openDialogWithListOfUsers(
        statusFlow: Flow<GetStatus<List<String>>>,
        @StringRes title: Int,
        @StringRes emptyText: Int,
        @StringRes errorText: Int,
    ) {
        val d = LayoutInflater.from(requireContext())
            .inflate(R.layout.users_dialog, null, false)

        val rvUsers = d.findViewById<RecyclerView>(R.id.rvUsers)
        val proBarLoading = d.findViewById<ProgressBar>(R.id.proBarLoading)
        val txtInfo = d.findViewById<MaterialTextView>(R.id.txtInfo)
        rvUsers.layoutManager = LinearLayoutManager(requireContext())
        rvUsers.adapter = userAdapter

        alertDialog = materialAlertDialogBuilder.setView(d)
            .setTitle(getString(title))
            .setPositiveButton(R.string.close) { dialog, _ ->
                dialog.cancel()
            }
            .setOnCancelListener {
                searchUsersJob?.cancel()
            }
            .show()

        searchUsersJob = lifecycleScope.launchWhenStarted {
            statusFlow.collectLatest {
                when (it) {
                    GetStatus.Sleep -> Unit
                    GetStatus.Loading -> {
                        rvUsers.isVisible = false
                        proBarLoading.isVisible = true
                        txtInfo.isVisible = false
                    }
                    is GetStatus.Success -> {
                        Log.d("listFollow", it.data.toString())
                        userAdapter.submitList(it.data)
                        proBarLoading.isVisible = false

                        if (it.data.isNotEmpty()) {
                            rvUsers.isVisible = true
                            txtInfo.isVisible = false
                        } else {
                            rvUsers.isVisible = false
                            txtInfo.isVisible = true
                            txtInfo.setText(emptyText)
                        }
                    }
                    is GetStatus.Failed -> {
                        rvUsers.isVisible = false
                        proBarLoading.isVisible = false
                        txtInfo.isVisible = true
                        txtInfo.setText(errorText)
                    }
                }
            }
        }
    }

    protected fun initRecyclers(
        isProfileFragment: Boolean,
    ) {

        if (isProfileFragment) {
            profileBinding.vpRecyclers.setPageTransformer(MarginPageTransformer(8.px))
        } else {
            userBinding.vpRecyclers.setPageTransformer(MarginPageTransformer(8.px))
        }

        uploadAdapter.postClickListener = this
        mentionedAdapter.postClickListener = this
        likedAdapter.postClickListener = this

        val uploads = StateRecyclerData(
            viewModel.uploadedPosts,
            uploadAdapter,
            StateData(
                emptyStateIcon = R.drawable.ic_dynamic_feed_24,
                emptyStateText = if (isProfileFragment) R.string.no_uploads_profile else R.string.no_uploads_user,
                bottomRecyclerPadding = R.dimen._142dp
            )
        )

        val mentioned = StateRecyclerData(
            viewModel.mentionPosts,
            mentionedAdapter,
            StateData(
                emptyStateIcon = R.drawable.ic_mentions_24,
                emptyStateText = if (isProfileFragment) R.string.no_mentions_profile else R.string.no_mentions_user,
                bottomRecyclerPadding = R.dimen._142dp
            )
        )

        val liked = StateRecyclerData(
            viewModel.likedPosts,
            likedAdapter,
            StateData(
                emptyStateIcon = R.drawable.ic_heart,
                emptyStateText = if (isProfileFragment) R.string.no_likes_profile else R.string.no_liked_user,
                bottomRecyclerPadding = R.dimen._142dp
            )
        )

        val recyclers = listOf(uploads, mentioned, liked)

        val adapter = PostCategoryAdapter(recyclers)

        val names = ProfileViewModel.DisplayPostCategory.values().map {
            it.categoryName
        }
        if (isProfileFragment) {
            profileBinding.vpRecyclers.adapter = adapter

            TabLayoutMediator(profileBinding.tabsPostType, profileBinding.vpRecyclers) { tab, pos ->
                tab.text = getString(names[pos])
            }.attach()
        } else {
            userBinding.vpRecyclers.adapter = adapter

            TabLayoutMediator(userBinding.tabsPostType, userBinding.vpRecyclers) { tab, pos ->
                tab.text = getString(names[pos])
            }.attach()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userAdapter.cancelScopes()
    }
}