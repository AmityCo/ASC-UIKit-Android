package com.ekoapp.ekosdk.uikit.community.profile.activity

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ekoapp.ekosdk.uikit.base.EkoBaseToolbarFragmentContainerActivity
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.profile.fragment.EkoUserProfilePageFragment

class EkoUserProfileActivity : EkoBaseToolbarFragmentContainerActivity() {

    override fun initToolbar() {
        getToolBar()?.setLeftDrawable(
                ContextCompat.getDrawable(
                        this,
                        R.drawable.amity_ic_arrow_back
                )
        )
    }

    override fun getContentFragment(): Fragment {
        val userId = intent.extras!!.getString(EXTRA_PARAM_USER_ID)!!
        return EkoUserProfilePageFragment.Builder().userId(userId).build(this)
    }

    companion object {
        private const val EXTRA_PARAM_USER_ID =
                "com.ekoapp.ekosdk.uikit.community.profile.activity.userid"

        fun newIntent(context: Context, id: String) =
                Intent(context, EkoUserProfileActivity::class.java).apply {
                    putExtra(EXTRA_PARAM_USER_ID, id)
                }
    }
}