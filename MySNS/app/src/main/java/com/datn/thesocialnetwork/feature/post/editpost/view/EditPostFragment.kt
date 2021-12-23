package com.datn.thesocialnetwork.feature.post.editpost.view

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.EventMessageStatus
import com.datn.thesocialnetwork.core.util.ViewUtils.setActionBarTitle
import com.datn.thesocialnetwork.core.util.ViewUtils.showSnackbarGravity
import com.datn.thesocialnetwork.core.util.ViewUtils.viewBinding
import com.datn.thesocialnetwork.databinding.FragmentEditPostBinding
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.post.detailpost.view.DetailPostFragment
import com.datn.thesocialnetwork.feature.post.editpost.viewmodel.EditPostViewModel
import com.datn.thesocialnetwork.feature.post.viewholder.DetailPostPhotosApdapter
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.hendraanggrian.appcompat.widget.SocialEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class EditPostFragment : Fragment(R.layout.fragment_edit_post) {

    companion object {
        private const val POST_ID = "POST_ID"

        fun newInstance(): EditPostFragment {
            val editPostFragment = EditPostFragment()
            return editPostFragment
        }
    }

    private lateinit var helper: SocialEditText

    @Inject
    lateinit var glide: RequestManager

    private val binding by viewBinding(FragmentEditPostBinding::bind)
    lateinit var mMainActivity: MainActivity
    val viewModel: EditPostViewModel by activityViewModels()

    private lateinit var post: PostWithId
    private var postId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setObserveData()
        setEvent()
        setInit()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_save -> {
                clickUpdate()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clickUpdate() {
        val base = viewModel.basePost.value
        val new = viewModel.updatedPost.value
        helper.setText(base.content)
        val oldHashtags = helper.hashtags
        val oldMentions = helper.mentions
        helper.setText(new.content)
        val newHashtags = helper.hashtags
        val newMentions = helper.mentions

        lifecycleScope.launchWhenStarted {
            viewModel.save(newHashtags, newMentions, oldHashtags, oldMentions)
                .collectLatest { status ->
                    when (status)
                    {
                        EventMessageStatus.Sleep ->
                        {
                            binding.progressBarEdit.isVisible = false
                        }
                        EventMessageStatus.Loading ->
                        {
                            binding.progressBarEdit.isVisible = true
                        }
                        is EventMessageStatus.Success ->
                        {
                            binding.progressBarEdit.isVisible = false
                            status.eventMessage.getContentIfNotHandled()?.let { msg ->
                                binding.cdRoot.showSnackbarGravity(
                                    msg.getFormattedMessage(
                                        requireContext()
                                    )
                                )
                            }
                        }
                        is EventMessageStatus.Failed ->
                        {
                            binding.progressBarEdit.isVisible = false
                            status.eventMessage.getContentIfNotHandled()?.let { msg ->
                                binding.cdRoot.showSnackbarGravity(
                                    msg.getFormattedMessage(
                                        requireContext()
                                    )
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun setObserveData() {
//        viewModel.postWithId.observe(viewLifecycleOwner,{ data ->
        val data = viewModel.postWithId.value
            Log.d("editpost","receive data from livedata ${data.toString()}")
        if (data != null) {
            viewModel.initPost(data)
        }
//        })
    }

    private fun setInit() {
        setActionBarTitle("Chỉnh sửa bài viết")
        mMainActivity.bd.toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_24)
        mMainActivity.bd.bottomAppBar.visibility = View.GONE
        mMainActivity.bd.fabAdd.visibility = View.GONE
        helper = SocialEditText(requireContext())

        binding.edtContent.doAfterTextChanged {
            viewModel.updateContent(it.toString())
        }
    }

    private fun setEvent()
    {
        lifecycleScope.launchWhenStarted {
            viewModel.basePost.collectLatest {
                Log.d("editPost", "post model ${it.toString()}")
                binding.edtContent.setText(it.content)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.basePostImage.collectLatest {
                Log.d("editPost", "post images ${it.toString()}")
                binding.itemFeedPhotos.adapter = DetailPostPhotosApdapter(it)
                binding.itemFeedPhotoIndicator.setViewPager2(binding.itemFeedPhotos)
                if (it?.size == 1) {
                    binding.itemFeedPhotoIndicator.visibility = View.GONE
                } else {
                    binding.itemFeedPhotoIndicator.visibility = View.VISIBLE
                }
            }
        }



        mMainActivity.bd.toolbar.setNavigationOnClickListener {
            mMainActivity.onBackPressed()
        }
    }

}