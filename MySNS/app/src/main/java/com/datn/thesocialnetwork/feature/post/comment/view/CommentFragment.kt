package com.datn.thesocialnetwork.feature.post.comment.view

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.DataStatus
import com.datn.thesocialnetwork.core.api.status.FirebaseStatus
import com.datn.thesocialnetwork.core.util.ViewUtils.setActionBarTitle
import com.datn.thesocialnetwork.core.util.ViewUtils.showSnackbarGravity
import com.datn.thesocialnetwork.databinding.FragmentCommentBinding
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.post.comment.adapter.CommentAdapter
import com.datn.thesocialnetwork.feature.post.comment.viewmodel.CommentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class CommentFragment : Fragment(R.layout.fragment_comment) {

    companion object {
        private const val POST_ID = "POST_ID"
        fun newInstance(
            postId: String,
        ): CommentFragment {
            val commentFragment = CommentFragment()
            val arg = Bundle()
            arg.putString(POST_ID, postId)
            commentFragment.arguments = arg
            return commentFragment
        }
    }

    @Inject
    lateinit var adapter: CommentAdapter

    private val viewModel: CommentViewModel by viewModels()
    lateinit var mMainActivity: MainActivity
    private var _bd: FragmentCommentBinding? = null
    lateinit var binding: FragmentCommentBinding

    private var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _bd = FragmentCommentBinding.bind(view)
        binding = _bd!!

        setInit()
        setObserveData()
        setEvent()
    }

    private fun extractData() {
        postId = arguments?.getString(POST_ID)
    }

    private fun setInit() {
        extractData()
        setActionBarTitle("Bình luận")
        mMainActivity.bd.toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_24)
        mMainActivity.bd.bottomAppBar.visibility = View.GONE
        mMainActivity.bd.fabAdd.visibility = View.GONE
        binding.rvComments.adapter = adapter
    }

    private fun setObserveData() {
        lifecycleScope.launchWhenStarted {
            postId?.let { postId ->
                viewModel.getComments(postId).collectLatest {
                    when (it) {
                        DataStatus.Loading -> {
                            binding.progressBarComments.isVisible = true
                            showNoCommentsInfo(false)
                        }
                        is DataStatus.Success -> {
                            adapter.submitList(it.data.toList().sortedBy { comment ->
                                comment.second.time
                            })
                            binding.progressBarComments.isVisible = false

                            showNoCommentsInfo(it.data.count() == 0)

                        }
                        is DataStatus.Failed -> {
                            binding.progressBarComments.isVisible = false
                            showNoCommentsInfo(false)
                            binding.root.showSnackbarGravity(
                                message = getString(R.string.something_went_wrong)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showNoCommentsInfo(isVisible: Boolean) {
        binding.imgCommentIcon.isVisible = isVisible
        binding.tvNoComments.isVisible = isVisible
        binding.tvWrite.isVisible = isVisible
    }

    private fun setEvent() {
        with(binding) {
            btnPost.setOnClickListener {
                lifecycleScope.launch {
                    postId?.let { postId ->
                        viewModel.addComment(postId, edtComment.text.toString().trim())
                            .collectLatest {

                                when (it) {
                                    FirebaseStatus.Sleep -> Unit
                                    FirebaseStatus.Loading -> {
                                        binding.progressBarPost.isVisible = true
                                        btnPost.isEnabled = false
                                    }
                                    is FirebaseStatus.Failed -> {
                                        binding.progressBarPost.isVisible = false
                                        btnPost.isEnabled = true
                                        binding.root.showSnackbarGravity(
                                            message = it.message.getFormattedMessage(requireContext())
                                        )
                                    }
                                    is FirebaseStatus.Success -> {
                                        binding.progressBarPost.isVisible = false
                                        btnPost.isEnabled = true
                                        binding.edtComment.setText("")
                                    }
                                }
                            }
                    }
                }
            }
        }
        mMainActivity.bd.toolbar.setNavigationOnClickListener {
            mMainActivity.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        hideKeyboard(requireContext())
    }
}