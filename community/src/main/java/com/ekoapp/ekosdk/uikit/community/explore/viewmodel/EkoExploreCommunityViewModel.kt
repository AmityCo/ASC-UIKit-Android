package com.ekoapp.ekosdk.uikit.community.explore.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.paging.PagedList
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.community.category.EkoCommunityCategory
import com.ekoapp.ekosdk.community.category.query.EkoCommunityCategorySortOption
import com.ekoapp.ekosdk.uikit.base.EkoBaseViewModel
import com.ekoapp.ekosdk.uikit.community.explore.listener.ICategoryPreviewFragmentDelegate
import com.ekoapp.ekosdk.uikit.community.explore.listener.IEkoCategoryItemClickListener
import com.ekoapp.ekosdk.uikit.community.explore.listener.IRecommendedCommunityFragmentDelegate
import com.ekoapp.ekosdk.uikit.community.explore.listener.ITrendingCommunityFragmentDelegate
import com.ekoapp.ekosdk.uikit.community.mycommunity.listener.IMyCommunityItemClickListener
import io.reactivex.Flowable

class EkoExploreCommunityViewModel : EkoBaseViewModel() {

    var categoryItemClickListener: IEkoCategoryItemClickListener? = null
    var trendingCommunityItemClickListener: IMyCommunityItemClickListener? = null
    var recommendedCommunityItemClickListener: IMyCommunityItemClickListener? = null
    var categoryPreviewFragmentDelegate: ICategoryPreviewFragmentDelegate? = null
    var trendingFragmentDelegate: ITrendingCommunityFragmentDelegate? = null
    var recommendedFragmentDelegate: IRecommendedCommunityFragmentDelegate? = null
    val emptyRecommendedList = ObservableBoolean(false)
    val emptyTrendingList = ObservableBoolean(false)
    val emptyCategoryList = ObservableBoolean(false)

    fun getRecommendedCommunity(): Flowable<PagedList<EkoCommunity>> {
        val communityRepository = EkoClient.newCommunityRepository()
        return communityRepository.getRecommendedCommunities()
    }

    fun getTrendingCommunity(): Flowable<PagedList<EkoCommunity>> {
        val communityRepository = EkoClient.newCommunityRepository()
        return communityRepository.getTrendingCommunities()
    }

    fun getCommunityCategory(): Flowable<PagedList<EkoCommunityCategory>> {
        val communityRepository = EkoClient.newCommunityRepository()
        return communityRepository.getAllCategories()
                .sortBy(EkoCommunityCategorySortOption.NAME)
                .includeDeleted(false)
                .build()
                .query()
    }
}