package com.ekoapp.ekosdk.uikit.community.ui.viewModel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.uikit.base.EkoBaseViewModel
import com.ekoapp.ekosdk.uikit.community.data.SelectMemberItem
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import com.ekoapp.ekosdk.user.EkoUser
import io.reactivex.Flowable

class EkoSelectMembersViewModel : EkoBaseViewModel() {

    val searchString = ObservableField("")
    val selectedMembersList: ArrayList<SelectMemberItem> = arrayListOf()
    val selectedMemberSet: HashSet<String> = hashSetOf()
    val memberMap: HashMap<String, Int> = hashMapOf()
    val searchMemberMap: HashMap<String, Int> = hashMapOf()
    val isSearchUser = ObservableBoolean(false)
    val leftString = MutableLiveData<String>("")
    val rightStringActive = MutableLiveData<Boolean>(false)

    fun getAllUsers(): Flowable<PagedList<EkoUser>> {
        val userRepo = EkoClient.newUserRepository()
        return userRepo.searchUserByDisplayName("")
                .build().query()
    }

    fun searchUser(): Flowable<PagedList<EkoUser>> {
        val userRepo = EkoClient.newUserRepository()
        return userRepo.searchUserByDisplayName(searchString.get() ?: "")
                .build().query()
    }

    fun prepareSelectedMembersList(member: SelectMemberItem, isSelected: Boolean) {
        if (isSelected) {
            selectedMembersList.add(member)
        } else {
            selectedMembersList.remove(member)
        }
    }

    fun setPropertyChangeCallback() {
        searchString.addOnPropertyChanged {
            triggerEvent(EventIdentifier.SEARCH_STRING_CHANGED)
        }
    }
}