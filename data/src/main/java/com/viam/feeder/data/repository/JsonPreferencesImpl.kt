package com.viam.feeder.data.repository

import com.viam.feeder.domain.repositories.system.JsonPreferences
import org.json.JSONObject
import java.io.ByteArrayInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonPreferencesImpl @Inject constructor() : JsonPreferences {
    override fun hasKey(name: String) = json.has(name)
    override fun getByKey(name: String): Any = json.get(name)

    private val changeListeners = mutableListOf<(String, Any) -> Unit>()
    private var saveJson = JSONObject()
    private var json: JSONObject = JSONObject()
        set(value) {
            field = value
            saveJson = value
            triggerChanges()
        }

    override fun store(name: String, any: Any) {
        saveJson.put(name, any)
    }

    private fun triggerChanges() {
        println("triggerChanges")
        // TODO: 10/22/2021 check the key here
        json.keys().forEach { key ->
            changeListeners.forEach {
                it.invoke(key, getByKey(key))
            }
        }
    }

    override fun addOnChangeListener(listener: (String, Any) -> Unit) {
        changeListeners.add(listener)
    }

    override fun removeOnChangeListener(listener: (String, Any) -> Unit) {
        changeListeners.remove(listener)
    }

    override fun commitChanges() {
        println("commitChanges")
        json = JSONObject(saveJson.toString())
    }

    override fun getByteStream(): ByteArrayInputStream {
        val toString = saveJson.toString()
        println("getByteStream $toString")
        return toString.byteInputStream()
    }

    override fun storeJson(jsonObject: JSONObject) {
        println("storeJson :${jsonObject}")
        json = jsonObject
    }
}