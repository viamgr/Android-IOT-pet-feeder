package com.viam.feeder.data.repository

import androidx.lifecycle.LiveData
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.viam.feeder.domain.repositories.system.JsonPreferences
import org.json.JSONArray
import java.lang.reflect.Type

class ArrayConfig<T>(
    private val moshi: Moshi,
    private val configObject: JsonPreferences,
    private val name: String,
    private val defaultValue: List<T>,
    private val type: Type
) : LiveData<List<T>>() {

    private val listener: (String, Any) -> Unit = { key, data ->
        if (key == name && value?.equals(data) == false) {
            postValue(parseList(data as String))
        }
    }

    override fun onActive() {
        super.onActive()
        configObject.addOnChangeListener(listener)
        postValue(
            if (configObject.hasKey(name)) {
                val jsonString = configObject.getByKey(name).toString()
                parseList(jsonString)
            } else defaultValue
        )
    }

    private fun parseList(jsonString: String): List<T> {
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