package com.ekoapp.ekosdk.uikit.community.ui.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.EkoCommunityRepository
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.community.category.EkoCommunityCategory
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.uikit.community.data.SelectCategoryItem
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.reactivex.Single
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class EkoCreateCommunityViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun initTest() {
        val viewModel = EkoCreateCommunityViewModel()
        assertEquals(viewModel.initialCategory, "")
        assertEquals(viewModel.category.get(), SelectCategoryItem())
        assertTrue(viewModel.initialStateChanged.value == false)
        assertEquals(viewModel.communityId.get(), "")
        assertEquals(viewModel.communityName.get(), "")
        assertEquals(viewModel.description.get(), "")
        assertTrue(viewModel.isPublic.get())
        assertTrue(viewModel.addMemberVisible.get())
        assertFalse(viewModel.isAdmin.get())
        assertFalse(viewModel.nameError.get())
    }

    @Test
    fun changePostTypeTest() {
        val viewModel = EkoCreateCommunityViewModel()
        viewModel.changePostType(false)
        assertFalse(viewModel.isPublic.get())
        assertTrue(viewModel.initialStateChanged.value == true)
    }

    @Test
    fun changeAdminPostTest() {
        val viewModel = EkoCreateCommunityViewModel()
        assertFalse(viewModel.isAdmin.get())
        viewModel.changeAdminPost()
        assertTrue(viewModel.isAdmin.get())
    }

    @Test
    fun when_createCommunity_expect_SingleEkoCommunity() {
        val ekoCommunity: EkoCommunity = mockk()
        every { ekoCommunity.getCommunityId() } returns "testId"
        mockkStatic(EkoClient::class)
        val communityRepository: EkoCommunityRepository = mockk()
        every { EkoClient.newCommunityRepository() } returns communityRepository
        every {
            communityRepository.createCommunity(any())
                    .isPublic(any()).description(any()).userIds(any()).build().create()
        } returns Single.just(ekoCommunity)

        val viewModel = EkoCreateCommunityViewModel()
        viewModel.isAdmin.set(false)
        val res = viewModel.createCommunity().blockingGet()
        assertEquals(res.getCommunityId(), "testId")
    }

    @Test
    fun when_editCommunity_expect_SingleEkoCommunity() {
        val ekoCommunity: EkoCommunity = mockk()
        every { ekoCommunity.getCommunityId() } returns "testId"
        mockkStatic(EkoClient::class)
        val communityRepository: EkoCommunityRepository = mockk()
        every { EkoClient.newCommunityRepository() } returns communityRepository
        every {
            communityRepository.updateCommunity(any())
                    .displayName(any()).isPublic(any()).description(any()).build().update()
        } returns Single.just(ekoCommunity)

        val viewModel = EkoCreateCommunityViewModel()
        val res = viewModel.editCommunity().blockingGet()
        assertEquals(res.getCommunityId(), "testId")
    }

    @Test
    fun setCommunityDetailsTest() {
        val displayName = "displayName"
        val description = "description"
        val ekoCommunity: EkoCommunity = mockk()
        val ekoImage: EkoImage = mockk()
        val ekoCommunityCategory: EkoCommunityCategory = mockk()
        every { ekoCommunityCategory.getName() } returns "test"
        val mockList = ArrayList<EkoCommunityCategory>()
        mockList.add(ekoCommunityCategory)
        every { ekoImage.getUrl(any()) } returns "testUrl"
        every { ekoCommunity.getDisplayName() } returns displayName
        every { ekoCommunity.getDescription() } returns description
        every { ekoCommunity.isPublic() } returns true
        every { ekoCommunity.getAvatar() } returns ekoImage
        every { ekoCommunity.getCategories() } returns mockList
        every { ekoCommunity.getCommunityId() } returns "testID"

        val viewModel = EkoCreateCommunityViewModel()
        viewModel.setCommunityDetails(ekoCommunity)
        assertEquals(viewModel.communityName.get(), displayName)
        assertEquals(viewModel.description.get(), description)
        assertEquals(viewModel.avatarUrl.get(), "testUrl")
        assertEquals(viewModel.communityId.get(), "testID")
        assertTrue(viewModel.isPublic.get())
    }

    @Test
    fun setPropertyChangeCallbackTest() {
        val viewModel = EkoCreateCommunityViewModel()
        assertTrue(viewModel.initialStateChanged.value == false)

        viewModel.setPropertyChangeCallback()
        viewModel.communityName.set("test")
        assertTrue(viewModel.initialStateChanged.value == true)
        viewModel.communityName.set("")
        assertTrue(viewModel.initialStateChanged.value == false)

        viewModel.description.set("test description")
        assertTrue(viewModel.initialStateChanged.value == true)
        viewModel.description.set("")
        assertTrue(viewModel.initialStateChanged.value == false)
    }
}