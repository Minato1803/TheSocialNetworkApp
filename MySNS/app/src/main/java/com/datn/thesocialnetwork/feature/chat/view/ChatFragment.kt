package com.datn.thesocialnetwork.feature.chat.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.FirebaseStatus
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.ViewUtils.showSnackbarGravity
import com.datn.thesocialnetwork.data.repository.model.ChatMessage
import com.datn.thesocialnetwork.databinding.FragmentChatBinding
import com.datn.thesocialnetwork.feature.chat.adapter.ChatAdapter
import com.datn.thesocialnetwork.feature.chat.viewmodel.ChatViewModel
import com.datn.thesocialnetwork.feature.chat.adapter.MessageClickListener
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.fragment_chat) {
    @Inject
    lateinit var chatAdapter: ChatAdapter

    private var binding: FragmentChatBinding? = null
    lateinit var bd: FragmentChatBinding
    lateinit var mMainActivity: MainActivity
    private val viewModel: ChatViewModel by viewModels()

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
        binding = FragmentChatBinding.inflate(layoutInflater, container, false)
        bd = binding!!
        setInit()
        setObserveData()
        setEvent()
        setupRecycler()
        return binding!!.root
    }

    private fun setInit() {
        //TODO("Not yet implemented")
    }

    private fun setObserveData() {
        lifecycleScope.launchWhenStarted {
            viewModel.allMassages.collectLatest { status ->
                when (status)
                {
                    GetStatus.Sleep ->
                    {
                        binding!!.progressBarMessages.isVisible = false
                        binding!!.linLayNoMsg.isVisible = false
                    }
                    GetStatus.Loading ->
                    {
                        binding!!.progressBarMessages.isVisible = true
                        binding!!.linLayNoMsg.isVisible = false
                    }
                    is GetStatus.Success ->
                    {
                        binding!!.progressBarMessages.isVisible = false

                        chatAdapter.submitList(status.data)
                        binding!!.linLayNoMsg.isVisible = status.data.isEmpty()
//                        binding.rvMessages.post {
//                            binding.rvMessages.scrollToPosition(0)
//                        }
                    }
                    is GetStatus.Failed ->
                    {
                        binding!!.progressBarMessages.isVisible = false
                        binding!!.linLayNoMsg.isVisible = false

                        binding!!.cdRoot.showSnackbarGravity(
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
                when (it)
                {
                    FirebaseStatus.Sleep ->
                    {
                        binding!!.progressBarSending.isVisible = false
                    }
                    FirebaseStatus.Loading ->
                    {
                        binding!!.progressBarSending.isVisible = true
                    }
                    is FirebaseStatus.Success ->
                    {
                        binding!!.progressBarSending.isVisible = false
                        binding!!.edTxtMessage.setText("")
                    }
                    is FirebaseStatus.Failed ->
                    {
                        binding!!.progressBarSending.isVisible = false
                        binding!!.cdRoot.showSnackbarGravity(
                            message = it.message.getFormattedMessage(
                                requireContext()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun setEvent() {
        //TODO("Not yet implemented")
    }

    private fun copyTextClick(chatMessage: ChatMessage)
    {
        chatMessage.textContent?.let { textToCopy ->
            val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(getString(R.string.copied_message), textToCopy)
            clipboardManager.setPrimaryClip(clipData)
        }
    }

    private fun deleteMessageClick(chatMessage: ChatMessage)
    {
        //Todo: not implement yet
    }


    private fun setupRecycler()
    {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.reverseLayout = true


        chatAdapter.messageClickListener = MessageClickListener(
            copyText = ::copyTextClick,
            deleteMessage = ::deleteMessageClick
        )

        with(binding!!.rvMessages)
        {
            adapter = chatAdapter
            layoutManager = linearLayoutManager
        }

        chatAdapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver()
            {
                override fun onChanged()
                {
                    binding!!.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeRemoved(
                    positionStart: Int,
                    itemCount: Int
                )
                {
                    binding!!.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeMoved(
                    fromPosition: Int,
                    toPosition: Int,
                    itemCount: Int
                )
                {
                    binding!!.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeInserted(
                    positionStart: Int,
                    itemCount: Int
                )
                {
                    binding!!.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeChanged(
                    positionStart: Int,
                    itemCount: Int
                )
                {
                    binding!!.rvMessages.smoothScrollToPosition(0)
                }

                override fun onItemRangeChanged(
                    positionStart: Int,
                    itemCount: Int,
                    payload: Any?
                )
                {
                    binding!!.rvMessages.smoothScrollToPosition(0)
                }
            }
        )
    }
}