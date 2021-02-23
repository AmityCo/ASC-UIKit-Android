package com.ekoapp.ekosdk.uikit.community.newsfeed.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.ekoapp.ekosdk.uikit.base.EkoBaseFragment
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityFragmentNewsFeedBinding
import com.ekoapp.ekosdk.uikit.community.home.fragments.EkoCommunityHomeViewModel
import com.ekoapp.ekosdk.uikit.community.home.listener.IGlobalFeedFragmentDelegate
import com.ekoapp.ekosdk.uikit.community.home.listener.IMyCommunityListPreviewFragmentDelegate
import com.ekoapp.ekosdk.uikit.community.mycommunity.fragment.EkoMyCommunityPreviewFragment
import com.ekoapp.ekosdk.uikit.community.newsfeed.listener.ICreatePostButtonClickListener
import com.ekoapp.ekosdk.uikit.community.newsfeed.viewmodel.EkoNewsFeedViewModel
import com.ekoapp.ekosdk.uikit.community.ui.view.EkoCommunityCreateActivity
import com.ekoapp.ekosdk.uikit.community.utils.EkoCommunityNavigation
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.amity_fragment_news_feed.*

class EkoNewsFeedFragment internal constructor() : EkoBaseFragment(),
        AppBarLayout.OnOffsetChangedListener {

    private lateinit var mBinding: AmityFragmentNewsFeedBinding
    private lateinit var mViewModel: EkoNewsFeedViewModel
    private val communityHomeViewModel: EkoCommunityHomeViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mViewModel = ViewModelProvider(requireActivity()).get(EkoNewsFeedViewModel::class.java)
        mBinding = DataBindingUtil.inflate(inflater, R.layout.amity_fragment_news_feed, container, false)
        mBinding.viewModel = mViewModel
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.myCommunityContainer, getMyCommunityPreviewFragment())
        fragmentTransaction.replace(R.id.globalFeedContainer, getGlobalFeed())
        fragmentTransaction.commit()

        addViewModelListener()

        tvCreateCommunity.setOnClickListener {
            val intent = Intent(requireContext(), EkoCommunityCreateActivity::class.java)
            startActivity(intent)
        }

        btnExplore.setOnClickListener {
            communityHomeViewModel.triggerEvent(EventIdentifier.EXPLORE_COMMUNITY)
        }

        fabCreatePost.setOnClickListener {
            activity?.let {
                if (mViewModel.createPostButtonClickListener != null)
                    mViewModel.createPostButtonClickListener!!.onClickCreatePost()
                else
                    EkoCommunityNavigation.navigateToCreatePostRoleSelection(it)
            }

        }

        refreshLayout.setColorSchemeResources(R.color.amityColorPrimary)
        refreshLayout.setOnRefreshListener {
            childFragmentManager.fragments.forEach { fragment ->
                when (fragment) {
                    is EkoGlobalFeedFragment -> {
                        fragment.refresh()
                    }
                    is EkoMyCommunityPreviewFragment -> {
                        fragment.refresh()
                    }
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                refreshLayout?.isRefreshing = false
            }, 1000)
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        refreshLayout.isEnabled = (verticalOffset == 0)
    }

    override fun onResume() {
        super.onResume()
        appBar.addOnOffsetChangedListener(this)
    }

    override fun onPause() {
        super.onPause()
        appBar.removeOnOffsetChangedListener(this)
    }

    private fun getMyCommunityPreviewFragment(): Fragment {
        if (mViewModel.myCommunityListPreviewFragmentDelegate != null)
            return mViewModel.myCommunityListPreviewFragmentDelegate!!.getMyCommunityListPreviewFragment()

        return EkoMyCommunityPreviewFragment.Builder().build(activity as AppCompatActivity)
    }

    private fun getGlobalFeed(): Fragment {
        if (mViewModel.globalFeedFragmentDelegate != null)
            return mViewModel.globalFeedFragmentDelegate!!.getGlobalFeedFragment()

        return EkoGlobalFeedFragment.Builder()
                .build(activity as AppCompatActivity)
    }

    private fun addViewModelListener() {
        mViewModel.onEventReceived += { event ->
            when (event.type) {
                EventIdentifier.EMPTY_GLOBAL_FEED -> mViewModel.emptyGlobalFeed.set(event.dataObj as Boolean)
                EventIdentifier.EMPTY_MY_COMMUNITY -> mViewModel.emptyCommunityList.set(event.dataObj as Boolean)
                else -> {
                }
            }
        }
    }

    class Builder {
        private var myCommunityListPreviewFragmentDelegate: IMyCommunityListPreviewFragmentDelegate? =
                null
        private var globalFeedFragmentDelegate: IGlobalFeedFragmentDelegate? = null
        private var createPostButtonClickListener: ICreatePostButtonClickListener? = null

        fun build(activity: AppCompatActivity): EkoNewsFeedFragment {
            val fragment = EkoNewsFeedFragment()
            fragment.mViewModel =
                    ViewModelProvider(activity).get(EkoNewsFeedViewModel::class.java)
            fragment.mViewModel.myCommunityListPreviewFragmentDelegate =
                    myCommunityListPreviewFragmentDelegate
            fragment.mViewModel.globalFeedFragmentDelegate = globalFeedFragmentDelegate
            fragment.mViewModel.createPostButtonClickListener = createPostButtonClickListener
            return fragment
        }

        private fun createPostButtonClickListener(listener: ICreatePostButtonClickListener): Builder {
            this.createPostButtonClickListener = listener
            return this
        }

        private fun myCommunityPreviewFragmentDelegate(delegate: IMyCommunityListPreviewFragmentDelegate): Builder {
            this.myCommunityListPreviewFragmentDelegate = delegate
            return this
        }

        fun globalFeedFragmentDelegate(delegate: IGlobalFeedFragmentDelegate): Builder {
            this.globalFeedFragmentDelegate = delegate
            return this
        }

    }
}