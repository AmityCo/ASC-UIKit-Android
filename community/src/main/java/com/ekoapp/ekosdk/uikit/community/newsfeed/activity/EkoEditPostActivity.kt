package com.ekoapp.ekosdk.uikit.community.newsfeed.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.uikit.base.EkoBaseToolbarFragmentContainerActivity
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.newsfeed.fragment.EkoPostEditFragment
import com.ekoapp.ekosdk.uikit.community.utils.EXTRA_PARAM_NEWS_FEED_ID

class EkoEditPostActivity : EkoBaseToolbarFragmentContainerActivity() {

    override fun initToolbar() {
        getToolBar()?.setLeftDrawable(ContextCompat.getDrawable(this, R.drawable.amity_ic_cross))
        getToolBar()?.setLeftString(getString(R.string.amity_edit_post))
    }

    override fun getContentFragment(): Fragment {
        val postId = intent?.getStringExtra(EXTRA_PARAM_NEWS_FEED_ID)
        return EkoPostEditFragment.Builder()
                .postId(postId!!)
                .build(this)
    }

    class EkoEditPostActivityContract : ActivityResultContract<EkoPost, String?>() {
        override fun createIntent(context: Context, input: EkoPost?): Intent {
            return Intent(context, EkoEditPostActivity::class.java).apply {
                putExtra(EXTRA_PARAM_NEWS_FEED_ID, input?.getPostId())
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): String? {
            val data = intent?.getStringExtra(EXTRA_PARAM_NEWS_FEED_ID)
            return if (resultCode == Activity.RESULT_OK && data != null) data
            else null
        }
    }
}