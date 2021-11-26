package com.datn.thesocialnetwork.data.repository.model.post

import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.ViewUtils.updatePadding
import com.datn.thesocialnetwork.databinding.StateRecyclerBinding
import com.datn.thesocialnetwork.feature.post.adapter.PostAdapter
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class StateData(
    @StringRes val emptyStateText: Int,
    @DrawableRes val emptyStateIcon: Int,
    @DimenRes val bottomRecyclerPadding: Int = R.dimen._86dp
)

enum class RecyclerState
{
    LOADING,
    EMPTY,
    ERROR,
    SUCCESS
}

private fun StateRecyclerBinding.setVisibility(state: RecyclerState)
{
    proBarLoadingPosts.isVisible = state == RecyclerState.LOADING
    linLayEmptyState.isVisible = state == RecyclerState.EMPTY
    linLayErrorState.isVisible = state == RecyclerState.ERROR
    rvPosts.isVisible = state == RecyclerState.SUCCESS
}

@ExperimentalCoroutinesApi
fun StateRecyclerBinding.setState(
    status: GetStatus<List<PostWithId>>,
    postAdapter: PostAdapter?
)
{
    when (status)
    {
        GetStatus.Sleep -> Unit
        GetStatus.Loading ->
        {
            setVisibility(RecyclerState.LOADING)
        }
        is GetStatus.Failed ->
        {
            setVisibility(RecyclerState.ERROR)
            txtErrorState.text = status.message.getFormattedMessage(root.context)
        }
        is GetStatus.Success ->
        {
            if (status.data.isEmpty())
            {
                setVisibility(RecyclerState.EMPTY)
            }
            else
            {
                setVisibility(RecyclerState.SUCCESS)
            }
            postAdapter?.submitList(status.data.sortedByDescending { post ->
                post.second.updatedTime
            })
        }
    }
}

fun StateRecyclerBinding.setupView(
    stateData: StateData,
    tryAgain: (() -> Unit)? = null
)
{
    /**
     * Set button TryAgain visibility based on function in VM
     */
    butTryAgain.isVisible = tryAgain != null

    butTryAgain.setOnClickListener {
        tryAgain?.invoke()
    }

    txtEmptyState.text = root.context.getString(stateData.emptyStateText)
    imgIconEmptyState.setImageResource(stateData.emptyStateIcon)

    rvPosts.updatePadding(bottom = root.context.resources.getDimensionPixelOffset(stateData.bottomRecyclerPadding))
}