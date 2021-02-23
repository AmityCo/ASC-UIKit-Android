package com.ekoapp.ekosdk.uikit.community.newsfeed.adapter

import android.view.View
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.comment.EkoComment
import com.ekoapp.ekosdk.comment.EkoCommentReference
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.uikit.base.EkoBaseRecyclerViewPagedAdapter
import com.ekoapp.ekosdk.uikit.common.views.text.EkoExpandableTextView
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.*
import com.ekoapp.ekosdk.uikit.community.newsfeed.util.EkoTimelineType
import com.ekoapp.ekosdk.uikit.community.views.newsfeed.EkoNewsFeedItemFooter
import com.ekoapp.ekosdk.uikit.community.views.newsfeed.EkoNewsFeedItemHeader
import com.ekoapp.ekosdk.user.EkoUser

open class NewsFeedViewHolder(
        itemView: View,
        private val itemActionLister: INewsFeedItemActionListener,
        private val timelineType: EkoTimelineType
) : RecyclerView.ViewHolder(itemView),
        EkoBaseRecyclerViewPagedAdapter.Binder<EkoPost> {
    val feed = itemView.findViewById<EkoExpandableTextView>(R.id.tvFeed)
    private val feedAction = itemView.findViewById<ImageButton>(R.id.btnFeedAction)
    private var headerLayout = itemView.findViewById<EkoNewsFeedItemHeader>(R.id.newsFeedHeader)
    private var footerLayout = itemView.findViewById<EkoNewsFeedItemFooter>(R.id.newsFeedFooter)

    override fun bind(data: EkoPost?, position: Int) {
        if (data != null) {
            //TODO data binding
            headerLayout.setFeed(data, timelineType)
            footerLayout.setFeed(data)

            headerLayout.setNewsFeedActionAvatarClickListener(object :
                    INewsFeedActionAvatarClickListener {
                override fun onClickUserAvatar(user: EkoUser) {
                    itemActionLister.onClickUserAvatar(data, user, position)
                }

            })

            headerLayout.setNewsFeedActionCommunityClickListener(object :
                    INewsFeedActionCommunityClickListener {

                override fun onClickCommunity(community: EkoCommunity) {
                    itemActionLister.onClickCommunity(community)
                }

            })

            this.footerLayout.setFeedLikeActionListener(object : INewsFeedActionLikeListener {
                override fun onLikeAction(liked: Boolean) {
                    itemActionLister.onLikeAction(liked, data, position)
                }

            })

            this.footerLayout.setFeedShareActionListener(object : INewsFeedActionShareListener {
                override fun onShareAction() {
                    itemActionLister.onShareAction(data, position)
                }
            })

            this.footerLayout.setItemClickListener(object : INewsFeedCommentItemClickListener {
                override fun onClickItem(comment: EkoComment, position: Int) {
                    val postId =
                            (comment.getReference() as? EkoCommentReference.Post)?.getPostId() ?: ""
                    itemActionLister.onClickItem(postId, position)
                }

                override fun onClickAvatar(user: EkoUser) {
                    itemActionLister.onClickUserAvatar(data, user, position)
                }
            })

            this.footerLayout.setShowAllReplyListener(object :
                    INewsFeedCommentShowAllReplyListener {
                override fun onClickShowAllReplies(comment: EkoComment, position: Int) {
                    itemActionLister.showAllReply(data, comment, position)
                }

            })

            this.footerLayout.setShowMoreActionListener(object :
                    INewsFeedCommentShowMoreActionListener {
                override fun onClickNewsFeedCommentShowMoreAction(
                        comment: EkoComment,
                        commentPosition: Int
                ) {
                    itemActionLister.onCommentAction(data, comment, commentPosition)
                }

            })

            this.feed.text = (data.getData() as? EkoPost.Data.TEXT)?.getText() ?: ""
            if (this.feed.tag != this.feed.getVisibleLineCount()) {
                this.feed.forceLayout()
                this.feed.tag = this.feed.getVisibleLineCount()
            }

            this.feed.visibility = if (this.feed.text.isEmpty()) View.GONE else View.VISIBLE


            footerLayout.submitComments(
                    data
                            .getLatestComments()
                            .filter {
                                !it.isDeleted() && it.getState() != EkoComment.State.FAILED
                            }
                            .takeLast(2)
            )
            feed.setExpandOnlyOnReadMoreClick(true)

            feed.setOnClickListener {
                if (feed.isReadMoreClicked()) {
                    feed.showCompleteText()
                    this.feed.tag = this.feed.getVisibleLineCount()
                } else {
                    itemActionLister.onClickItem(data.getPostId(), position)
                }

            }
            footerLayout.setOnClickListener {
                itemActionLister.onClickItem(data.getPostId(), position)
            }

            feedAction.setOnClickListener {
                itemActionLister.onFeedAction(data, position)
            }
        }

    }

}