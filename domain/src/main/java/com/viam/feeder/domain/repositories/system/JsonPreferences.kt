package com.viam.feeder.domain.repositories.system

import org.json.JSONObject
import java.io.ByteArrayInputStream

interface JsonPreferences {
    fun resetFromTemp()
    fun getByteStream(): ByteArrayInputStream
    fun storeJson(jsonObject: JSONObject)
    fun addOnChangeListener(listener: (String, Any) -> Unit)
    fun hasKey(name: String): Boolean
    fun getByKey(name: String): Any
    fun removeOnChangeListener(listener: (String, Any) -> Unit)
    fun store(name: String, any: Any)
}