package com.viam.feeder.core.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.viam.feeder.core.Resource
import com.viam.feeder.core.livedata.Event
import javax.inject.Singleton

@Singleton
object TaskEventLogger : EventLogger {

    private val data = MutableLiveData<Event<Resource<*>?>>()
    val lastEvent: LiveData<Event<Resource<*>?>> = data

    override fun newEvent(resource: Resource<*>?) {
        data.postValue(Event(resource))
    }
}