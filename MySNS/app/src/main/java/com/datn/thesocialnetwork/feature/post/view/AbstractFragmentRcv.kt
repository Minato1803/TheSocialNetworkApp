package com.datn.thesocialnetwork.feature.post.view

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.data.repository.model.post.StateData
import com.datn.thesocialnetwork.feature.post.adapter.PostAdapter
import com.datn.thesocialnetwork.feature.post.viewmodel.ViewModelStateRcv
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
abstract class AbstractFragmentRcv(
    @LayoutRes layout: Int,
    private val stateData: StateData
) : AbstractFragmentPost(layout)
{

    abstract override val viewModel: ViewModelStateRcv

    @Inject
    lateinit var postAdapter: PostAdapter

    protected lateinit var stateRecycler: StateRecycler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        postAdapter.postClickListener = this
        stateRecycler = StateRecycler()
        stateRecycler.initView(viewModel, postAdapter, stateData)

        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentRecycler, stateRecycler).commit()
    }

}