package com.viam.feeder.data.repository

import androidx.lifecycle.LiveData
import com.viam.feeder.domain.repositories.system.JsonPreferences

@Suppress("UNCHECKED_CAST")
class LiveDataConfig<T>(
    private val configObject: JsonPreferences,
    private val name: String,
    private val defaultValue: T
) : LiveData<T>() {

    private val listener: (String, Any) -> Unit = { key, data ->
        if (key == name && value?.equals(data) == false) {
            postValue(data as T)
        }
    }

    override fun onActive() {
        super.onActive()
        configObject.addOnChangeListener(listener)
        val newValue: T? = if (configObject.hasKey(name)) {
            configObject.getByKey(name) as T
        } else defaultValue
        if (newValue?.equals(value) == false)
            value = newValue
    }

    override fun onInactive() {
        super.onInactive()
        configObject.removeOnChangeListener(listener)
    }

    fun store(value: T) {
        configObject.store(name, value as Any)
    }
}