package com.datn.thesocialnetwork.feature.profile.editprofile.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.EventMessageStatus
import com.datn.thesocialnetwork.core.util.ViewUtils.setViewAndChildrenEnabled
import com.datn.thesocialnetwork.databinding.FragmentChangePasswdBinding
import com.datn.thesocialnetwork.databinding.FragmentChatBinding
import com.datn.thesocialnetwork.databinding.FragmentCreatePostBinding
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.profile.editprofile.viewmodel.EditProfileViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest


class ChangePasswdFragment : DialogFragment(R.layout.fragment_change_passwd) {
    val viewModel: EditProfileViewModel by viewModels()
    private var _bd: FragmentChangePasswdBinding? = null
    lateinit var binding: FragmentChangePasswdBinding
    lateinit var mMainActivity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme)
        mMainActivity = activity as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _bd = FragmentChangePasswdBinding.bind(view)
        binding = _bd!!
        setObserveData()
        setEvent()
    }

    private fun setEvent() {
        binding.btnChangePwd.setOnClickListener {
            viewModel.changePasswd()
        }
    }

    private fun setObserveData() {
        lifecycleScope.launchWhenStarted {
            viewModel.updateStatus.collectLatest {
                when (it) {
                    EventMessageStatus.Sleep -> {
                        setLoadingState(false)
                    }
                    EventMessageStatus.Loading -> {
                        setLoadingState(true)
                    }
                    is EventMessageStatus.Success -> {
                        setLoadingState(false)
                        it.eventMessage.getContentIfNotHandled()?.let { message ->
                            mMainActivity.showSnackbar(
                                message = message.getFormattedMessage(requireContext()),
                                length = Snackbar.LENGTH_SHORT,
                                buttonText = getString(R.string.ok)
                            )
                        }
                        findNavController().popBackStack()
                    }
                    is EventMessageStatus.Failed -> {
                        setLoadingState(false)
                        it.eventMessage.getContentIfNotHandled()?.let { message ->
                            mMainActivity.showSnackbar(
                                message = message.getFormattedMessage(requireContext()),
                                length = Snackbar.LENGTH_SHORT,
                                buttonText = getString(R.string.ok)
                            )
                        }
                    }
                }
            }
        }

    }

    private fun setLoadingState(isLoading: Boolean) {
        with(binding)
        {
            progressBarChangeEmail.isVisible = isLoading
            root.alpha = if (isLoading) 0.5f else 1f
            root.setViewAndChildrenEnabled(!isLoading)
        }
    }
}