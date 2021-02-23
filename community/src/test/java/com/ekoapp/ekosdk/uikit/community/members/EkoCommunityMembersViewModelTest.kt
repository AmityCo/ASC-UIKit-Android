package com.ekoapp.ekosdk.uikit.community.members

import androidx.paging.PagedList
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.EkoCommunityRepository
import com.ekoapp.ekosdk.community.membership.EkoCommunityMembership
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.reactivex.Flowable
import org.junit.Assert.*
import org.junit.Test

class EkoCommunityMembersViewModelTest {

    @Test
    fun initTest() {
        val viewModel = EkoCommunityMembersViewModel()
        assertEquals(viewModel.communityId, "")
        assertEquals(viewModel.searchString.get(), "")
        assertFalse(viewModel.emptyMembersList.get())
    }

    @Test
    fun when_getCommunityMembers_expect_Flowable_pagedList_EkoCommunityMemberShip() {
        val mockList: PagedList<EkoCommunityMembership> = mockk()
        every { mockList.size } returns 5
        mockkStatic(EkoClient::class)
        val communityRepository: EkoCommunityRepository = mockk()
        every { EkoClient.newCommunityRepository() } returns communityRepository
        every {
            communityRepository.membership(any()).getCollection()
                    .filter(any()).build().query()
        } returns Flowable.just(mockList)

        val viewModel = EkoCommunityMembersViewModel()
        val res = viewModel.getCommunityMembers().blockingFirst()
        assertEquals(res.size, 5)
    }

    @Test
    fun when_searchString_change_expect_event_SEARCH_STRING_CHANGED() {
        var searchStringChanged = false
        val viewModel = EkoCommunityMembersViewModel()
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