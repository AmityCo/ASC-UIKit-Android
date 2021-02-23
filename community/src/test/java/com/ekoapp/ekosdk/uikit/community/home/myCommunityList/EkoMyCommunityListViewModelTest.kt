package com.ekoapp.ekosdk.uikit.community.home.myCommunityList

import androidx.paging.PagedList
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.EkoCommunityRepository
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.community.query.EkoCommunityFilter
import com.ekoapp.ekosdk.community.query.EkoCommunitySortOption
import com.ekoapp.ekosdk.uikit.community.mycommunity.viewmodel.EkoMyCommunityListViewModel
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.reactivex.Flowable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EkoMyCommunityListViewModelTest {

    @Test
    fun when_getCommunityList_expect_FlowablePagedListEkoCommunity() {
        val mockList: PagedList<EkoCommunity> = mockk()
        every { mockList.size } returns 5
        mockkStatic(EkoClient::class)
        val communityRepository: EkoCommunityRepository = mockk()
        every { EkoClient.newCommunityRepository() } returns communityRepository
        every {
            communityRepository.getCommunityCollection().withKeyword(any())
                    .filter(EkoCommunityFilter.MEMBER).sortBy(EkoCommunitySortOption.DISPLAY_NAME)
                    .includeDeleted(false)
                    .build().query()
        } returns Flowable.just(mockList)

        val viewModel = EkoMyCommunityListViewModel()
        val res = viewModel.getCommunityList().blockingFirst()
        assertEquals(res.size, 5)
    }

    @Test
    fun when_searchString_change_expect_event_SEARCH_STRING_CHANGED() {
        var searchStringChanged = false
        val viewModel = EkoMyCommunityListViewModel()
        viewModel.onEventReceived += { event ->
            when (event.type) {
                EventIdentifier.SEARCH_STRING_CHANGED -> searchStringChanged = true
                else -> {
                }
            }
        }
        viewModel.setPropertyChangeCallback()
        viewModel.searchString.set("123")
        assertTrue(searchStringChanged)
    }
}