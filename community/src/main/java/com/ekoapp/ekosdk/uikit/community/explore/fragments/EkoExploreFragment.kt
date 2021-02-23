package com.ekoapp.ekosdk.uikit.community.explore.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ekoapp.ekosdk.uikit.community.R
import com.ekoapp.ekosdk.uikit.community.databinding.AmityFragmentExploreBinding
import com.ekoapp.ekosdk.uikit.community.explore.listener.ICategoryPreviewFragmentDelegate
import com.ekoapp.ekosdk.uikit.community.explore.listener.IRecommendedCommunityFragmentDelegate
import com.ekoapp.ekosdk.uikit.community.explore.listener.ITrendingCommunityFragmentDelegate
import com.ekoapp.ekosdk.uikit.community.explore.viewmodel.EkoExploreCommunityViewModel
import kotlinx.android.synthetic.main.amity_fragment_news_feed.*

class EkoExploreFragment internal constructor() : Fragment() {

    lateinit var mViewModel: EkoExploreCommunityViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mViewModel =
                ViewModelProvider(requireActivity()).get(EkoExploreCommunityViewModel::class.java)
        val binding: AmityFragmentExploreBinding = DataBindingUtil.inflate(
                inflater, R.layout.amity_fragment_explore, container, false
        )
        binding.viewModel = mViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()

        if (savedInstanceState == null) {
            val fragmentManager = childFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.add(R.id.recommendedContainer, getRecommendedFragment())
            fragmentTransaction.add(R.id.trendingContainer, getTrendingFragment())
            fragmentTransaction.add(R.id.categoryContainer, getCategoryPreviewFragment())
            fragmentTransaction.commit()
            fragmentManager.executePendingTransactions()
        }
    }

    private fun initListener() {
        refreshLayout.setColorSchemeResources(R.color.amityColorPrimary)
        refreshLayout.setOnRefreshListener {
            childFragmentManager.fragments.forEach { fragment ->
                when (fragment) {
                    is EkoCategoryPreviewFragment -> {
                        fragment.refresh()
                    }
                    is EkoTrendingCommunityFragment -> {
                        fragment.refresh()
                    }
                    is EkoRecommendedCommunityFragment -> {
                        fragment.refresh()
                    }
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                refreshLayout?.isRefreshing = false
            }, 1000)
        }
    }

    private fun getCategoryPreviewFragment(): Fragment {
        if (mViewModel.categoryPreviewFragmentDelegate != null)
            return mViewModel.categoryPreviewFragmentDelegate!!.categoryPreviewCommunityFragment()

        return EkoCategoryPreviewFragment.Builder().build(activity as AppCompatActivity)
    }

    private fun getTrendingFragment(): Fragment {
        if (mViewModel.trendingFragmentDelegate != null)
            return mViewModel.trendingFragmentDelegate!!.trendingCommunityFragment()
        return EkoTrendingCommunityFragment()
    }

    private fun getRecommendedFragment(): Fragment {
        if (mViewModel.recommendedFragmentDelegate != null)
            return mViewModel.recommendedFragmentDelegate!!.recommendedCommunityFragment()
        return EkoRecommendedCommunityFragment()
    }

    class Builder {
        private var recommendedFragmentDelegate: IRecommendedCommunityFragmentDelegate? = null
        private var trendingFragmentDelegate: ITrendingCommunityFragmentDelegate? = null
        private var categoryPreviewFragmentDelegate: ICategoryPreviewFragmentDelegate? = null

        fun build(activity: AppCompatActivity): EkoExploreFragment {
            val fragment = EkoExploreFragment()
            fragment.mViewModel =
                    ViewModelProvider(activity).get(EkoExploreCommunityViewModel::class.java)
            fragment.mViewModel.recommendedFragmentDelegate = recommendedFragmentDelegate
            fragment.mViewModel.trendingFragmentDelegate = trendingFragmentDelegate
            fragment.mViewModel.categoryPreviewFragmentDelegate = categoryPreviewFragmentDelegate
            return fragment
        }

        fun recommendedCommunityFragmentDelegate(delegate: IRecommendedCommunityFragmentDelegate): Builder {
            this.recommendedFragmentDelegate = delegate
            return this
        }

        fun trendingCommunityFragmentDelegate(delegate: ITrendingCommunityFragmentDelegate): Builder {
            this.trendingFragmentDelegate = delegate
            return this
        }

        fun categoryPreviewFragmentDelegate(delegate: ICategoryPreviewFragmentDelegate): Builder {
            this.categoryPreviewFragmentDelegate = delegate
            return this
        }

    }
}