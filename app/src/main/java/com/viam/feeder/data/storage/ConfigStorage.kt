package com.viam.feeder.data.storage

import android.R.attr.data
import android.content.Context
import androidx.annotation.WorkerThread
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import timber.log.Timber
import java.io.*
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class ConfigStorage @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val configFileName: String
) {

    private val file by lazy {
        File(appContext.cacheDir, configFileName)
    }

    private val configObject = lazy {
        JSONObject(read())
    }

    private fun read(): String {
        val inputStreamReader = InputStreamReader(file.inputStream())
        val bufferedReader = BufferedReader(inputStreamReader)
        return bufferedReader.use { it.readText() }
    }

    private fun write() {
        try {
            val openFileOutput = file.outputStream()
            val outputStreamWriter = OutputStreamWriter(openFileOutput)
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Timber.e("File write failed: %s", e.toString())
        }
    }

    override fun toString(): String {
        return configObject.value.toString()
    }

    var soundVolume by ReadWriteConfig(configObject, "soundVolume", 3.99)


}


class ReadWriteConfig<T>(
    private val configObject: Lazy<JSONObject>,
    private val name: String,
    private val defaultValue: T
) : ReadWriteProperty<Any, T> {

    @Suppress("UNCHECKED_CAST")
    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return (configObject.value.get(name) ?: defaultValue) as T
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        configObject.value.put(name, value)
    }
}
