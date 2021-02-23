package com.ekoapp.ekosdk.uikit.community.profile.activity

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ekoapp.ekosdk.uikit.base.EkoBaseToolbarFragmentContainerActivity
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.profile.fragment.EkoEditUserProfileFragment

class EkoEditUserProfileActivity : EkoBaseToolbarFragmentContainerActivity() {

    override fun initToolbar() {
        getToolBar()?.setLeftDrawable(
                ContextCompat.getDrawable(this, R.drawable.amity_ic_arrow_back)
        )
        getToolBar()?.setLeftString(getString(R.string.amity_edit_profile))
    }

    override fun getContentFragment(): Fragment {
        return EkoEditUserProfileFragment.Builder().build(this)
    }


    companion object {
        fun newIntent(context: Context) =
                Intent(context, EkoEditUserProfileActivity::class.java)
    }
}