package com.ekoapp.ekosdk.uikit.community.newsfeed.adapter

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.uikit.base.SpacesItemDecoration
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.INewsFeedImageClickListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.INewsFeedItemActionListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.IPostImageItemClickListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.util.EkoTimelineType


class NewsFeedItemImageViewHolder(
        itemView: View,
        itemActionLister: INewsFeedItemActionListener,
        private val imageClickListener: INewsFeedImageClickListener?,
        timelineType: EkoTimelineType
) : NewsFeedViewHolder(itemView, itemActionLister, timelineType), IPostImageItemClickListener {
    private val imageRecyclerView = itemView.findViewById<RecyclerView>(R.id.rvImages)
    val space = itemView.context.resources.getDimensionPixelSize(R.dimen.eight)
    val itemDecor = SpacesItemDecoration(0, 0, 0, space)
    var adapter: PostImageItemAdapter? = null

    private var mImages = listOf<EkoImage>()

    override fun bind(data: EkoPost?, position: Int) {
        super.bind(data, position)
        data?.let { ekoPost ->
            val images = mutableListOf<EkoImage>()
            if (!ekoPost.getChildren().isNullOrEmpty()) {
                ekoPost.getChildren().forEach {
                    when (val postData = it.getData()) {
                        is EkoPost.Data.IMAGE -> {
                            postData.getImage()?.let { ekoImage ->
                                images.add(ekoImage)
                            }
                        }
                    }
                }
                mImages = images
                initAdapter(mImages)
                submitImages(mImages)
            }
        }
    }

    private fun initAdapter(mImages: List<EkoImage>) {
        if (adapter == null)
            adapter = PostImageItemAdapter(this)
        if (adapter?.itemCount != mImages.size) {

            imageRecyclerView.removeItemDecoration(itemDecor)
            imageRecyclerView.addItemDecoration(itemDecor)

            val layoutManager = GridLayoutManager(itemView.context, 12)
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (adapter?.itemCount) {
                        1 -> 12 //in case single image it takes full row
                        2 -> 6 //in case two image it takes each item take half of the row
                        3 -> if (position == 0) 12 else 6
                        else -> if (position == 0) 12 else 4
                    }
                }
            }
            imageRecyclerView.layoutManager = layoutManager
            imageRecyclerView.adapter = adapter
        }

    }

    private fun submitImages(images: List<EkoImage>) {
        adapter?.submitList(images)
    }

    override fun onClickItem(position: Int) {
        imageClickListener?.onClickImage(mImages, position)
    }
}