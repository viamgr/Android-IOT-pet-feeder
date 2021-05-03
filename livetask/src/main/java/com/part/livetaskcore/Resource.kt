package com.part.livetaskcore

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Resource<out T> {
    data class Success<out T : Any>(val data: T?) : Resource<T>()
    data class Error(val exception: Exception) : Resource<Nothing>()
    data class Loading(val data: Any? = null) : Resource<Nothing>()
}

inline fun <reified T> Resource.Loading.data(): T? {
    return this.data as T
}

fun <T> Resource<T>.onSuccess(callback: (T?) -> Unit): Resource<T> {
    if (this is Resource.Success) {
        callback.invoke(this.data)
    }
    return this
}

fun <T> Resource<T>.onError(callback: (Exception) -> Unit): Resource<T> {
    if (this is Resource.Error) {
        callback.invoke(exception)
    }
    return this
}

inline fun <T, reified L> Resource<Any?>.onLoading(callback: (data: L?) -> Unit): Resource<T> {
    if (this is Resource.Loading) {
        callback.invoke(this.data as L)
    }
    return this as Resource<T>
}

fun <T> Resource<T>.isSuccess(): Boolean {
    return this is Resource.Success
}

fun <T> Resource<T>.isLoading(): Boolean {
    return this is Resource.Loading
}

fun <T> Resource<T>.isError(): Boolean {
    return this is Resource.Error
}

fun <T> Resource<T>?.dataOrNull(): T? {
    return if (this is Resource.Success) this.data else null
}

inline fun <T> Resource<T>.withResult(
    onLoading: (Any?) -> Unit = {},
    onSuccess: (T?) -> Unit = {},
    onError: (Exception) -> Unit = {},
) {
    when (this) {
        is Resource.Success -> {
            onLoading(false)
            onSuccess(data)
        }
        is Resource.Error -> {
            onLoading(false)
            onError(exception)
        }
        is Resource.Loading -> {
            onLoading(true)
        }
    }
}