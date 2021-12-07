package com.datn.thesocialnetwork.feature.chat.view

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.core.util.ViewUtils.setActionBarTitle
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.databinding.FragmentChatBinding
import com.datn.thesocialnetwork.feature.chat.adapter.ConversationAdapter
import com.datn.thesocialnetwork.feature.chat.viewmodel.ChatViewModel
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.profile.editprofile.view.EditProfileFragment
import com.datn.thesocialnetwork.feature.profile.view.UserFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ChatFragment : Fragment(R.layout.fragment_chat) {
    companion object {
        private const val USER_DATA = "USER_DATA"
        fun newInstance(
            userModel: UserModel
        ): ChatFragment {
            val chatFragment = ChatFragment()
            val arg = Bundle()
            arg.putParcelable(USER_DATA, userModel)
            chatFragment.arguments = arg
            return chatFragment
        }
    }

    @Inject
    lateinit var conversationAdapter: ConversationAdapter

    private var bd: FragmentChatBinding? = null
    lateinit var binding: FragmentChatBinding
    lateinit var mMainActivity: MainActivity
    private val viewModel: ChatViewModel by activityViewModels()
    private var userModel : UserModel? = null
    lateinit var actionBarDrawerToggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        bd = FragmentChatBinding.inflate(layoutInflater, container, false)
        binding = bd!!
        setInit()
        setObserveData()
        setEvent()
        return binding.root
    }

    private fun extractData() {
        userModel = arguments?.getParcelable(USER_DATA)
    }

    private fun setInit() {
        extractData()
        setActionBarTitle("Tin nhắn")
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

        binding.rvConversations.adapter = conversationAdapter.apply {
            actionMessageClick = ::conversationClick
        }
    }

    private fun conversationClick(user: UserModel)
    {
        val messageFragment = MessageFragment.newInstance(user)
        navigateFragment(messageFragment,"messageFragment")
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
                        Log.d("AllListChat", "${it.data.toString()}")
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
        //

    }

    private fun setEvent() {
        binding.root.setOnRefreshListener {
            viewModel.updateConversations()
            setObserveData()
            binding.root.isRefreshing = false
        }
    }

    private fun navigateFragment(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(id, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId)
        {
            R.id.miAdd ->
            {
                // create group chat
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
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