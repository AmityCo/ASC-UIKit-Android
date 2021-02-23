package com.ekoapp.ekosdk.uikit.community.newsfeed.adapter

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewAdapter
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.ICreatePostImageActionListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.model.FeedImage

class EkoCreatePostImageAdapter(private val listener: ICreatePostImageActionListener) :
        EkoBaseRecyclerViewAdapter<FeedImage>(), IListItemChangeListener {

    override fun getLayoutId(position: Int, obj: FeedImage?): Int {
        return R.layout.amity_item_create_post_image
//        if(itemCount < 3)
//            return R.layout.layout_create_post_image_item
//        else
//            return R.layout.layout_create_post_image_item_multiple
    }


    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return EkoCreatePostImageViewHolder(view, listener, this)
    }

    fun submitList(newList: List<FeedImage>) {
        setItems(newList, DiffCallback(list, newList))
    }


    class DiffCallback(
            private val oldList: List<FeedImage>,
            private val newList: List<FeedImage>
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].url == newList[newItemPosition].url
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].url == newList[newItemPosition].url
                    && oldList[oldItemPosition].id == newList[newItemPosition].id
                    && oldList[oldItemPosition].uploadState == newList[newItemPosition].uploadState
                    && oldList[oldItemPosition].currentProgress == newList[newItemPosition].currentProgress
        }
    }

    override fun itemCount(): Int {
        return itemCount
    }


}

interface IListItemChangeListener {
    fun itemCount(): Int
}