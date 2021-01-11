package com.viam.feeder.data.storage

import android.content.Context
import androidx.annotation.WorkerThread
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import timber.log.Timber
import java.io.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Singleton
class ConfigStorage @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private val configFileName: String = "config.json"

    private val configObject = lazy {
        JSONObject(read())
    }

    val file by lazy {
        File(appContext.cacheDir, configFileName)
    }

    var soundVolume by ReadWriteConfig(configObject, "soundVolume", 3.99F)

    fun isConfigured() = configObject.isInitialized()

    fun write(input: InputStream) {
        val fos = FileOutputStream(file)
        fos.use { output ->
            val buffer = ByteArray(4 * 1024) // or other buffer size
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
            }
            output.flush()
        }
    }

    fun save() {
        try {
            val openFileOutput = file.outputStream()
            val outputStreamWriter = OutputStreamWriter(openFileOutput)
            outputStreamWriter.write(configObject.toString())
            outputStreamWriter.close()
        } catch (e: IOException) {
            Timber.e("File write failed: %s", e.toString())
        }
    }

    override fun toString(): String {
        return configObject.value.toString()
    }

    private fun read(): String {
        val inputStreamReader = InputStreamReader(file.inputStream())
        val bufferedReader = BufferedReader(inputStreamReader)
        return bufferedReader.use { it.readText() }
    }
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
