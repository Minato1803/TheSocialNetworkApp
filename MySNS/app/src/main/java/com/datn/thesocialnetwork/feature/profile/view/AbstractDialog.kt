package com.datn.thesocialnetwork.feature.profile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.feature.profile.adapter.UserAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

abstract class AbstractDialog (@LayoutRes layout: Int
) : Fragment(layout)
{
    @Inject
    lateinit var userAdapter: UserAdapter

    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())

        userAdapter.userClick = {
            alertDialog?.cancel()
        }
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
    private var alertDialog: AlertDialog? = null
    private var searchUsersJob: Job? = null
}