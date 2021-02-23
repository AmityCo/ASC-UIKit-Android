package com.ekoapp.ekosdk.uikit.community.newsfeed.viewmodel

import androidx.paging.PagedList
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.EkoFeedRepository
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.IAvatarClickListener
import io.reactivex.Flowable
import io.reactivex.Single

class EkoCommunityTimelineViewModel : EkoBaseFeedViewModel() {
    var communityId: String? = null
    var community: EkoCommunity? = null
    var hasAdminAccess: Boolean = false
    var avatarClickListener: IAvatarClickListener? = null

    override fun getFeed(): Flowable<PagedList<EkoPost>>? {
        if (community != null) {
            val feedRepository: EkoFeedRepository = EkoClient.newFeedRepository()
            return feedRepository.getCommunityFeed(community!!.getCommunityId())
                    .includeDeleted(false)
                    .build()
                    .query()
        } else {
            return null
        }
    }

    fun canCreatePost(): Boolean {
        if (community != null) {
            return community!!.isJoined()
        }
        return false
    }

    fun getCommunity(communityId: String): Single<EkoCommunity>? {
        return EkoClient.newCommunityRepository().getCommunity(communityId).firstOrError()
    }

    fun updateAdminAccess() {
        hasAdminAccess = community?.getUserId() == EkoClient.getUserId()
    }

}