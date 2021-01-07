package com.ekoapp.ekosdk.uikit.community.views.newsfeed

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.community.membership.query.EkoCommunityMembershipFilter
import com.ekoapp.ekosdk.community.membership.query.EkoCommunityMembershipSortOption
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.feed.EkoPostTarget
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.uikit.common.readableFeedPostTime
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.LayoutNewsFeedItemHeaderBinding
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.INewsFeedActionAvatarClickListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.INewsFeedActionCommunityClickListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.util.EkoTimelineType
import com.ekoapp.ekosdk.uikit.utils.EkoConstants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_news_feed_item_header.view.*

class EkoNewsFeedItemHeader : ConstraintLayout {

    private lateinit var mBinding: LayoutNewsFeedItemHeaderBinding
    private var newsFeedActionAvatarClickListener: INewsFeedActionAvatarClickListener? = null
    private var newsFeedActionCommunityClickListener: INewsFeedActionCommunityClickListener? = null
    private var showFeedAction = true

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.layout_news_feed_item_header, this, true)
    }

    fun setNewsFeedActionAvatarClickListener(listener: INewsFeedActionAvatarClickListener) {
        newsFeedActionAvatarClickListener = listener
    }

    fun setNewsFeedActionCommunityClickListener(listener: INewsFeedActionCommunityClickListener) {
        newsFeedActionCommunityClickListener = listener
    }

    fun showFeedAction(showFeedAction: Boolean) {
        this.showFeedAction = showFeedAction
        mBinding.showFeedAction = showFeedAction && !(mBinding.readOnly ?: false)
    }

    fun setFeed(data: EkoPost, timelineType: EkoTimelineType?) {
        if (data.getPostedUser() != null) {
            userName.visibility = View.VISIBLE
            userName.text = data.getPostedUser()!!.getDisplayName()
        } else {
            userName.visibility = View.GONE
        }

        feedPostTime.text = data.getCreatedAt()!!.millis.readableFeedPostTime(context)
        mBinding.isModerator = false
        mBinding.avatarUrl = data.getPostedUser()?.getAvatar()?.getUrl(EkoImage.Size.LARGE)

        data.getPostedUser()?.getRoles()

        avatarView.setOnClickListener {
            handleUserClick(data)
        }
        userName.setOnClickListener {
            handleUserClick(data)
        }
        communityName.setOnClickListener {
            handleCommunityClick(data)
        }

        val editedVisibility = if (data.isEdited()) View.VISIBLE else View.GONE
        tvEdited.visibility = editedVisibility
        val target = data.getTarget()

        if (timelineType != EkoTimelineType.COMMUNITY && target is EkoPostTarget.COMMUNITY) {
            val community = target.getCommunity()
            community?.also {
                if (!data.getPostedUserId().isNullOrEmpty()) {
                    getCommunityModerators(it.getCommunityId(), data.getPostedUserId()!!)
                } else {
                    mBinding.isModerator = false
                }
                userName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(context, R.drawable.ic_uikit_arrow),
                    null
                )
                communityName.text = it.getDisplayName().trim()
                if (community.isOfficial()) {
                    communityName.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        ContextCompat.getDrawable(context, R.drawable.ic_uikit_verified),
                        null
                    )
                } else {
                    //TODO Handle when community isn't official
                }

                communityName.visibility = View.VISIBLE
                mBinding.readOnly = !community.isJoined()
            }
        } else if (target is EkoPostTarget.COMMUNITY) {
            val communityId = target.getCommunity()?.getCommunityId()
            if (!data.getPostedUserId().isNullOrEmpty() && !communityId.isNullOrEmpty()) {
                getCommunityModerators(communityId, data.getPostedUserId()!!)
            } else {
                mBinding.isModerator = false
            }

            val community = target.getCommunity()
            showFeedAction = community!!.isJoined()
        } else {
            mBinding.readOnly = false
            communityName.visibility = View.GONE
            userName.setCompoundDrawables(null, null, null, null)
        }

        mBinding.showFeedAction = showFeedAction && !(mBinding.readOnly ?: false)


        /*data.avatarUrl?.run {
            avatarView.setImage(this)
        }*/

        /*if (data.postedByModerator) {
            tvPostBy.visibility = View.VISIBLE
        } else {
            tvPostBy.visibility = View.GONE
        }*/

    }

    private fun getCommunityModerators(communityId: String, userIdPostCreator: String) {
        val communityRepository = EkoClient.newCommunityRepository()
        communityRepository.membership(communityId).getCollection()
            .filter(EkoCommunityMembershipFilter.MEMBER)
            .sortBy(EkoCommunityMembershipSortOption.FIRST_CREATED)
            .roles(listOf(EkoConstants.MODERATOR_ROLE))
            .build()
            .query()
            .firstOrError()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { members ->
                mBinding.isModerator = members.any { it.getUserId() == userIdPostCreator }
            }
            .doOnError {
                mBinding.isModerator = false
            }.subscribe()
    }

    private fun handleCommunityClick(data: EkoPost) {
        if (data.getTarget() is EkoPostTarget.COMMUNITY) {
            val community = (data.getTarget() as EkoPostTarget.COMMUNITY).getCommunity()!!
            newsFeedActionCommunityClickListener?.onClickCommunity(community)
        } else {
            //TODO Handle when target isn't community
        }
    }

    private fun handleUserClick(feed: EkoPost) {
        feed.getPostedUser()?.let { user ->
            newsFeedActionAvatarClickListener?.onClickUserAvatar(
                user
            )
        }
    }
}