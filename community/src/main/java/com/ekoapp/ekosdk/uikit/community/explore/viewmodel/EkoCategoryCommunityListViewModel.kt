package com.ekoapp.ekosdk.uikit.community.explore.viewmodel

import androidx.paging.PagedList
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.EkoCommunityRepository
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.uikit.base.EkoBaseViewModel
import com.ekoapp.ekosdk.uikit.community.explore.listener.IEkoCommunityItemClickListener
import io.reactivex.Flowable

class EkoCategoryCommunityListViewModel : EkoBaseViewModel() {

    var communityItemClickListener: IEkoCommunityItemClickListener? = null

    private val communityRepository: EkoCommunityRepository = EkoClient.newCommunityRepository()

    fun getCommunityByCategory(parentCategoryId: String): Flowable<PagedList<EkoCommunity>> {
        return communityRepository
                .getCommunityCollection()
                .categoryId(parentCategoryId)
                .includeDeleted(false)
                .build()
                .query()
    }
}