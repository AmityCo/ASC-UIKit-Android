package com.ekoapp.ekosdk.uikit.community.newsfeed.adapter

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.comment.EkoComment
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewAdapter
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.INewsFeedCommentItemClickListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.INewsFeedCommentShowAllReplyListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.INewsFeedCommentShowMoreActionListener

class EkoNewsFeedCommentAdapter(
        val itemClickListener: INewsFeedCommentItemClickListener?,
        private val showAllReplyListener: INewsFeedCommentShowAllReplyListener?,
        private val showMoreActionListener: INewsFeedCommentShowMoreActionListener?,
        private val preExpandCommentId: String? = null,
        var readOnlyMode: Boolean = false
) : EkoBaseRecyclerViewAdapter<EkoComment>() {
    var subItemCount: Int? = null


    constructor(
            itemCount: Int,
            itemClickListener: INewsFeedCommentItemClickListener?,
            showAllReplyListener: INewsFeedCommentShowAllReplyListener?,
            showMoreActionListener: INewsFeedCommentShowMoreActionListener?,
            preExpandCommentId: String? = null,
            readOnlyMode: Boolean = false
    ) : this(
            itemClickListener,
            showAllReplyListener,
            showMoreActionListener,
            preExpandCommentId,
            readOnlyMode
    ) {
        this.subItemCount = itemCount
    }

    override fun getLayoutId(position: Int, obj: EkoComment?): Int {
        return if (obj?.isDeleted() == true) {
            R.layout.amity_item_deleted_news_feed_comment
        } else {
            R.layout.amity_item_news_feed_comment
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? EkoNewsFeedCommentViewHolder)?.readOnlyMode = readOnlyMode
        super.onBindViewHolder(holder, position)
    }

    override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == R.layout.amity_item_deleted_news_feed_comment)
            EkoNewsFeedCommentDeletedViewHolder(view)
        else {
            EkoNewsFeedCommentViewHolder(
                    view,
                    subItemCount,
                    itemClickListener,
                    showAllReplyListener,
                    showMoreActionListener, preExpandCommentId, readOnlyMode
            )
        }
    }

    fun submitList(newList: List<EkoComment>) {
        setItems(newList, EkoNewsFeedCommentDiffUtil(list, newList))
    }

    class EkoNewsFeedCommentDiffUtil(
            private val oldList: List<EkoComment>,
            private val newList: List<EkoComment>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].getCommentId() == newList[newItemPosition].getCommentId()
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].getCommentId() == newList[newItemPosition].getCommentId()
                    && oldList[oldItemPosition].getData() == newList[newItemPosition].getData()
                    && oldList[oldItemPosition].isDeleted() == newList[newItemPosition].isDeleted()
                    && oldList[oldItemPosition].isEdited() == newList[newItemPosition].isEdited()
                    && oldList[oldItemPosition].getChildrenNumber() == newList[newItemPosition].getChildrenNumber()
                    && oldList[oldItemPosition].getDataType() == newList[newItemPosition].getDataType()
                    && oldList[oldItemPosition].getUser() == newList[newItemPosition].getUser()
                    && oldList[oldItemPosition].getParentId() == newList[newItemPosition].getParentId()
                    && oldList[oldItemPosition].getReference() == newList[newItemPosition].getReference()
                    && oldList[oldItemPosition].getState() == newList[newItemPosition].getState()
                    && oldList[oldItemPosition].getUpdatedAt() == newList[newItemPosition].getUpdatedAt()
        }

    }
}