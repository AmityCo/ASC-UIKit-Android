package com.ekoapp.ekosdk.uikit.base

import androidx.databinding.Observable
import androidx.lifecycle.ViewModel
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.permission.EkoPermission
import com.ekoapp.ekosdk.uikit.utils.Event
import com.ekoapp.ekosdk.uikit.model.EventIdentifier
import com.ekoapp.ekosdk.uikit.model.EventType
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Base viewModel to be extended by all viewModels of application.
 * @author sumitlakra
 * @date 06/01/2020
 */
open class EkoBaseViewModel : ViewModel() {

    protected val compositeDisposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    val onEventReceived: Event<EventType> = Event()

    fun checkModeratorPermissionAtCommunity(
            permission: EkoPermission,
            communityId: String
    ): Flowable<Boolean> {
        return EkoClient.hasPermission(permission).atCommunity(communityId)
                .check()
    }

    fun checkModeratorPermissionAtChannel(
            permission: EkoPermission,
            channelId: String
    ): Flowable<Boolean> {
        return EkoClient.hasPermission(permission).atChannel(channelId)
                .check()
    }

    fun checkPermissionAtGlobal(permission: EkoPermission): Flowable<Boolean> {
        return EkoClient.hasPermission(permission).atGlobal().check()
    }

    /**
     * Function to be used by child view models to trigger any event
     * @author sumitlakra
     * @date 06/01/2020
     */
    fun triggerEvent(type: EventIdentifier, dataObj: Any = "") {
        val eventType = EventType(type, dataObj)
        onEventReceived(eventType)
    }

    /**
     * define a property change callback which calls "callback " on change
     * @return Unit
     * @author sumitlakra
     * @date 06/01/2020
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Observable> T.addOnPropertyChanged(callback: (T) -> Unit) =
            object : Observable.OnPropertyChangedCallback() {
                @Suppress("UNCHECKED_CAST")
                override fun onPropertyChanged(observable: Observable?, i: Int) =
                        callback(observable as T)
            }.also { addOnPropertyChangedCallback(it) }

    /**
     * add disposable to [compositeDisposable] to dispose later
     * @param [disposable]
     * @author sumitlakra
     * @date 08/11/2020
     */
    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}