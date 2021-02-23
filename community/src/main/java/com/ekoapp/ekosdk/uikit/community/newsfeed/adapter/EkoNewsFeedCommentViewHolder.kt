package com.ekoapp.ekosdk.uikit.community.newsfeed.adapter

import android.view.View
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.comment.EkoComment
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewAdapter
import com.ekoapp.ekosdk.uikit.common.toPagedList
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.INewsFeedCommentItemClickListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.INewsFeedCommentShowAllReplyListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.INewsFeedCommentShowMoreActionListener
import com.ekoapp.ekosdk.uikit.community.views.newsfeed.EkoNewsFeedCommentView
import kotlinx.android.synthetic.main.amity_item_comment_news_feed.view.*


class EkoNewsFeedCommentViewHolder(
        itemView: View,
        private val itemCount: Int?,
        private val itemClickListener: INewsFeedCommentItemClickListener?,
        private val showAllReplyListener: INewsFeedCommentShowAllReplyListener?,
        private val showMoreActionListener: INewsFeedCommentShowMoreActionListener?,
        private val preExpandCommentId: String? = null,
        var readOnlyMode: Boolean
) : RecyclerView.ViewHolder(itemView), EkoBaseRecyclerViewAdapter.IBinder<EkoComment> {
    private var newsFeedCommentAdapter: EkoNewsFeedCommentAdapter? = null
    private val ekoNewsFeedComment: EkoNewsFeedCommentView =
            itemView.findViewById(R.id.ekoNewsFeedComment)
    private val rvReply: RecyclerView = itemView.findViewById(R.id.rvReply)

    override fun bind(data: EkoComment?, position: Int) {
        data?.let { ekoComment ->
            ekoNewsFeedComment.setComment(ekoComment)
            ekoNewsFeedComment.setOnExpandClickListener(View.OnClickListener {
                itemClickListener?.onClickItem(ekoComment, position)
            })
            if (preExpandCommentId != null && ekoComment.getCommentId() == preExpandCommentId) {
                handleShowAllReply(ekoComment)
            }
            ekoNewsFeedComment.setReadOnlyMode(readOnlyMode)
            addCommentActionListener(ekoComment, position)
            addItemClickListener(ekoComment, position)
        }
    }

    private fun addItemClickListener(comment: EkoComment, position: Int) {
        itemView.setOnClickListener {
            itemClickListener?.onClickItem(comment, position)
        }

        itemView.ivAvatar.setOnClickListener {
            onClickAvatarListener(comment)
        }

        itemView.tvUserName.setOnClickListener {
            onClickAvatarListener(comment)
        }
    }

    private fun onClickAvatarListener(comment: EkoComment) {
        comment.getUser()?.let { user ->
            itemClickListener?.onClickAvatar(user)
        }
    }

    private fun addCommentActionListener(comment: EkoComment, position: Int) {
        ekoNewsFeedComment.setCommentActionListener(object :
                EkoNewsFeedCommentView.ICommentActionListener {
            override fun showAllReplies() {
                if (showAllReplyListener == null) {
                    handleShowAllReply(comment)

                } else {
                    showAllReplyListener.onClickShowAllReplies(comment, position)
                }
            }

            override fun showMoreAction() {
                showMoreActionListener?.onClickNewsFeedCommentShowMoreAction(comment, position)
            }
        })
    }

    private fun handleShowAllReply(comment: EkoComment) {
        //TODO Pass post id for get comment
//        val disposable = EkoClient.newCommentRepository()
//            .getCommentCollection()
//            .post(comment.id)
//            .parentId(comment.getParentId())
//            .build()
//            .query()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//                showReplies(it)
//            }, {
//
//            })
    }

    private fun showReplies(data: List<EkoComment>) {
        initEkoPostCommentRecyclerview(data.size)
        setReplies(data.toPagedList(data.size))
    }

    private fun initEkoPostCommentRecyclerview(size: Int) {
        newsFeedCommentAdapter = EkoNewsFeedCommentAdapter(
                size,
                itemClickListener,
                showAllReplyListener,
                showMoreActionListener,
                null,
                readOnlyMode
        )
        rvReply.layoutManager = LinearLayoutManager(itemView.context)
        rvReply.adapter = newsFeedCommentAdapter
    }

    private fun setReplies(pagedList: PagedList<EkoComment>) {
        newsFeedCommentAdapter?.submitList(pagedList)
    }
}