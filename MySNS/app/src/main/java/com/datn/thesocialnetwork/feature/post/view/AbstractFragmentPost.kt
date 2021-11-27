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
import com.datn.thesocialnetwork.core.util.ViewUtils.showSnackbarGravity
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

abstract class AbstractFragmentPost(
    @LayoutRes layout: Int
) : Fragment(layout), PostClickListener
{
    @Inject
    lateinit var userAdapter: UserAdapter

    @ExperimentalCoroutinesApi
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
//        viewModel.setLikeStatus(postId, status)
    }

    override fun shareClick(postId: String)
    {
//        startActivity(getShareIntent(Constants.getShareLinkToPost(postId)))
    }

    private var searchUsersJob: Job? = null

    private var alertDialog: AlertDialog? = null

    @ExperimentalCoroutinesApi
    override fun likeCounterClick(postId: String)
    {
//        openDialogWithListOfUsers(
//            statusFlow = viewModel.getUsersThatLikePost(postId),
//            title = R.string.users_that_like_post,
//            emptyText = R.string.empty_users_liking_post,
//            errorText = R.string.something_went_wrong_loading_users_that_liked_post
//        )
    }


    override fun linkClick(link: String)
    {
//        requireContext().tryOpenUrl(link) {
//            showSnackbar(R.string.could_not_open_browser)
//        }
    }

    override fun menuReportClick(postId: String)
    {
//        val d = LayoutInflater.from(requireContext())
//            .inflate(R.layout.report_dialog, null, false)
//
//        val reportTextField = d.findViewById<TextInputEditText>(R.id.edTxtReportText)
//
//        materialAlertDialogBuilder.setView(d)
//            .setTitle(getString(R.string.report_post))
//            .setMessage(getString(R.string.report_tip))
//            .setPositiveButton(getString(R.string.report)) { dialog, _ ->
//                val reportText = reportTextField.input
//                Timber.d("Report send $reportText")
//                dialog.dismiss()
//                viewModel.reportPost(postId, reportText)
//                showSnackbar(R.string.thanks_for_reporting)
//            }
//            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
//                dialog.dismiss()
//            }
//            .show()
    }


    // endregion

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