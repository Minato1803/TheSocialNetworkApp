package com.datn.thesocialnetwork.feature.home.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.util.ViewUtils.viewBinding
import com.datn.thesocialnetwork.data.repository.model.post.StateData
import com.datn.thesocialnetwork.databinding.FragmentHomeBinding
import com.datn.thesocialnetwork.feature.home.viewmodel.HomeViewModel
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.post.view.AbstractFragmentRcv
import com.datn.thesocialnetwork.feature.post.viewmodel.ViewModelStateRcv
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class HomeFragment() : AbstractFragmentRcv(
    R.layout.fragment_home,
    StateData(
        emptyStateIcon = R.drawable.ic_dynamic_feed_24,
        emptyStateText = R.string.nothing_to_show_home
    )
) {

    override val viewModel: HomeViewModel by activityViewModels()
    override val binding by viewBinding(FragmentHomeBinding::bind)
    lateinit var mMainActivity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
    }

    override fun profileClick(postOwner: String) {
        //TODO("Not yet implemented")
    }

    override fun commentClick(postId: String) {
        //TODO("Not yet implemented")
    }

    override fun tagClick(tag: String) {
        //TODO("Not yet implemented")
    }

    override fun mentionClick(mention: String) {
        //TODO("Not yet implemented")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Inflate the layout for this fragment
        super.onViewCreated(view, savedInstanceState)
        setInit()
    }

    private fun setInit() {
        mMainActivity.enableLayoutBehaviour()
    }

}