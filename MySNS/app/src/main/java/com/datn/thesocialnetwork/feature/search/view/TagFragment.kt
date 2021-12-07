package com.datn.thesocialnetwork.feature.search.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.SystemUtils.normalize
import com.datn.thesocialnetwork.core.util.ViewUtils.setActionBarTitle
import com.datn.thesocialnetwork.core.util.ViewUtils.showSnackbarGravity
import com.datn.thesocialnetwork.core.util.ViewUtils.viewBinding
import com.datn.thesocialnetwork.data.repository.model.TagModel
import com.datn.thesocialnetwork.data.repository.model.UserModel
import com.datn.thesocialnetwork.data.repository.model.post.StateData
import com.datn.thesocialnetwork.databinding.FragmentTagBinding
import com.datn.thesocialnetwork.feature.chat.view.MessageFragment
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.post.comment.view.CommentFragment
import com.datn.thesocialnetwork.feature.post.detailpost.view.DetailPostFragment
import com.datn.thesocialnetwork.feature.post.editpost.view.EditPostFragment
import com.datn.thesocialnetwork.feature.post.editpost.viewmodel.EditPostViewModel
import com.datn.thesocialnetwork.feature.post.view.AbstractFragmentRcv
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.datn.thesocialnetwork.feature.profile.view.ProfileFragment
import com.datn.thesocialnetwork.feature.profile.view.UserFragment
import com.datn.thesocialnetwork.feature.search.viewmodel.TagViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class TagFragment : AbstractFragmentRcv(
    R.layout.fragment_tag,
    StateData(
        emptyStateIcon = R.drawable.ic_dynamic_feed_24,
        emptyStateText = R.string.nothing_to_show_home,
        bottomRecyclerPadding = R.dimen._142dp
    )
) {
    companion object {
        private const val TAG_DATA = "TAG_DATA"
        fun newInstance(
            tagModel: TagModel,
        ): MessageFragment {
            val messageFragment = MessageFragment()
            val arg = Bundle()
            arg.putParcelable(TAG_DATA, tagModel)
            messageFragment.arguments = arg
            return messageFragment
        }
    }

    @Inject
    lateinit var glide: RequestManager

    override val binding by viewBinding(FragmentTagBinding::bind)
    lateinit var mMainActivity: MainActivity
    override val viewModel: TagViewModel by viewModels()
    private val editPostViewModel: EditPostViewModel by activityViewModels()

    private var tagModel: TagModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setInit()
        setObserveData()
    }

    private fun extractData() {
        tagModel = arguments?.getParcelable(TAG_DATA)
    }

    private fun setInit() {
        extractData()
        viewModel.initTag(tagModel!!)
        setActionBarTitle(getString(R.string.tag_title_format, tagModel!!.title))
    }

    private fun setObserveData() {
        lifecycleScope.launchWhenStarted {
            viewModel.tag.collectLatest {
                when (it) {
                    GetStatus.Sleep -> Unit
                    is GetStatus.Failed -> {
                    }
                    GetStatus.Loading -> {
                        with(binding)
                        {
                            tvPosts.text = getString(R.string.str_loading_dot)
                        }
                    }
                    is GetStatus.Success -> {
                        with(binding)
                        {
                            tvPosts.text = getString(R.string.posts_counter, it.data.count.toInt())
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.postToDisplay.collectLatest { postsStatus ->
                if (postsStatus is GetStatus.Success) {
                    glide
                        .load(postsStatus.data.maxByOrNull { it.second.createdTime }?.third?.get(0)?.second?.imageUrl)
                        .into(binding.imgAvartar)
                }
            }
        }
    }

    override fun profileClick(postOwner: String) {
        if (viewModel.isOwnAccountId(postOwner)) {
            val profileFragment = ProfileFragment()
            navigateFragment(profileFragment, "profileFragment")
        } else {
            val userFragment =
                UserFragment.newInstance(UserModel(uidUser = postOwner), isLoadFromDb = true)
            navigateFragment(userFragment, "userFragment")
        }
    }

    override fun commentClick(postId: String) {
        val commentFragment = CommentFragment.newInstance(postId)
        navigateFragment(commentFragment, "commentFragment")
    }

    override fun imageClick(postWithId: PostWithId) {
        val detailPostFragment = DetailPostFragment.newInstance(postWithId.first)
        navigateFragment(detailPostFragment, "detailPostFragment")
    }


    override fun mentionClick(mention: String) {
        if (viewModel.isOwnAccountUsername(mention)) // user clicked on own profile
        {
            val profileFragment = ProfileFragment()
            navigateFragment(profileFragment, "profileFragment")
        } else {
            val userFragment =
                UserFragment.newInstance(UserModel(userName = mention), isLoadFromDb = true)
            navigateFragment(userFragment, "userFragment")
        }
    }

    override fun menuEditClick(post: PostWithId) {
        Log.d("editPost", "${post.toString()}")
        editPostViewModel.postWithId.postValue(post)
        val editPostFragment = EditPostFragment.newInstance()
        navigateFragment(editPostFragment, "editPostFragment")
    }

    override fun tagClick(tag: String) {
        if (tag.lowercase() == (viewModel.tag.value as? GetStatus.Success<TagModel>)?.data?.title.toString().lowercase())
        {
            showSnackbar(R.string.currently_on_this_tag)
        } else {
            val tags = TagModel(tag,-1)
            val tagFragment = TagFragment.newInstance(tags)
            navigateFragment(tagFragment, "tagFragment")
        }
    }

    private fun navigateFragment(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(id, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

}