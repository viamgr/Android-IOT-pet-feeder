package com.viam.feeder.data.storage

import androidx.annotation.WorkerThread
import org.json.JSONObject
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Singleton
class ConfigStorage @Inject constructor() {

    private var configObject: JSONObject? = null

    var soundVolume by ReadWriteConfig(configObject, "soundVolume", 3.99F)

    fun isConfigured(): Boolean {
        return configObject != null
    }

    fun write(input: InputStream) {
        val inputAsString = input.bufferedReader().use { it.readText() }  // defaults to UTF-8
        configObject = JSONObject(inputAsString)
    }
}


class ReadWriteConfig<T>(
    private val configObject: JSONObject?,
    private val name: String,
    private val defaultValue: T
) : ReadWriteProperty<Any, T> {

    @Suppress("UNCHECKED_CAST")
    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val value = if (configObject != null && configObject.has(name)) {
            configObject.get(name)
        } else null
        return (value ?: defaultValue) as T
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        configObject!!.put(name, value)
    }
}
