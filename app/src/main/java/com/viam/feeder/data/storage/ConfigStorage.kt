package com.viam.feeder.data.storage

import androidx.annotation.WorkerThread
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.viam.feeder.data.repository.ConfigsRepository
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.concurrent.Semaphore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


@Singleton
class ConfigObject @Inject constructor(
    private val configsRepository: ConfigsRepository,
) {
    private val semaphore = Semaphore(1)
    var json: JSONObject = JSONObject()

    fun isConfigured(): Boolean {
        return json.toString() != "{}"
    }

    suspend fun downloadOrSet() {
        try {
            semaphore.acquire()
            if (!isConfigured()) {
                downloadConfigs()
            }
        } finally {
            semaphore.release()
        }

    }

    private suspend fun downloadConfigs() {
        try {
            json = JSONObject(
                configsRepository.downloadConfigs().bufferedReader().use { it.readText() })
        } catch (e: Exception) {
            json =
                JSONObject("{\"feedingDuration\":2000,\"ledTurnOffDelay\":300000,\"ledState\":2,\"soundVolume\":3.99,\"wifiSsid\":\"V. M\",\"wifiPassword\":\"6037991302\",\"alarms\":[\"0 30 08 * * *\",\"0 30 10 * * *\"],\"accessPointName\":\"Feeder-Access-Point\",\"isAccessPointOn\":true,\"isStationOn\":true}")
        }
    }
}

@Singleton
class ConfigStorage @Inject constructor(
    private val configObject: ConfigObject,
    moshi: Moshi
) {

    var soundVolume by ReadWriteConfig(configObject, "soundVolume", 3.99F)
    var wifiSsid by ReadWriteConfig(configObject, "wifiSsid", "")
    var wifiPassword by ReadWriteConfig(configObject, "wifiPassword", "")
    var feedingDuration by ReadWriteConfig(configObject, "feedingDuration", 2000)
    var ledState by ReadWriteConfig(configObject, "ledState", 2)
    var ledTurnOffDelay by ReadWriteConfig(configObject, "ledTurnOffDelay", 60000)
    var alarms by ArrayConfig<String>(
        moshi = moshi,
        configObject = configObject,
        name = "alarms",
        defaultValue = emptyList(),
        type = String::class.java
    )

    fun isConfigured() = configObject.isConfigured()
    suspend fun downloadOrSet() = configObject.downloadOrSet()
    fun getJsonString() = configObject.json.toString()

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

class ArrayConfig<T>(
    private val moshi: Moshi,
    private val configObject: ConfigObject,
    private val name: String,
    private val defaultValue: List<T>,
    private val type: Type
) : ReadWriteProperty<Any, List<T>> {

    @Suppress("UNCHECKED_CAST")
    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): List<T> {
        return if (configObject.json.has(name)) {
            val type = Types.newParameterizedType(
                List::class.java,
                type
            )
            val adapter: JsonAdapter<List<*>> = moshi.adapter(type)
            adapter.fromJson(configObject.json.get(name).toString()) as List<T>
        } else defaultValue
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: List<T>) {
        val type = Types.newParameterizedType(
            List::class.java,
            type
        )
        val adapter: JsonAdapter<List<*>> = moshi.adapter(type)
        configObject.json.put(name, JSONArray(adapter.toJson(value)))
    }
}
