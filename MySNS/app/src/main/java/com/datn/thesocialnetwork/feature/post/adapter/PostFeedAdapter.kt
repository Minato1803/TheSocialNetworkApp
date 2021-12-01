package com.datn.thesocialnetwork.feature.post.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.databinding.ItemPostFeedBinding
import javax.inject.Inject

class PostFeedAdapter @Inject constructor(): RecyclerView.Adapter<PostFeedAdapter.ViewHolder>() {

    @Inject
    lateinit var glide: RequestManager
    var uriArr: List<Uri>? = null
     set(value) {
         field = value
         notifyDataSetChanged()
     }
    var preview: ImageView? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    lateinit var binding: ItemPostFeedBinding
    var multiple = false
    val multipleArray = arrayListOf<Uri>()
    var defaultUrl = uriArr?.get(0)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = binding.itemFeedImageView
        val multipleOrderLayout = binding.itemFeedMultipleOrder
        val orderText = binding.itemFeedOrderText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater  = LayoutInflater.from(parent.context)
        binding = ItemPostFeedBinding.inflate(inflater, parent, false)

        val lp: GridLayoutManager.LayoutParams =
            binding.root.layoutParams as GridLayoutManager.LayoutParams
        lp.width = parent.measuredWidth / 4
        lp.height = parent.measuredWidth / 4
        binding.root.layoutParams = lp

        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        glide
            .load(uriArr?.get(position))
            .into(holder.imageView)

        if (multiple) {
            multipleArray.clear()
            holder.multipleOrderLayout.visibility = View.VISIBLE

            holder.imageView.setOnClickListener {
                preview?.let { it1 ->
                    glide
                        .load(uriArr?.get(position))
                        .into(it1)
                }

                if (!multipleArray.contains(uriArr?.get(position))) {
                    holder.multipleOrderLayout.setBackgroundResource(R.drawable.feed_photo_order_selected_background)
                    uriArr?.get(position)?.let { it1 -> multipleArray.add(it1) }
                    holder.orderText.text = (multipleArray.indexOf(uriArr?.get(position)) + 1).toString()
                    defaultUrl = uriArr?.get(position)
                } else {
                    holder.multipleOrderLayout.setBackgroundResource(R.drawable.feed_photo_order_unselected_background)
                    multipleArray.remove(uriArr?.get(position))
                    holder.orderText.text = ""
                }
            }
        } else {
            multipleArray.clear()
            holder.multipleOrderLayout.visibility = View.GONE
            holder.imageView.setOnClickListener {
                if (multipleArray.size != 0) {
                    multipleArray.clear()
                }
                uriArr?.get(position)?.let { it1 -> multipleArray.add(it1) }
                preview?.let { it1 ->
                    glide
                        .load(uriArr?.get(position))
                        .into(it1)
                }
                defaultUrl = uriArr?.get(position)
            }
        }
    }

    override fun getItemCount(): Int = uriArr?.size ?: 0

}