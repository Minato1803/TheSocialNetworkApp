package com.datn.thesocialnetwork.feature.post.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.data.repository.model.post.StateData
import com.datn.thesocialnetwork.data.repository.model.post.setState
import com.datn.thesocialnetwork.data.repository.model.post.setupView
import com.datn.thesocialnetwork.databinding.FragmentCreatePostBinding
import com.datn.thesocialnetwork.databinding.StateRecyclerBinding
import com.datn.thesocialnetwork.feature.post.adapter.PostAdapter
import com.datn.thesocialnetwork.feature.post.viewmodel.ViewModelStateRcv
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest

@ExperimentalCoroutinesApi
class StateRecycler : Fragment(R.layout.state_recycler) {
    private lateinit var viewModel: ViewModelStateRcv

    private var postAdapter: PostAdapter? = null
    private lateinit var stateData: StateData

    private var _bd: StateRecyclerBinding? = null
    lateinit var binding: StateRecyclerBinding

    fun initView(
        viewModel: ViewModelStateRcv,
        postAdapter: PostAdapter,
        stateData: StateData,
    ) {
        this.viewModel = viewModel
        this.postAdapter = postAdapter
        this.stateData = stateData
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _bd = StateRecyclerBinding.bind(view)
        binding = _bd!!

        if (postAdapter != null) {
            binding.setupView(stateData, viewModel.tryAgain)
            setupRecycler()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        postAdapter?.cancelScopes()
    }

    private fun setupRecycler() {
        postAdapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
        binding.rvPosts.layoutManager = LinearLayoutManager(binding.root.context)
        binding.rvPosts.adapter = postAdapter

        lifecycleScope.launchWhenStarted {
            viewModel.postToDisplay.collectLatest {
                binding.setState(it, postAdapter)
            }
        }
    }
}