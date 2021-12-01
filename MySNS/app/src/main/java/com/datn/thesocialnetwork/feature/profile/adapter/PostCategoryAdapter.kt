package com.datn.thesocialnetwork.feature.profile.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.data.repository.model.post.StateData
import com.datn.thesocialnetwork.feature.post.adapter.PostAdapter
import com.datn.thesocialnetwork.feature.post.viewholder.PostWithId
import com.datn.thesocialnetwork.feature.profile.viewholder.RecyclerStateViewHolder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
class PostCategoryAdapter (
    private val stateRecyclerData: List<StateRecyclerData>
) : RecyclerView.Adapter<RecyclerStateViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerStateViewHolder =
        RecyclerStateViewHolder.create(parent)

    override fun onBindViewHolder(holder: RecyclerStateViewHolder, position: Int) =
        holder.bind(stateRecyclerData[position])

    override fun getItemCount(): Int = stateRecyclerData.size

}
@ExperimentalCoroutinesApi
data class StateRecyclerData constructor(
    val postsToDisplay: Flow<GetStatus<List<PostWithId>>>,
    val postAdapter: PostAdapter,
    val stateData: StateData,
    val tryAgain: (() -> Unit)? = null
)