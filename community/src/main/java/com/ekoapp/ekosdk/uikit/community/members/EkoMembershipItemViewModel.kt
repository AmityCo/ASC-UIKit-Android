package com.ekoapp.ekosdk.uikit.community.members

import androidx.databinding.ObservableBoolean
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.community.EkoCommunity
import com.ekoapp.ekosdk.uikit.base.EkoBaseViewModel
import com.ekoapp.ekosdk.user.EkoUser
import io.reactivex.Completable
import io.reactivex.Flowable

class EkoMembershipItemViewModel : EkoBaseViewModel() {

    var communityId = ""
    var isModerator = ObservableBoolean(false)

    fun reportUser(ekoUser: EkoUser): Completable {
        return ekoUser.report().flag()
    }

    fun unReportUser(ekoUser: EkoUser): Completable {
        return ekoUser.report().unflag()
    }

    fun getUser(userId: String): Flowable<EkoUser> {
        val userRepository = EkoClient.newUserRepository()
        return userRepository.getUser(userId)
    }

    fun removeUser(list: List<String>): Completable {
        val communityRepository = EkoClient.newCommunityRepository()
        return communityRepository.membership(communityId).removeUsers(list)
    }

    fun assignRole(role: String, userIdList: List<String>): Completable {
        val communityRepository = EkoClient.newCommunityRepository()
        return communityRepository.moderate(communityId)
                .addRole(role, userIdList)
    }

    fun removeRole(role: String, userIdList: List<String>): Completable {
        val communityRepository = EkoClient.newCommunityRepository()
        return communityRepository.moderate(communityId)
                .removeRole(role, userIdList)
    }

    fun getCommunityDetail(): Flowable<EkoCommunity> {
        return EkoClient.newCommunityRepository().getCommunity(communityId)
    }
}