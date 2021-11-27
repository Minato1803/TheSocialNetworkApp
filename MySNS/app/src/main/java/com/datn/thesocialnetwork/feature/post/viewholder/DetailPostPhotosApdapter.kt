package com.datn.thesocialnetwork.feature.post.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.data.repository.model.PostsImage
import com.datn.thesocialnetwork.databinding.ItemFeedPhotoBinding
import javax.inject.Inject

class DetailPostPhotosApdapter(
    private val listImage: List<Pair<String, PostsImage>>?
): RecyclerView.Adapter<DetailPostPhotosApdapter.ViewHolder>() {

    lateinit var binding: ItemFeedPhotoBinding

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val image = binding.itemFeedPhotosImage
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): DetailPostPhotosApdapter.ViewHolder {
        val inflater  = LayoutInflater.from(parent.context)
        binding = ItemFeedPhotoBinding.inflate(inflater, parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: DetailPostPhotosApdapter.ViewHolder, position: Int) {
        Glide.with(binding.root.context)
            .load(listImage?.get(position)?.second?.imageUrl)
            .centerCrop()
            .fitCenter()
            .into(holder.image)
    }

    override fun getItemCount(): Int = listImage?.size ?: 0

}