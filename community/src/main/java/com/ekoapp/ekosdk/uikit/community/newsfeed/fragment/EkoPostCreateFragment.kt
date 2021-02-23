package com.ekoapp.ekosdk.uikit.community.newsfeed.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.explore.activity.EXTRA_PARAM_COMMUNITY
import com.ekoapp.ekosdk.uikit.community.newsfeed.util.NewsFeedEvents
import com.ekoapp.ekosdk.uikit.community.utils.EXTRA_PARAM_COMMUNITY_ID
import com.ekoapp.ekosdk.uikit.community.utils.EXTRA_PARAM_NEWS_FEED_ID
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_fragment_post_create.*

class EkoPostCreateFragment internal constructor() : EkoBaseCreatePostFragment() {

    override fun handlePostMenuItemClick() {
        createPost()
    }

    override fun setToolBarText() {
        (activity as AppCompatActivity).supportActionBar?.title = getToolbarTitleForCreatePost()
    }

    override fun getPostMenuText(): String {
        return getString(R.string.amity_post_caps)
    }

    private fun getToolbarTitleForCreatePost(): String {
        if (mViewModel.community != null)
            return mViewModel.community!!.getDisplayName()
        return getString(R.string.amity_my_timeline)
    }

    private fun createPost() {
        if (isLoading) {
            return
        }
        isLoading = true
        updatePostMenu(false)
        val ekoPostSingle = mViewModel.createPost(etPost.text.toString())

        val disposable = ekoPostSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                    handleCreatePostSuccessResponse(it)
                }
                .doOnError {
                    updatePostMenu(true)
                    isLoading = false
                    showErrorMessage(it.message)
                }
                .subscribe()
        compositeDisposable.add(disposable)
    }

    private fun handleCreatePostSuccessResponse(post: EkoPost) {
        val resultIntent = Intent("postCreation")
        resultIntent.putExtra(
                EXTRA_PARAM_NEWS_FEED_ID,
                post.getPostId()
        )
        activity?.setResult(Activity.RESULT_OK, resultIntent)
        refresh()
        NewsFeedEvents.newPostCreated = true
        activity?.finish()
    }

    class Builder {
        private var community: EkoCommunity? = null
        private var communityId: String? = null

        fun build(activity: AppCompatActivity): EkoPostCreateFragment {
            return EkoPostCreateFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_PARAM_COMMUNITY_ID, this@Builder.communityId)
                    putParcelable(EXTRA_PARAM_COMMUNITY, this@Builder.community)
                }
            }
        }

        fun onMyFeed(): Builder {
            return this
        }

        fun onCommunityFeed(communityId: String): Builder {
            this.communityId = communityId
            return this
        }

        fun onCommunityFeed(community: EkoCommunity?): Builder {
            this.community = community
            return this
        }
    }
}