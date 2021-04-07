package com.part.livetaskcore

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.part.livetaskcore.ErrorObserver.timeOut
import com.part.livetaskcore.ErrorObserver.watch
import com.part.livetaskcore.ErrorObserver.watchAll

/**
 * You have access to the cause of every failed LiveTask via this Object. [watchAll] method
 * gets triggered every time a LiveTask fails and has the exception that has been thrown through
 * [ErrorEvent]. The only difference between [watchAll] and [watch] is that [watch] has a [timeOut]
 * factor and in that duration, [ErrorEvent]s that are returned are distinct. The default value
 * of [timeOut] is set to 5000 mills.
 */
object ErrorObserver : ErrorObserverCallback {

    private val errorEvent = MutableLiveData<ErrorEvent>()
    private val errorEventDistinct = MutableLiveData<ErrorEvent>()
    private var lastShownErrorTime = 1L
    private var timeOut = 5000L
    private var lastErrorEvent = Exception("")

    override fun notifyError(errorEvent: ErrorEvent) {
        ErrorObserver.errorEvent.postValue(errorEvent)
        if (lastShownErrorTime <= System.currentTimeMillis() - timeOut || errorEvent.exception.javaClass != lastErrorEvent.javaClass) {
            errorEventDistinct.postValue(errorEvent)
            lastErrorEvent = errorEvent.exception
            lastShownErrorTime = System.currentTimeMillis()
        }
    }

    fun watchAll(owner: LifecycleOwner, observer: Observer<ErrorEvent>) =
        errorEvent.observe(owner, observer)

    fun watch(
        owner: LifecycleOwner,
        mills: Long = timeOut,
        observer: Observer<ErrorEvent>
    ) {
        if (mills in 0..60000) timeOut = mills
        return errorEventDistinct.observe(owner, observer)
    }
}