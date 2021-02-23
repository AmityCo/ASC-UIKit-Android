package com.ekoapp.ekosdk.uikit.community.newsfeed.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.newsfeed.util.NewsFeedEvents
import com.ekoapp.ekosdk.uikit.community.utils.EXTRA_PARAM_NEWS_FEED_ID
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.amity_activity_create_post.*

class EkoPostEditFragment internal constructor() : EkoBaseCreatePostFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.postId = arguments?.getString(EXTRA_PARAM_NEWS_FEED_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPostDetails(mViewModel.postId!!)
        //hide compose bar until sdk support update image and file
        //right now support delete image and file
        hideComposeBar()
    }

    override fun handlePostMenuItemClick() {
        updatePost()
    }

    override fun setToolBarText() {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.amity_edit_post)
    }

    override fun getPostMenuText(): String {
        return getString(R.string.amity_save_caps)
    }

    override fun isRightButtonActive(): Boolean {
        if (isEditMode() && !mViewModel.hasUpdateOnPost(etPost.text.toString().trim())) {
            return false
        } else {
            return super.isRightButtonActive()
        }
    }

    private fun updatePost() {
        updatePostMenu(false)
        val disposable = mViewModel.deleteImageOrFileInPost()
                .andThen(mViewModel.updatePostText(etPost.text.toString()))
                .doOnComplete {
                    mViewModel.getNewsFeed()?.let { handleEditPostSuccessResponse(it) }
                }.doOnError {
                    updatePostMenu(true)
                    showErrorMessage(it.message)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()

        compositeDisposable.add(disposable)
    }

    private fun handleEditPostSuccessResponse(post: EkoPost) {
        val resultIntent = Intent("postCreation")
        resultIntent.putExtra(
                EXTRA_PARAM_NEWS_FEED_ID,
                post.getPostId()
        )
        activity?.setResult(Activity.RESULT_OK, resultIntent)
        refresh()
        NewsFeedEvents.newPostCreated = false
        activity?.finish()
    }

    private fun getPostDetails(postId: String) {
        val disposable = mViewModel.getPostDetails(postId)
                .firstOrError()
                .doOnError {
                    showErrorMessage(it.message)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    setUpPostData(it)
                }, {
                    showErrorMessage(it.message)
                })
        compositeDisposable.add(disposable)
    }

    private fun setUpPostData(post: EkoPost) {
        //feed = post
        mViewModel.setNewsFeed(post)
        mViewModel.getNewsFeed()?.let {
            etPost.setText(mViewModel.postText)
        }
    }


    class Builder {

        private var postId: String? = null
        fun build(activity: AppCompatActivity): EkoPostEditFragment {
            return EkoPostEditFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_PARAM_NEWS_FEED_ID, this@Builder.postId)
                }
            }
        }

        fun postId(postId: String): Builder {
            this.postId = postId
            return this
        }

        fun post(post: EkoPost): Builder {
            this.postId = post.getPostId()
            return this
        }
    }
}
