package com.ekoapp.ekosdk.uikit.community.explore.viewmodel

import androidx.paging.PagedList
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.EkoCommunityRepository
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.community.category.EkoCommunityCategory
import com.ekoapp.ekosdk.community.category.query.EkoCommunityCategorySortOption
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.reactivex.Flowable
import org.junit.Assert.*
import org.junit.Test

class EkoExploreCommunityViewModelTest {

    @Test
    fun initTest() {
        val viewModel = EkoExploreCommunityViewModel()
        assertFalse(viewModel.emptyCategoryList.get())
        assertFalse(viewModel.emptyRecommendedList.get())
        assertFalse(viewModel.emptyTrendingList.get())

        viewModel.emptyCategoryList.set(true)
        viewModel.emptyRecommendedList.set(true)
        viewModel.emptyTrendingList.set(true)

        assertTrue(viewModel.emptyCategoryList.get())
        assertTrue(viewModel.emptyRecommendedList.get())
        assertTrue(viewModel.emptyTrendingList.get())
    }

    @Test
    fun when_getRecommendedCommunity_Expect_FlowablePagedListEkoCommunity() {
        val mockList: PagedList<EkoCommunity> = mockk()
        every { mockList.size } returns 5
        mockkStatic(EkoClient::class)
        val communityRepository: EkoCommunityRepository = mockk()
        every { EkoClient.newCommunityRepository() } returns communityRepository
        every { communityRepository.getRecommendedCommunities() } returns Flowable.just(mockList)

        val viewModel = EkoExploreCommunityViewModel()
        val res = viewModel.getRecommendedCommunity().blockingFirst()
        assertEquals(res.size, 5)
    }

    @Test
    fun when_getTrendingCommunity_Expect_FlowablePagedListEkoCommunity() {
        val mockList: PagedList<EkoCommunity> = mockk()
        every { mockList.size } returns 5
        mockkStatic(EkoClient::class)
        val communityRepository: EkoCommunityRepository = mockk()
        every { EkoClient.newCommunityRepository() } returns communityRepository
        every { communityRepository.getTrendingCommunities() } returns Flowable.just(mockList)

        val viewModel = EkoExploreCommunityViewModel()
        val res = viewModel.getTrendingCommunity().blockingFirst()
        assertEquals(res.size, 5)
    }

    @Test
    fun when_getCommunityCategory_Expect_FlowablePagedListEkoCommunity() {
        val mockList: PagedList<EkoCommunityCategory> = mockk()
        every { mockList.size } returns 5
        mockkStatic(EkoClient::class)
        val communityRepository: EkoCommunityRepository = mockk()
        every { EkoClient.newCommunityRepository() } returns communityRepository
        every {
            communityRepository.getAllCategories().sortBy(EkoCommunityCategorySortOption.NAME)
                    .includeDeleted(false)
                    .build().query()
        } returns Flowable.just(mockList)

        val viewModel = EkoExploreCommunityViewModel()
        val res = viewModel.getCommunityCategory().blockingFirst()
        assertEquals(res.size, 5)
    }
}