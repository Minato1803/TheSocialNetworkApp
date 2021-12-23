package com.datn.thesocialnetwork.feature.post.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.listener.PostClickListener
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.ModelMapping
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.core.util.ViewUtils.showSnackbarGravity
import com.datn.thesocialnetwork.core.util.ViewUtils.tryOpenUrl
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.datn.thesocialnetwork.feature.post.viewmodel.ViewModelPost
import com.datn.thesocialnetwork.feature.profile.adapter.UserAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
@ExperimentalCoroutinesApi
abstract class AbstractFragmentPost(
    @LayoutRes layout: Int
) : Fragment(layout), PostClickListener
{
    @Inject
    lateinit var userAdapter: UserAdapter

    protected abstract val viewModel: ViewModelPost

    protected abstract val binding: ViewBinding

    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())

        userAdapter.userClick = {
            alertDialog?.cancel()
            profileClick(it.uidUser)
        }
    }

    @Suppress("SameParameterValue")
    protected open fun showSnackbar(@StringRes message: Int)
    {
        (binding.root as? CoordinatorLayout)?.showSnackbarGravity(getString(message))
    }


    override fun likeClick(postId: String, status: Boolean)
    {
        viewModel.setLikeStatus(postId, status)
    }

    override fun markClick(postId: String, status: Boolean) {
        viewModel.setMarkStatus(postId, status)
    }

    private var searchUsersJob: Job? = null

    private var alertDialog: AlertDialog? = null


    override fun likeCounterClick(postId: String)
    {
        openDialogWithListOfUsers(
            statusFlow = viewModel.getUsersThatLikePost(postId),
            title = R.string.users_that_like_post,
            emptyText = R.string.empty_users_liking_post,
            errorText = R.string.something_went_wrong_loading_users_that_liked_post
        )
    }


    override fun linkClick(link: String)
    {
        requireContext().tryOpenUrl(link) {
            showSnackbar(R.string.could_not_open_browser)
        }
    }

    override fun deletePostClick(post: PostWithId)
    {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.str_delete_post))
            .setMessage(resources.getString(R.string.delete_confirmation))
            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ ->
            }
            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                //delete post
                viewModel.deletePost(post.first)
            }
            .show()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        userAdapter.cancelScopes()
    }

    @ExperimentalCoroutinesApi
    protected fun openDialogWithListOfUsers(
        statusFlow: Flow<GetStatus<List<String>>>,
        @StringRes title: Int,
        @StringRes emptyText: Int,
        @StringRes errorText: Int
    )
    {
        val d = LayoutInflater.from(requireContext())
            .inflate(R.layout.users_dialog, null, false)

        val rvUsers = d.findViewById<RecyclerView>(R.id.rvUsers)
        val proBarLoading = d.findViewById<ProgressBar>(R.id.proBarLoading)
        val txtInfo = d.findViewById<MaterialTextView>(R.id.txtInfo)
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
                when (it)
                {
                    GetStatus.Sleep -> Unit
                    GetStatus.Loading ->
                    {
                        rvUsers.isVisible = false
                        proBarLoading.isVisible = true
                        txtInfo.isVisible = false
                    }
                    is GetStatus.Success ->
                    {
                        userAdapter.submitList(it.data)
                        proBarLoading.isVisible = false

                        if (it.data.isNotEmpty())
                        {
                            rvUsers.isVisible = true
                            txtInfo.isVisible = false
                        }
                        else
                        {
                            rvUsers.isVisible = false
                            txtInfo.isVisible = true
                            txtInfo.setText(emptyText)
                        }
                    }
                    is GetStatus.Failed ->
                    {
                        rvUsers.isVisible = false
                        proBarLoading.isVisible = false
                        txtInfo.isVisible = true
                        txtInfo.setText(errorText)
                    }
                }
            }
        }
    }

}