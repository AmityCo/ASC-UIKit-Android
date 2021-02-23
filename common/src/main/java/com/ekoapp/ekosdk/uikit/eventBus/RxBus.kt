
package com.ekoapp.ekosdk.uikit.eventBus

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
@brief: RxBus will serve as event bus for communication between two decoupled components
@date: 06/01/2020
@author: slakra
 */
object RxBus {

    private val publisher = PublishSubject.create<Any>()

    /**
     * Will post message event over bus
     * @param event
     * @author sumitlakra
     * @date 06/01/2020
     */
    fun publish(event: Any) {
        publisher.onNext(event)
    }

    /**
     * Will keep list of all subscribers for a given event
     * @param eventType
     * @author sumitlakra
     * @date 06/01/2020
     */
    fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}
