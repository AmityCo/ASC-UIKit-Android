package com.ekoapp.ekosdk.uikit.community.home.fragments

import androidx.databinding.ObservableBoolean
import androidx.paging.PagedList
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.community.query.EkoCommunitySortOption
import com.ekoapp.ekosdk.uikit.base.EkoBaseViewModel
import com.ekoapp.ekosdk.uikit.community.home.listener.IExploreFragmentFragmentDelegate
import com.ekoapp.ekosdk.uikit.community.home.listener.INewsFeedFragmentDelegate
import io.reactivex.Flowable

class EkoCommunityHomeViewModel : EkoBaseViewModel() {
    var exploreFragmentDelegate: IExploreFragmentFragmentDelegate? = null
    var newsFeedFragmentDelegate: INewsFeedFragmentDelegate? = null
    var isSearchMode = ObservableBoolean(false)
    val emptySearch = ObservableBoolean(false)
    val emptySearchString = ObservableBoolean(true)

    fun searchCommunity(searchString: String): Flowable<PagedList<EkoCommunity>> {
        val communityRepository = EkoClient.newCommunityRepository()
        return communityRepository.getCommunityCollection()
                .withKeyword(searchString)
                .sortBy(EkoCommunitySortOption.DISPLAY_NAME)
                .includeDeleted(false)
                .build()
                .query()
    }
}