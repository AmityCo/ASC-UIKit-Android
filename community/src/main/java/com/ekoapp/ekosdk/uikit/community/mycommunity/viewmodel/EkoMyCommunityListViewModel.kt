package com.ekoapp.ekosdk.uikit.community.mycommunity.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.paging.PagedList
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.community.query.EkoCommunityFilter
import com.ekoapp.ekosdk.community.query.EkoCommunitySortOption
import com.ekoapp.ekosdk.uikit.base.EkoBaseViewModel
import com.ekoapp.ekosdk.uikit.community.mycommunity.listener.IMyCommunityItemClickListener
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import io.reactivex.Flowable

class EkoMyCommunityListViewModel : EkoBaseViewModel() {

    var myCommunityItemClickListener: IMyCommunityItemClickListener? = null
    val searchString = ObservableField("")
    val emptyCommunity = ObservableBoolean(false)

    fun getCommunityList(): Flowable<PagedList<EkoCommunity>> {
        val communityRepository = EkoClient.newCommunityRepository()
        return communityRepository.getCommunityCollection()
                .withKeyword(searchString.get() ?: "")
                .filter(EkoCommunityFilter.MEMBER).sortBy(EkoCommunitySortOption.DISPLAY_NAME)
                .includeDeleted(false)
                .build().query()
    }

    fun setPropertyChangeCallback() {
        searchString.addOnPropertyChanged {
            triggerEvent(EventIdentifier.SEARCH_STRING_CHANGED)
        }
    }
}