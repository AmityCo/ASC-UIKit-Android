package com.ekoapp.ekosdk.uikit.community.newsfeed.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.feed.EkoPost
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityViewMyTimelineFeedEmptyBinding
import com.ekoapp.ekosdk.uikit.community.databinding.AmityViewOtherUserTimelineEmptyBinding
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.IAvatarClickListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.util.EkoTimelineType
import com.ekoapp.ekosdk.uikit.community.newsfeed.viewmodel.EkoBaseFeedViewModel
import com.ekoapp.ekosdk.uikit.community.newsfeed.viewmodel.EkoCommunityTimelineViewModel
import com.ekoapp.ekosdk.uikit.community.utils.EkoCommunityNavigation
import com.ekoapp.ekosdk.uikit.feed.settings.IPostShareClickListener
import com.ekoapp.ekosdk.user.EkoUser
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

const val ARG_COMMUNITY = "community"
const val ARG_COMMUNITY_ID = "community_id"

class EkoCommunityFeedFragment : EkoBaseFeedFragment() {

    private val TAG = EkoCommunityFeedFragment::class.java.simpleName
    private lateinit var mViewModel: EkoCommunityTimelineViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel =
                ViewModelProvider(requireActivity()).get(EkoCommunityTimelineViewModel::class.java)
        arguments.let {
            mViewModel.communityId = it?.getString(ARG_COMMUNITY_ID)
            mViewModel.community = it?.getParcelable(ARG_COMMUNITY)
            if (mViewModel.community == null) {
                getCommunityDetails()
            } else {
                mViewModel.updateAdminAccess()
            }
        }
    }

    private fun getCommunityDetails() {
        val communityDisposable = mViewModel
                .getCommunity(mViewModel.communityId!!)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnSuccess {
                    mViewModel.community = it
                    mViewModel.updateAdminAccess()
                }
                ?.doOnError {
                    Log.d(TAG, it.message ?: "")
                }
                ?.subscribe()

        communityDisposable?.let(disposable::add)
    }

    override fun getFeedType(): EkoTimelineType = EkoTimelineType.COMMUNITY

    override fun getViewModel(): EkoBaseFeedViewModel {
        return mViewModel
    }

    override fun getEmptyView(): View {
        return if (mViewModel.hasAdminAccess) {
            getAdminUserEmptyFeed()
        } else {
            getOtherUserEmptyFeed()
        }
    }

    private fun getOtherUserEmptyFeed(): View {
        val inflater =
                requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mBinding: AmityViewOtherUserTimelineEmptyBinding =
                DataBindingUtil.inflate(
                        inflater,
                        R.layout.amity_view_other_user_timeline_empty,
                        getRootView(),
                        false
                )
        return mBinding.root
    }

    private fun getAdminUserEmptyFeed(): View {
        val inflater =
                context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mBinding: AmityViewMyTimelineFeedEmptyBinding =
                DataBindingUtil.inflate(
                        inflater,
                        R.layout.amity_view_my_timeline_feed_empty,
                        getRootView(),
                        false
                )
        return mBinding.root
    }

    override fun onClickUserAvatar(feed: EkoPost, user: EkoUser, position: Int) {
        if (mViewModel.avatarClickListener != null) {
            mViewModel.avatarClickListener?.onClickUserAvatar(user)
        } else {
            EkoCommunityNavigation.navigateToUserProfile(requireContext(), user.getUserId())
        }
    }

    class Builder() {
        private var communityId: String? = null
        private var community: EkoCommunity? = null
        private var avatarClickListener: IAvatarClickListener? = null
        private var postShareClickListener: IPostShareClickListener? = null

        fun build(activity: AppCompatActivity): EkoCommunityFeedFragment {
            if (communityId == null && community == null) {
                throw IllegalArgumentException("Missing either communityId or community")
            }

            val fragment = EkoCommunityFeedFragment()
            fragment.mViewModel = ViewModelProvider(activity).get(EkoCommunityTimelineViewModel::class.java)
            fragment.mViewModel.avatarClickListener = avatarClickListener

            if (postShareClickListener != null) {
                fragment.mViewModel.postShareClickListener = postShareClickListener
            }

            fragment.arguments = Bundle().apply {
                putParcelable(ARG_COMMUNITY, this@Builder.community)
                putString(ARG_COMMUNITY_ID, this@Builder.communityId)
            }
            return fragment
        }

        fun communityId(communityId: String): Builder {
            return apply { this.communityId = communityId }
        }

        fun community(community: EkoCommunity?): Builder {
            return apply { this.community = community }
        }

        fun onClickUserAvatar(onAvatarClickListener: IAvatarClickListener?): Builder {
            return apply { this.avatarClickListener = onAvatarClickListener }
        }

        fun postShareClickListener(onPostShareClickListener: IPostShareClickListener): Builder {
            return apply { this.postShareClickListener = onPostShareClickListener }
        }
    }
}