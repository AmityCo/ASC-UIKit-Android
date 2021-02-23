package com.ekoapp.ekosdk.uikit.community.profile.viewmodel

import android.net.Uri
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.file.upload.EkoUploadResult
import com.ekoapp.ekosdk.uikit.base.EkoBaseViewModel
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import com.ekoapp.ekosdk.user.EkoUser
import io.reactivex.Flowable
import io.reactivex.Single

class EkoEditUserProfileViewModel : EkoBaseViewModel() {
    var profileImage: EkoImage? = null
    var profileUri: Uri? = null
    var updating: Boolean = false
    var user: EkoUser? = null

    var userNameMaxTextLength = 100
    var aboutMaxTextLength = 180

    val displayName = MutableLiveData<String>().apply { value = "" }
    val about = MutableLiveData<String>().apply { value = "" }
    val hasProfileUpdate = MutableLiveData<Boolean>(false)
    val mediatorLiveData = MediatorLiveData<String>().apply {
        addSource(displayName) { value ->
            setValue(value)
        }
        addSource(about) { value -> setValue(value) }
    }


    fun updateUser(): Single<EkoUser> {
        val updateUserBuilder = EkoClient.updateUser()
                .displayName(displayName.value!!)
                .description(about.value!!)

        if (!avatarImageUrl.isNullOrBlank()) {
            updateUserBuilder.avatarCustomUrl(avatarImageUrl.orEmpty())
        }
        return updateUserBuilder.build().update()
    }

    fun uploadProfilePicture(uri: Uri): Flowable<EkoUploadResult<EkoImage>> {
        updating = true
        checkProfileUpdate()
        return EkoClient.newFileRepository().uploadImage(uri).isFullImage(true).build().transfer()
    }

    fun updateImageUploadStatus(ekoImageUpload: EkoUploadResult<EkoImage>) {
        when (ekoImageUpload) {
            is EkoUploadResult.COMPLETE -> {
                profileImage = ekoImageUpload.getFile()
                triggerEvent(EventIdentifier.PROFILE_PICTURE_UPLOAD_SUCCESS)
            }
            is EkoUploadResult.ERROR, EkoUploadResult.CANCELLED -> {
                updating = false
                checkProfileUpdate()
                triggerEvent(EventIdentifier.PROFILE_PICTURE_UPLOAD_FAILED)
            }
            else -> {
            }
        }
    }

    fun getUser(): Single<EkoUser>? {
        return EkoClient.getCurrentUser()?.firstOrError()
    }


    fun updateProfileUri(profileUri: Uri?) {
        this.profileUri = profileUri
    }

    private fun hasDraft(): Boolean {
        return user != null && (displayName.value != user!!.getDisplayName() || about.value != user!!.getDescription() || profileUri != null && profileUri.toString() != getCurrentProfileUrl())
    }

    private fun getCurrentProfileUrl(): String {
        return user!!.getAvatar()
                ?.getUrl(EkoImage.Size.SMALL) ?: ""
    }

    fun checkProfileUpdate() {
        val updateAvailable = hasDraft() && !displayName.value.isNullOrEmpty() && !updating
        hasProfileUpdate.value = updateAvailable
    }

    fun errorOnUpdate() {
        updating = false
        checkProfileUpdate()
    }

}