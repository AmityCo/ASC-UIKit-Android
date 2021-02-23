package com.ekoapp.ekosdk.uikit.community.home.fragments

import androidx.paging.PagedList
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.EkoCommunityRepository
import com.ekoapp.ekosdk.community.EkoCommunity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.reactivex.Flowable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class EkoCommunityHomeViewModelTest {

    @Test
    fun initTest() {
        val viewModel = EkoCommunityHomeViewModel()
        assertFalse(viewModel.isSearchMode.get())
        assertFalse(viewModel.emptySearch.get())
    }

    @Test
    fun when_searchCommunity_expect_FlowablePagedListEkoCommunity() {
        val mockList: PagedList<EkoCommunity> = mockk()
        every { mockList.size } returns 5
        mockkStatic(EkoClient::class)
        val communityRepository: EkoCommunityRepository = mockk()
        every { EkoClient.newCommunityRepository() } returns communityRepository
        every {
            communityRepository.getCommunityCollection().withKeyword(any())
                    .sortBy(any()).includeDeleted(false)
                    .build()
                    .query()
        } returns Flowable.just(mockList)

        val viewModel = EkoCommunityHomeViewModel()
        val res = viewModel.searchCommunity("abc").blockingFirst()
        assertEquals(res.size, 5)
    }
}