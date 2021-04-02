package com.viam.feeder.data.storage

import androidx.lifecycle.LiveData
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class JsonPreferences @Inject constructor(
    val moshi: Moshi
) {
    private val changeListeners = mutableListOf<(String, Any) -> Unit>()
    private var saveJson = JSONObject()
    var json: JSONObject = JSONObject()
        set(value) {
            field = value
            saveJson = value
            triggerChanges()
        }

    fun store(name: String, value: Any) {
        saveJson.put(name, value)
    }

    private fun triggerChanges() {
        json.keys().forEach { key ->
            changeListeners.forEach {
                it.invoke(key, json.get(key))
            }
        }
    }

    fun addOnChangeListener(callback: (String, Any) -> Unit) {
        changeListeners.add(callback)
    }

    fun removeOnChangeListener(callback: (String, Any) -> Unit) {
        changeListeners.remove(callback)
    }

    fun resetFromTemp() {
        json = saveJson
    }
}

@Singleton
class ConfigFields @Inject constructor(
    configObject: JsonPreferences,
    moshi: Moshi
) {
    var soundVolume = LiveDataConfig(configObject, "soundVolume", 3.99F)
    var wifiSsid = LiveDataConfig(configObject, "wifiSsid", "")
    var wifiPassword = LiveDataConfig(configObject, "wifiPassword", "")
    var feedingDuration = LiveDataConfig(configObject, "feedingDuration", 2000)
    var ledState = LiveDataConfig(configObject, "ledState", 2)
    var ledTurnOffDelay = LiveDataConfig(configObject, "ledTurnOffDelay", 60000)
    var alarms = ArrayConfig<String>(
        moshi = moshi,
        configObject = configObject,
        name = "alarms",
        defaultValue = emptyList(),
        type = String::class.java
    )
}


@Suppress("UNCHECKED_CAST")
class LiveDataConfig<T>(
    private val configObject: JsonPreferences,
    private val name: String,
    private val defaultValue: T
) : LiveData<T>() {

    val listener: (String, Any) -> Unit = { key, data ->
        if (key == name && value?.equals(data) == false) {
            value = data as T
        }
    }

    override fun onActive() {
        super.onActive()
        configObject.addOnChangeListener(listener)
        value = if (configObject.json.has(name)) {
            configObject.json.get(name) as T
        } else defaultValue
    }

    override fun onInactive() {
        super.onInactive()
        configObject.removeOnChangeListener(listener)
    }

    fun store(value: T) {
        configObject.store(name, value as Any)
    }
}

class ArrayConfig<T>(
    private val moshi: Moshi,
    private val configObject: JsonPreferences,
    private val name: String,
    private val defaultValue: List<T>,
    private val type: Type
) : LiveData<List<T>>() {

    private val listener: (String, Any) -> Unit = { key, data ->
        if (key == name && value?.equals(data) == false) {
            value = parseList(data as String) as List<T>
        }
    }

    override fun onActive() {
        super.onActive()
        configObject.addOnChangeListener(listener)
        value = if (configObject.json.has(name)) {
            val jsonString = configObject.json.get(name).toString()
            parseList(jsonString)
        } else defaultValue
    }

    private fun parseList(jsonString: String): List<T>? {
        val type = Types.newParameterizedType(
            List::class.java,
            type
        )
        val adapter: JsonAdapter<List<T>> = moshi.adapter(type)
        return adapter.fromJson(jsonString) as List<T>
    }

    override fun onInactive() {
        super.onInactive()
        configObject.removeOnChangeListener(listener)
    }

    fun store(value: List<T>) {
        val type = Types.newParameterizedType(
            List::class.java,
            type
        )
        val adapter: JsonAdapter<List<*>> = moshi.adapter(type)
        configObject.store(name, JSONArray(adapter.toJson(value)).toString())
    }
}
