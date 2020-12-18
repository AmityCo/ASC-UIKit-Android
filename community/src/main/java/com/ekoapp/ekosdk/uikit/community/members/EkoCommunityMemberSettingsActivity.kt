package com.ekoapp.ekosdk.uikit.community.members

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.uikit.base.EkoBaseToolbarFragmentContainerActivity
import com.ekoapp.ekosdk.uikit.community.R

class EkoCommunityMemberSettingsActivity : EkoBaseToolbarFragmentContainerActivity() {

    override fun initToolbar() {
        getToolBar()?.setLeftDrawable(
            ContextCompat.getDrawable(this, R.drawable.ic_uikit_arrow_back)
        )
        getToolBar()?.setLeftString(getString(R.string.members_capital))
    }

    override fun getContentFragment(): Fragment {
        val fragment = EkoCommunityMemberSettingsFragment.Builder()
        return intent?.getParcelableExtra<EkoCommunity>(COMMUNITY_MODEL)?.let { community ->
            fragment.community(community).build(this)
        } ?: kotlin.run {
            fragment.communityId(intent?.getStringExtra(COMMUNITY_ID) ?: "")
                .isPublic(intent?.getBooleanExtra(IS_PUBLIC, true) ?: true)
                .build(this)
        }
    }

    companion object {
        private const val COMMUNITY_ID = "COMMUNITY_ID"
        private const val IS_PUBLIC = "IS_PUBLIC"
        private const val COMMUNITY_MODEL = "COMMUNITY_MODEL"

        fun newIntent(context: Context, id: String, isPublic: Boolean): Intent =
            Intent(context, EkoCommunityMemberSettingsActivity::class.java).apply {
                putExtra(COMMUNITY_ID, id)
                putExtra(IS_PUBLIC, isPublic)
            }

        fun newIntent(context: Context, community: EkoCommunity): Intent =
            Intent(context, EkoCommunityMemberSettingsActivity::class.java).apply {
                putExtra(COMMUNITY_MODEL, community)
            }
    }
}