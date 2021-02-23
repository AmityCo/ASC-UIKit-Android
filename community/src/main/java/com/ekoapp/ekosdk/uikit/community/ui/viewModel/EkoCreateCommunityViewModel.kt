package com.ekoapp.ekosdk.uikit.community.ui.viewModel

import android.net.Uri
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableParcelable
import androidx.lifecycle.MutableLiveData
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.EkoCommunityRepository
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.file.upload.EkoUploadResult
import com.ekoapp.ekosdk.uikit.base.EkoBaseViewModel
import com.ekoapp.ekosdk.uikit.community.data.SelectCategoryItem
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem
import io.reactivex.Flowable
import io.reactivex.Single

class EkoCreateCommunityViewModel : EkoBaseViewModel() {

    val initialStateChanged = MutableLiveData<Boolean>(false)
    private var initialCommunityName: String = ""
    private var initialCommunityDescription = ""
    private var initialIsPublic = true
    var initialCategory = ""
    val avatarUrl = ObservableField("")
    val communityId = ObservableField<String>("")
    val communityName = ObservableField<String>(initialCommunityName)
    val description = ObservableField("")
    val isPublic = ObservableBoolean(true)
    val isAdmin = ObservableBoolean(false)
    val addMemberVisible = ObservableBoolean(true)
    val category = ObservableParcelable<SelectCategoryItem>(SelectCategoryItem())
    val nameError = ObservableBoolean(false)
    val selectedMembersList = ArrayList<SelectMemberItem>()
    private val userIdList = ArrayList<String>()
    var ekoImage: EkoImage? = null

    fun changePostType(value: Boolean) {
        isPublic.set(value)
        initialStateChanged.value = isPublic.get() != initialIsPublic
    }

    fun changeAdminPost() {
        isAdmin.set(!isAdmin.get())
    }

    fun uploadProfilePicture(uri: Uri): Flowable<EkoUploadResult<EkoImage>> {
        val fileRepository = EkoClient.newFileRepository()
        return fileRepository.uploadImage(uri).isFullImage(true).build().transfer()
    }

    fun setCategory(category: SelectCategoryItem) {
        this.category.set(category)
        initialStateChanged.value = category.name != initialCategory
    }

    fun createCommunity(): Single<EkoCommunity> {
        resetError()
        val communityRepository: EkoCommunityRepository = EkoClient.newCommunityRepository()
        return if (isAdmin.get()) {
            val builder = communityRepository.createCommunityByAdmin(communityName.get()!!.trim())
            if (ekoImage != null) {
                builder.avatar(ekoImage!!)
            }
            if (category.get()?.categoryId?.isNotEmpty() == true) {
                builder.categoryIds(listOf(category.get()?.categoryId!!))
            }
            builder.isPublic(isPublic.get())
                    .description(description.get()?.trim() ?: "")
                    .userIds(userIdList)
                    .build()
                    .create()
        } else {
            val builder = communityRepository.createCommunity(communityName.get()!!.trim())
            if (ekoImage != null) {
                builder.avatar(ekoImage!!)
            }
            if (category.get()?.categoryId?.isNotEmpty() == true) {
                builder.categoryIds(listOf(category.get()?.categoryId!!))
            }
            builder.isPublic(isPublic.get())
                    .description(description.get()?.trim() ?: "")
                    .userIds(userIdList)
                    .build()
                    .create()
        }
    }

    fun editCommunity(): Single<EkoCommunity> {
        val communityRepository: EkoCommunityRepository = EkoClient.newCommunityRepository()
        val builder = communityRepository.updateCommunity(communityId.get()!!)
        if (ekoImage != null) {
            builder.avatar(ekoImage!!)
        }
        if (category.get()?.categoryId?.isNotEmpty() == true) {
            builder.categoryIds(listOf(category.get()?.categoryId!!))
        }
        return builder.displayName(communityName.get()!!.trim())
                .isPublic(isPublic.get())
                .description(description.get()?.trim() ?: "")
                .build().update()
    }

    fun getCommunityDetail(): Flowable<EkoCommunity> {
        val communityRepository: EkoCommunityRepository = EkoClient.newCommunityRepository()
        return communityRepository.getCommunity(communityId.get()!!)
    }

    fun setCommunityDetails(ekoCommunity: EkoCommunity) {
        initialCommunityName = ekoCommunity.getDisplayName()
        initialCommunityDescription = ekoCommunity.getDescription()
        initialIsPublic = ekoCommunity.isPublic()
        communityId.set(ekoCommunity.getCommunityId())
        communityName.set(ekoCommunity.getDisplayName())
        avatarUrl.set(ekoCommunity.getAvatar()?.getUrl(EkoImage.Size.LARGE) ?: "")
        description.set(ekoCommunity.getDescription())
        isPublic.set(ekoCommunity.isPublic())
        category.set(
                SelectCategoryItem(
                        name = ekoCommunity.getCategories().joinToString(separator = " ") { it.getName() })
        )
    }

    fun createIdList() {
        userIdList.clear()
        if (!isPublic.get()) {
            for (i in 0 until selectedMembersList.size - 1) {
                userIdList.add(selectedMembersList[i].id)
            }
        }
    }

    private fun resetError() {
        nameError.set(false)
    }

    fun setPropertyChangeCallback() {
        communityName.addOnPropertyChanged {
            initialStateChanged.value = communityName.get() != initialCommunityName
        }

        description.addOnPropertyChanged {
            initialStateChanged.value = description.get() != initialCommunityDescription
        }
    }
}