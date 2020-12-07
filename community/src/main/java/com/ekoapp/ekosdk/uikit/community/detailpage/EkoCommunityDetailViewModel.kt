package com.ekoapp.ekosdk.uikit.community.detailpage

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.file.EkoImage
import com.ekoapp.ekosdk.uikit.base.EkoBaseViewModel
import com.ekoapp.ekosdk.uikit.common.formatCount
import com.ekoapp.ekosdk.uikit.community.detailpage.listener.IEditCommunityProfileClickListener
import com.ekoapp.ekosdk.uikit.community.detailpage.listener.IMessageClickListener
import com.ekoapp.ekosdk.uikit.community.profile.listener.IFeedFragmentDelegate
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import io.reactivex.Completable
import io.reactivex.Flowable

class EkoCommunityDetailViewModel : EkoBaseViewModel() {

    var communityID = ""
    var ekoCommunity: EkoCommunity? = null
    val avatarUrl = ObservableField("")
    val name = ObservableField("")
    val category = ObservableField("")
    val posts = ObservableField("0")
    val members = ObservableField("0")
    val description = ObservableField("")
    val isPublic = ObservableBoolean(true)
    val isMember = ObservableBoolean(true)
    val isOfficial = ObservableBoolean(false)
    val isCreator = ObservableBoolean(false)
    val isModerator = ObservableBoolean(false)
    var feedFragmentDelegate: IFeedFragmentDelegate? = null
    var messageClickListener: IMessageClickListener? = null
    var editCommunityProfileClickListener: IEditCommunityProfileClickListener? = null

    fun getCommunityDetail(): Flowable<EkoCommunity> {
        val communityRepository = EkoClient.newCommunityRepository()
        return communityRepository.getCommunity(communityID)
    }

    fun setCommunity(ekoCommunity: EkoCommunity) {
        this.ekoCommunity = ekoCommunity
        name.set(ekoCommunity.getDisplayName())
        category.set(ekoCommunity.getCategories().joinToString(separator = " ") { it.getName() })
        avatarUrl.set(ekoCommunity.getAvatar()?.getUrl(EkoImage.Size.LARGE) ?: "")
        posts.set(ekoCommunity.getPostCount().toDouble().formatCount())
        members.set(ekoCommunity.getMemberCount().toDouble().formatCount())
        description.set(ekoCommunity.getDescription())
        isPublic.set(ekoCommunity.isPublic())
        isMember.set(ekoCommunity.isJoined())
        isOfficial.set(ekoCommunity.isOfficial())
        isCreator.set(ekoCommunity.getUserId() == EkoClient.getUserId())
    }

    fun joinCommunity(): Completable {
        val communityRepository = EkoClient.newCommunityRepository()
        return communityRepository.joinCommunity(communityID)
    }

    fun onPrimaryButtonClick() {
        if (isCreator.get()) {
            triggerEvent(EventIdentifier.EDIT_PROFILE)
        }
        /*else if (isModerator.get()) { //TODO Uncomment when SDK already support role feature.
            triggerEvent(EventIdentifier.MODERATOR_MESSAGE)
        }*/
        else {
            triggerEvent(EventIdentifier.SEND_MESSAGE)
        }


    }

    fun onSecondaryButtonClick() {
        triggerEvent(EventIdentifier.SEND_MESSAGE)
    }
}