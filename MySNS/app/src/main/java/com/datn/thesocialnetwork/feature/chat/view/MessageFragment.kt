package com.datn.thesocialnetwork.feature.chat.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.FirebaseStatus
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.TimeUtils
import com.datn.thesocialnetwork.core.util.ViewUtils.showSnackbarGravity
import com.datn.thesocialnetwork.data.repository.model.ChatMessage
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.databinding.FragmentMessageBinding
import com.datn.thesocialnetwork.feature.chat.adapter.MessageAdapter
import com.datn.thesocialnetwork.feature.chat.adapter.MessageClickListener
import com.datn.thesocialnetwork.feature.chat.viewmodel.MessagesViewModel
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.notification.*
import com.google.firebase.database.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import javax.security.auth.callback.Callback

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MessageFragment : DialogFragment(R.layout.fragment_message) {

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

    @Inject
    lateinit var glide: RequestManager

    private var _bd: FragmentMessageBinding? = null
    lateinit var binding: FragmentMessageBinding
    lateinit var mMainActivity: MainActivity
    private val viewModel: MessagesViewModel by viewModels()

    private var userModel: UserModel? = null

//    val apiService =
//        Client.getRetrofit("https://fcm.googleapis.com/")?.create(APIService::class.java)
//    var notify: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
        mMainActivity = activity as MainActivity
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
        with(binding) {
            glide.load(userModel!!.avatarUrl)
                .into(imgAvatar)
            tvUserName.text = userModel!!.userName
            if (userModel!!.onlineStatus == 0L) {
                tvUserStatus.text = "Online"
            } else {
                tvUserStatus.text = "Truy cập ${TimeUtils.showTimeDetail(userModel!!.onlineStatus)}"
            }
        }
        viewModel.initViewModel(userModel!!)
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
        viewModel.deleteMessage(chatMessage)
        setObserveData()
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
                    binding.rvMessages.smoothScrollToPosition(0)
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
//            notify = true
            viewModel.messageText.value = binding.edTxtMessage.text.toString().trim()
            viewModel.sendMessage()
            setObserveData()
//            sendNotification(viewModel.selectedUser.uidUser,
//                GlobalValue.USER?.userDetail!!.userName,
//                binding.edTxtMessage.text.toString().trim())
        }

        binding.imgBack.setOnClickListener {
            this.dismiss()
        }
    }

//    private fun sendNotification(uidReceiver: String, userName: String, message: String) {
//        val allTokens = FirebaseDatabase.getInstance().getReference("Tokens")
//        val query: Query = allTokens.orderByKey().equalTo(uidReceiver)
//        query.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (ds in dataSnapshot.children) {
//                    val token: Token? = ds.getValue(Token::class.java)
//                    val data = NotiModel(
//                        "" + GlobalValue.USER!!.uidUser,
//                        "$userName: $message",
//                        "New Message",
//                        "" + uidReceiver,
//                        "ChatNotification",
//                        R.drawable.ic_profile_24)
//                    val sender = token?.let { Sender(data, it.token) }
//                    if (sender != null) {
//                        apiService?.sendNotification(sender)
//                            ?.enqueue(object : retrofit2.Callback<ResponseNoti> {
//                                override fun onResponse(
//                                    call: Call<ResponseNoti>,
//                                    response: Response<ResponseNoti>,
//                                ) {
//                                    Log.d("sendNotiSucc", "${response.message().toString()}")
//                                }
//
//                                override fun onFailure(call: Call<ResponseNoti>, t: Throwable) {
//                                    Log.d("sendNotiFail", "${t.message.toString()}")
//                                }
//                            })
//                    }
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {}
//        })
//    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

}