package com.datn.thesocialnetwork.feature.chat.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.databinding.FragmentMessageBinding
import com.datn.thesocialnetwork.feature.chat.adapter.ConversationAdapter
import com.datn.thesocialnetwork.feature.chat.viewmodel.MessagesViewModel
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.search.view.UserFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MessageFragment : Fragment(R.layout.fragment_message) {

    @Inject
    lateinit var conversationAdapter: ConversationAdapter

    private var _bd: FragmentMessageBinding? = null
    lateinit var binding: FragmentMessageBinding
    lateinit var mMainActivity: MainActivity
    private val viewModel: MessagesViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        _bd = FragmentMessageBinding.bind(view)
        binding = _bd!!

        setInit()
        setObserveData()
        setEvent()
    }

    private fun setInit() {
        //TODO("Not yet implemented")
    }

    private fun setObserveData() {
        lifecycleScope.launchWhenStarted {
            viewModel.conversation.collectLatest {

                when (it)
                {
                    GetStatus.Sleep ->
                    {
                        binding.proBarLoadingConversations.isVisible = false
                        binding.linLayEmptyState.isVisible = false
                        binding.linLayErrorState.isVisible = false
                        binding.rvConversations.isVisible = false
                    }
                    GetStatus.Loading ->
                    {
                        binding.proBarLoadingConversations.isVisible = true
                        binding.linLayEmptyState.isVisible = false
                        binding.linLayErrorState.isVisible = false
                        binding.rvConversations.isVisible = false
                    }
                    is GetStatus.Success ->
                    {
                        binding.proBarLoadingConversations.isVisible = false
                        binding.linLayEmptyState.isVisible = it.data.isEmpty()
                        binding.rvConversations.isVisible = it.data.isNotEmpty()
                        binding.linLayErrorState.isVisible = false

                        conversationAdapter.submitList(it.data)
                    }
                    is GetStatus.Failed ->
                    {
                        binding.proBarLoadingConversations.isVisible = false
                        binding.linLayEmptyState.isVisible = false
                        binding.linLayErrorState.isVisible = true
                        binding.rvConversations.isVisible = false
                    }
                }
            }
        }
    }

    private fun setEvent() {
        //TODO("Not yet implemented")
    }

    private fun conversationClick(user: UserDetail)
    {
        val userFragment = UserFragment.newInstance(user)
        navigateFragment(userFragment,"userFragment")
    }

    private fun navigateFragment(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(id, fragment, "tag")
            .addToBackStack(null)
            .commit()
    }


    override fun onResume()
    {
        super.onResume()
        viewModel.updateConversations()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        conversationAdapter.cancelScopes()
    }

}