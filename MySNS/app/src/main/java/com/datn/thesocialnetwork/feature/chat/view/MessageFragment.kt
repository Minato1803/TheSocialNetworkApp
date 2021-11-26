package com.datn.thesocialnetwork.feature.chat.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.FirebaseStatus
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.core.util.ViewUtils.setActionBarTitle
import com.datn.thesocialnetwork.core.util.ViewUtils.showSnackbarGravity
import com.datn.thesocialnetwork.data.repository.model.ChatMessage
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.databinding.FragmentMessageBinding
import com.datn.thesocialnetwork.feature.chat.adapter.ConversationAdapter
import com.datn.thesocialnetwork.feature.chat.adapter.MessageAdapter
import com.datn.thesocialnetwork.feature.chat.adapter.MessageClickListener
import com.datn.thesocialnetwork.feature.chat.viewmodel.MessagesViewModel
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.profile.view.UserFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MessageFragment : Fragment(R.layout.fragment_message) {

    companion object {
        private const val USER_DATA = "USER_DATA"
        fun newInstance(
            userModel: UserModel,
        ): MessageFragment {
            val messageFragment = MessageFragment()
            val arg = Bundle()
            arg.putParcelable(USER_DATA, userModel)
            messageFragment.arguments = arg
            return messageFragment
        }
    }

    @Inject
    lateinit var messageAdapter: MessageAdapter

    private var _bd: FragmentMessageBinding? = null
    lateinit var binding: FragmentMessageBinding
    lateinit var mMainActivity: MainActivity
    private val viewModel: MessagesViewModel by activityViewModels()

    private var userModel: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _bd = FragmentMessageBinding.bind(view)
        binding = _bd!!

        setInit()
        setObserveData()
        setEvent()
        setupRecycler()
    }

    private fun extractData() {
        userModel = arguments?.getParcelable(USER_DATA)
    }

    private fun setInit() {
        extractData()
        viewModel.initViewModel(userModel!!)
        setActionBarTitle(userModel!!.userName)
        mMainActivity.bd.toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_24)
        mMainActivity.bd.bottomAppBar.visibility = View.GONE
        mMainActivity.bd.fabAdd.visibility = View.GONE
    }

    private fun setObserveData() {
        lifecycleScope.launchWhenStarted {
            viewModel.allMassages.collectLatest { status ->
                when (status) {
                    GetStatus.Sleep -> {
                        binding.progressBarMessages.isVisible = false
                        binding.linLayNoMsg.isVisible = false
                    }
                    GetStatus.Loading -> {
                        binding.progressBarMessages.isVisible = true
                        binding.linLayNoMsg.isVisible = false
                    }
                    is GetStatus.Success -> {
                        binding.progressBarMessages.isVisible = false

                        messageAdapter.submitList(status.data)
                        binding.linLayNoMsg.isVisible = status.data.isEmpty()
                        binding.rvMessages.post {
                            binding.rvMessages.scrollToPosition(0)
                        }
                    }
                    is GetStatus.Failed -> {
                        binding.progressBarMessages.isVisible = false
                        binding.linLayNoMsg.isVisible = false

                        binding.cdRoot.showSnackbarGravity(
                            message = status.message.getFormattedMessage(
                                requireContext()
                            )
                        )
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.sendingMessageStatus.collectLatest {
                when (it) {
                    FirebaseStatus.Sleep -> {
                        binding.progressBarSending.isVisible = false
                    }
                    FirebaseStatus.Loading -> {
                        binding.progressBarSending.isVisible = true
                    }
                    is FirebaseStatus.Success -> {
                        binding.progressBarSending.isVisible = false
                        binding.edTxtMessage.setText("")
                    }
                    is FirebaseStatus.Failed -> {
                        binding.progressBarSending.isVisible = false
                        binding.cdRoot.showSnackbarGravity(
                            message = it.message.getFormattedMessage(
                                requireContext()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun copyTextClick(chatMessage: ChatMessage) {
        chatMessage.textContent?.let { textToCopy ->
            val clipboardManager =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(getString(R.string.copied_message), textToCopy)
            clipboardManager.setPrimaryClip(clipData)
        }
    }

    private fun deleteMessageClick(chatMessage: ChatMessage) {
        //Todo: not implement yet
    }


    private fun setupRecycler() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.reverseLayout = true


        messageAdapter.messageClickListener = MessageClickListener(
            copyText = ::copyTextClick,
            deleteMessage = ::deleteMessageClick
        )

        with(binding.rvMessages)
        {
            adapter = messageAdapter
            layoutManager = linearLayoutManager
        }

        messageAdapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    binding.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeRemoved(
                    positionStart: Int,
                    itemCount: Int,
                ) {
                    binding.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeMoved(
                    fromPosition: Int,
                    toPosition: Int,
                    itemCount: Int,
                ) {
                    binding!!.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeInserted(
                    positionStart: Int,
                    itemCount: Int,
                ) {
                    binding.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeChanged(
                    positionStart: Int,
                    itemCount: Int,
                ) {
                    binding.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeChanged(
                    positionStart: Int,
                    itemCount: Int,
                    payload: Any?,
                ) {
                    binding.rvMessages.smoothScrollToPosition(0)
                }
            }
        )
    }

    private fun setEvent() {
        binding.butSend.setOnClickListener {
            viewModel.messageText.value = binding.edTxtMessage.text.toString().trim()
            viewModel.sendMessage()
            setObserveData()
        }

        mMainActivity.bd.toolbar.setNavigationOnClickListener {
            SystemUtils.hideKeyboard(requireContext())
            mMainActivity.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

}