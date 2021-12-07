package com.datn.thesocialnetwork.feature.profile.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.datn.thesocialnetwork.data.repository.model.post.setState
import com.datn.thesocialnetwork.data.repository.model.post.setupView
import com.datn.thesocialnetwork.databinding.StateRecyclerBinding
import com.datn.thesocialnetwork.feature.profile.adapter.StateRecyclerData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class RecyclerStateViewHolder private constructor(
    private val binding: StateRecyclerBinding,
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun create(parent: ViewGroup): RecyclerStateViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = StateRecyclerBinding.inflate(layoutInflater, parent, false)


            return RecyclerStateViewHolder(
                binding
            )
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null

    @ExperimentalCoroutinesApi
    fun bind(
        stateRecyclerData: StateRecyclerData,
    ) {
        val linerLayoutManager = LinearLayoutManager(binding.root.context)
        binding.rvPosts.layoutManager = linerLayoutManager
        binding.rvPosts.adapter = stateRecyclerData.postAdapter

        binding.setupView(
            stateRecyclerData.stateData,
            stateRecyclerData.tryAgain
        )

        job?.cancel()

        job = scope.launch {
            stateRecyclerData.postsToDisplay.collectLatest {
                binding.setState(it, stateRecyclerData.postAdapter)
            }
        }
    }
}