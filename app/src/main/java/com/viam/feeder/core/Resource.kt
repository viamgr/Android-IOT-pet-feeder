/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.viam.feeder.core

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Resource<out T> {

    data class Success<out T : Any>(val data: T) : Resource<T>()
    data class Error(val exception: Exception) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}


fun <T> Resource<T>.onSuccess(callback: (T) -> Unit): Resource<T> {
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

fun <T> Resource<T>.onLoading(callback: () -> Unit): Resource<T> {
    if (this is Resource.Loading) {
        callback.invoke()
    }
    return this
}

fun <T> Resource<T>.isLoading(): Boolean {
    return this is Resource.Loading
}

fun <T> Resource<T>?.dataOrNull(): T? {
    return if (this is Resource.Success) this.data else null
}