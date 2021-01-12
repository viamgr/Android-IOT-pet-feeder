package com.viam.feeder.data.storage

import androidx.annotation.WorkerThread
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Singleton
class ConfigObject @Inject constructor() {
    var json: JSONObject = JSONObject()
}

@Singleton
class ConfigStorageImpl @Inject constructor(private val configObject: ConfigObject) {

    var soundVolume by ReadWriteConfig(configObject, "soundVolume", 3.99F)

    fun isConfigured(): Boolean {
        return configObject.json.toString() != "{}"
    }

    fun setup(input: String) {
        configObject.json = JSONObject(input)
    }
}


class ReadWriteConfig<T>(
    private val configObject: ConfigObject,
    private val name: String,
    private val defaultValue: T
) : ReadWriteProperty<Any, T> {

    @Suppress("UNCHECKED_CAST")
    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val value = if (configObject.json.has(name)) {
            configObject.json.get(name)
        } else null
        return (value ?: defaultValue) as T
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        configObject.json.put(name, value)
    }
}
