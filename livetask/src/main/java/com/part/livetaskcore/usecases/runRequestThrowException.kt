package com.part.livetaskcore.usecases

import com.part.livetaskcore.utils.detectException
import com.viam.resource.Resource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException

suspend fun <R> runRequestThrowException(
    coroutineDispatcher: CoroutineDispatcher,
    action: suspend () -> R,
): Resource<R> {
    return try {
        withContext(coroutineDispatcher) {
            Resource.Success(action())
        }
    } catch (e: CancellationException) {
        Resource.Error(e)
    } catch (e: IOException) {
        Resource.Error(e.detectException())
    } catch (e: Exception) {
        Resource.Error(e)
    }
}