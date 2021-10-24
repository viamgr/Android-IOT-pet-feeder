package com.part.livetaskcore

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.part.livetaskcore.connection.WebConnectionChecker
import com.part.livetaskcore.livatask.CoroutineLiveTask
import com.part.livetaskcore.livatask.LiveTaskBuilder
import com.part.livetaskcore.views.ViewType
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CoroutineLiveTaskUnitTest {
    @Rule
    @JvmField
    val coroutineRule = CoroutineRule()

    /**
     * Example local unit test, which will execute on the development machine (host).
     *
     * See [testing documentation](http://d.android.com/tools/testing).
     */

    private val coroutineDispatcher = coroutineRule.testDispatcherProvider.io()

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @RelaxedMockK
    lateinit var liveTaskManager: LiveTaskManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun retry_coughException_onNotRunning() {
        val coroutineLiveTask = CoroutineLiveTask<String>(liveTaskManager)
        try {
            coroutineLiveTask.retry()
            assert(false) {
                "Retry should cough an exception"
            }
        } catch (e: Exception) {
            assertEquals(e.message, "You shouldn't retry before calling run")
        }
    }

    @Test
    fun cancelable_withTrue_ShouldTrue() {
        val coroutineLiveTask = CoroutineLiveTask<String>(liveTaskManager) {
            cancelable(true)
        }
        coroutineLiveTask.configure()
        assertTrue(coroutineLiveTask.isCancelable())
    }

    @Test
    fun cancelable_withFalse_ShouldFalse() {
        val coroutineLiveTask = CoroutineLiveTask<String>(liveTaskManager) {
            cancelable(false)
        }
        coroutineLiveTask.configure()
        assertFalse(coroutineLiveTask.isCancelable())
    }


    @Test
    fun cancelable_withNotSet_ShouldFalse() {
        val coroutineLiveTask = CoroutineLiveTask<String>(liveTaskManager)
        coroutineLiveTask.configure()
        assertFalse(coroutineLiveTask.isCancelable())
    }

    @Test
    fun initialState_shouldNull() = runBlocking {
        val coroutineLiveTask = CoroutineLiveTask<Any>(liveTaskManager)
        assertNull(coroutineLiveTask.result())
    }

    @Test
    fun loadingCallback_shouldCall() = runBlocking {

        var loadingCount = 0
        val block: LiveTaskBuilder<Any>.() -> Unit = {
            onLoading {
                loadingCount++
            }
        }
        val coroutineLiveTask = CoroutineLiveTask(liveTaskManager, block)
        coroutineLiveTask.run(coroutineDispatcher)
        assertEquals(1, loadingCount)
    }

    @Test
    fun successCallback_withoutResourceMapper_shouldReturnResource() =
        runBlocking {
            val block: LiveTaskBuilder<Any>.() -> Unit = {
                emitData {

                }
            }
            val coroutineLiveTask = CoroutineLiveTask(liveTaskManager, block)
            coroutineLiveTask.run(coroutineDispatcher)
            val result = coroutineLiveTask.result()
            assertTrue(result is Resource)
        }

    @Test
    fun resourceMapper_withSuccess_shouldMap() = runBlocking {

        val success = Resource.Success(Unit)

        every { liveTaskManager.resourceMapper } returns {
            success
        }

        val coroutineLiveTask = CoroutineLiveTask<String>(liveTaskManager) {
            emitData { "should return success" }
        }
        coroutineLiveTask.run(coroutineDispatcher)

        val result = coroutineLiveTask.result()

        assertEquals(success, result)
    }

    @Test
    fun emit_shouldCall() = runBlocking {
        val emitItem = "An Item"
        every { liveTaskManager.resourceMapper } returns null
        val result = CoroutineLiveTask<String>(liveTaskManager) {
            emitData { emitItem }
        }
            .run(coroutineDispatcher)
            .result()
        assertEquals(result.dataOrNull(), emitItem)
    }

    @Test
    fun errorMapper_onLiveTaskManager_shouldCall() = runBlocking {
        val mappedException = Exception("Mapped Exception")
        every { liveTaskManager.errorMapper } returns ErrorMapper {
            mappedException
        }
        val result = CoroutineLiveTask<String>(liveTaskManager) {
            emitResult { Resource.Error(Exception("Another Exception")) }
        }
            .run(coroutineDispatcher)
            .result()

        assertEquals(mappedException, (result as Resource.Error).exception)

    }

    @Test
    fun errorMapper_onDslBuilder_shouldCall() = runBlocking {
        val mappedException = Exception("Mapped Exception")
        val result = CoroutineLiveTask<String>(liveTaskManager) {
            errorMapper {
                mappedException
            }
            emitResult { Resource.Error(Exception("Another Exception")) }
        }
            .run(coroutineDispatcher)
            .result()

        assertEquals(mappedException, (result as Resource.Error).exception)

    }

    @Test
    fun errorMapper_withResult_shouldCall() = runBlocking {
        val mappedException = Exception("Mapped Exception")
        val result = CoroutineLiveTask<String>(liveTaskManager) {
            emitResult { Resource.Error(Exception("Another Exception")) }

            errorMapper {
                mappedException
            }
        }
            .run(coroutineDispatcher)
            .result()

        assertEquals(mappedException, (result as Resource.Error).exception)

    }

    @Test
    fun connectionInformer_withoutAutoRetry_shouldNotCall() = runBlocking {
        val exception = Exception("An Exception")
        CoroutineLiveTask<String>(liveTaskManager) {
            emitResult { Resource.Error(exception) }
        }
            .run(coroutineDispatcher).let {
                val connectionInformer = liveTaskManager.connectionInformer
                verify(exactly = 0) {
                    connectionInformer?.register(exception, it)
                }
            }
    }

    @Test
    fun connectionInformer_withAutoRetry_shouldCall() = runBlocking {
        val exception = Exception("An Exception")
        CoroutineLiveTask<String>(liveTaskManager) {
            autoRetry(true)
            emitResult { Resource.Error(exception) }
        }
            .run(coroutineDispatcher).let {
                verify {
                    liveTaskManager.connectionInformer?.register(exception, it)
                }
            }
    }

    @Test
    fun viewType_shouldSet() = runBlocking {
        val viewType = mockk<ViewType>()
        CoroutineLiveTask<String>(liveTaskManager) {
            viewType(viewType)
        }.run(coroutineDispatcher).let {
            assertEquals(viewType, it.loadingViewType())
        }
    }

    @Test
    fun onSuccess_shouldCall() = runBlocking {
        val success = Resource.Success(null)
        var successCount = 0
        CoroutineLiveTask<Nothing?>(liveTaskManager) {
            onSuccess<Nothing?> {
                successCount++
            }
            emitResult { success }
        }.run(coroutineDispatcher)
            .let {
                assertEquals(1, successCount)
            }
    }

    @Test
    fun onSuccess_data_shouldCall() = runBlocking {
        val success = Resource.Success("data")
        var onSuccessData: String? = null
        CoroutineLiveTask<String>(liveTaskManager) {
            onSuccess<String> {
                onSuccessData = it
            }
            emitResult { success }
        }.run(coroutineDispatcher)
            .let {
                assertEquals(success.data, onSuccessData)
            }
    }

    @Test
    fun onErrorCallback_WithErrorMapper_shouldCallWithError() = runBlocking {
        val error = Resource.Error(Exception())
        var errorHappenedInCallback: Exception? = null
        val mappedException = Exception("New Exception")
        every { liveTaskManager.errorMapper } returns ErrorMapper {
            mappedException
        }

        CoroutineLiveTask<String>(liveTaskManager) {
            onError {
                errorHappenedInCallback = it
            }
            emitResult { error }
        }.run(coroutineDispatcher)
            .let {
                assertEquals(error.exception, errorHappenedInCallback)
            }
    }

    @Test
    fun liveResult_shouldChange() = runBlocking {
        val error = Resource.Error(Exception())
        CoroutineLiveTask<String>(liveTaskManager) {
            emitResult { error }
        }.run(coroutineDispatcher)
            .let {
                val liveResult = it.liveResult
                assertTrue(liveResult.getOrAwaitValue() is Resource.Error)
            }
    }


    @Test
    fun webConnectionChecker_onRetry_shouldRetryLiveTask() = runBlocking {
        val error = Resource.Error(Exception("Unable to resolve host"))

        val webConnectionChecker = WebConnectionChecker(mockk(relaxed = true))
        every { liveTaskManager.connectionInformer } returns webConnectionChecker

        val task = spyk<CoroutineLiveTask<Nothing>>(CoroutineLiveTask(liveTaskManager) {
            autoRetry(true)
        })


        task.run(coroutineDispatcher)
        task.setResultForTest(error)

        webConnectionChecker.retryFailed()

        verify(exactly = 1) { task.retry() }
    }
}