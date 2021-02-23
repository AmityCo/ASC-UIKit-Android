package com.ekoapp.ekosdk.uikit.community.utils

import android.content.Context
import android.content.Intent
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.comment.EkoComment
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.uikit.community.detailpage.EkoCommunityPageActivity
import com.ekoapp.ekosdk.uikit.community.explore.activity.EXTRA_PARAM_COMMUNITY
import com.ekoapp.ekosdk.uikit.community.newsfeed.activity.*
import com.ekoapp.ekosdk.uikit.community.newsfeed.util.EkoTimelineType
import com.ekoapp.ekosdk.uikit.community.profile.activity.EkoEditUserProfileActivity
import com.ekoapp.ekosdk.uikit.community.profile.activity.EkoUserProfileActivity
import com.ekoapp.ekosdk.uikit.imagepreview.EkoImagePreviewActivity
import com.ekoapp.ekosdk.uikit.imagepreview.PreviewImage

const val EXTRA_PARAM_NEWS_FEED = "news_feed"
const val EXTRA_PARAM_NEWS_FEED_ID = "news_feed_id"
const val EXTRA_PARAM_COMMUNITY_ID = "channel"
const val EXTRA_PARAM_DISPLAY_NAME = "display_name"
const val EXTRA_PARAM_TIMELINE_TYPE = "timeline_type"

class EkoCommunityNavigation {
    companion object {

        fun navigateToMyTimeline(context: Context) {
            //TODO call navigate to my timeline page
            navigateToUserProfile(context, EkoClient.getUserId())
        }

        fun navigateToCreatePost(context: Context) {
            val intent = Intent(context, EkoCreatePostActivity::class.java)
            context.startActivity(intent)
        }

        fun navigateToCreatePost(context: Context, community: EkoCommunity) {
            val intent = Intent(context, EkoCreatePostActivity::class.java).apply {
                putExtra(EXTRA_PARAM_COMMUNITY, community)
            }
            context.startActivity(intent)
        }

        fun navigateToEditPost(context: Context, post: EkoPost) {
            val intent = Intent(context, EkoEditPostActivity::class.java)
            intent.putExtra(EXTRA_PARAM_NEWS_FEED_ID, post.getPostId())
            context.startActivity(intent)
        }

        fun navigateToCreatePostRoleSelection(context: Context) {
            val intent = Intent(context, EkoPostTargetSelectionActivity::class.java)
            context.startActivity(intent)
        }

        fun navigateToPostDetails(context: Context, postId: String, timelineType: EkoTimelineType) {
            var intent = Intent(context, EkoPostDetailsActivity::class.java)
            intent.putExtra(EXTRA_PARAM_NEWS_FEED_ID, postId)
            intent.putExtra(EXTRA_PARAM_TIMELINE_TYPE, timelineType)
            context.startActivity(intent)
        }

        fun navigateToPostDetails(
                context: Context,
                post: EkoPost,
                comment: EkoComment,
                timelineType: EkoTimelineType
        ) {
            var intent = Intent(context, EkoPostDetailsActivity::class.java)
            intent.putExtra(EXTRA_PARAM_NEWS_FEED_ID, post.getPostId())
            intent.putExtra(EXTRA_PARAM_COMMENT, comment)
            intent.putExtra(EXTRA_PARAM_TIMELINE_TYPE, timelineType)
            context.startActivity(intent)
        }

        fun navigateToImagePreview(context: Context, images: List<EkoImage>, position: Int) {
            val previewImages = mutableListOf<PreviewImage>()
            images.forEach {
                previewImages.add(PreviewImage(it.getUrl(EkoImage.Size.LARGE)))
            }
            val intent =
                    EkoImagePreviewActivity.newIntent(context, position, true, ArrayList(previewImages))
            context.startActivity(intent)
        }

        fun navigateToUserProfile(context: Context, userId: String) {
            val intent = EkoUserProfileActivity.newIntent(context, userId)
            context.startActivity(intent)
        }

        fun navigateToEditProfile(context: Context) {
            var intent = Intent(context, EkoEditUserProfileActivity::class.java)
            context.startActivity(intent)
        }

        fun navigateToCommunityDetails(context: Context, community: EkoCommunity) {
            val detailIntent = EkoCommunityPageActivity
                    .newIntent(context, community.getCommunityId())
            context.startActivity(detailIntent)
        }
    }
}