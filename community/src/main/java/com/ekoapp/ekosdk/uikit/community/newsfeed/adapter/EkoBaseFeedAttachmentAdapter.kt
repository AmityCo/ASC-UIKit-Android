package com.ekoapp.ekosdk.uikit.community.newsfeed.adapter

import androidx.recyclerview.widget.DiffUtil
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewAdapter
import com.ekoapp.ekosdk.uikit.community.domain.model.FileAttachment

abstract class EkoBaseFeedAttachmentAdapter() : EkoBaseRecyclerViewAdapter<FileAttachment>() {


    fun submitList(newList: List<FileAttachment>) {
        setItems(newList, DiffCallback(list, newList))
    }

    class DiffCallback(
            private val oldList: List<FileAttachment>,
            private val newList: List<FileAttachment>
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].uri == newList[newItemPosition].uri
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id &&
                    oldList[oldItemPosition].name == newList[newItemPosition].name &&
                    oldList[oldItemPosition].uploadState == newList[newItemPosition].uploadState &&
                    oldList[oldItemPosition].progress == newList[newItemPosition].progress &&
                    oldList[oldItemPosition].size == newList[newItemPosition].size &&
                    oldList[oldItemPosition].uri == newList[newItemPosition].uri
        }
    }


}