package com.viam.feeder.core.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.viam.feeder.core.livedata.Event
import com.viam.resource.Resource
import javax.inject.Singleton

@Singleton
object TaskEventLogger : EventLogger<Resource<*>> {

    private val data = MutableLiveData<Event<Resource<*>?>>()
    val events: LiveData<Event<Resource<*>?>> = data

    override fun newEvent(event: Resource<*>?) {
        data.postValue(Event(event))
    }
}