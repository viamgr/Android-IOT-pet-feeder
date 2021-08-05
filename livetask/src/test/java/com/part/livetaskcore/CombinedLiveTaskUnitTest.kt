package com.part.livetaskcore

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.part.livetaskcore.livatask.BaseLiveTask
import com.part.livetaskcore.livatask.CombinedException
import com.part.livetaskcore.livatask.CombinedLiveTask
import com.part.livetaskcore.livatask.CoroutineLiveTask
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CancellationException

class CombinedLiveTaskUnitTest {
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
    fun singleLiveTask_onSuccess_returnSuccess() {
        val success = Resource.Success("value")
        val liveTask1 = CoroutineLiveTask<String> {
            emitBlock { success }
        }
        val combinedLiveTask = CombinedLiveTask(liveTask1, liveTaskManager = liveTaskManager)
        liveTask1.run(coroutineDispatcher)
        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue(result is Resource.Success)
    }

    @Test
    fun multipleLiveTask_onSuccess_returnSuccess() {
        val success1 = Resource.Success("value")
        val success2 = Resource.Success("value")
        val liveTask1 = CoroutineLiveTask<String> {
            emitBlock { success1 }
        }
        val liveTask2 = CoroutineLiveTask<String> {
            emitBlock { success2 }
        }
        val combinedLiveTask =
            CombinedLiveTask(liveTask1, liveTask2, liveTaskManager = liveTaskManager)
        liveTask1.run(coroutineDispatcher)
        liveTask2.run(coroutineDispatcher)
        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue(result is Resource.Success)
    }

    @Test
    fun multipleLiveTask_withoutRun_DoNotReturnErrorOrLoading() {
        val success1 = Resource.Success("value")
        val success2 = Resource.Success("value")
        val liveTask1 = spyk(CoroutineLiveTask<String>())
        liveTask1.setResultForTest(success1)

        val liveTask2 = spyk(CoroutineLiveTask<String>())
        liveTask2.setResultForTest(success2)

        val combinedLiveTask =
            CombinedLiveTask(liveTask1, liveTask2, liveTaskManager = liveTaskManager)
        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue(result !is Resource.Error)
        assertTrue(result !is Resource.Loading)
    }

    @Test
    fun multipleLiveTask_withErrorAndSuccess_ReturnError() {
        val success = Resource.Success("value")
        val error = Resource.Error(Exception())
        val successLiveTask = CoroutineLiveTask<String> {
            emitBlock { success }
        }.run(coroutineDispatcher)
        val errorLiveTask = CoroutineLiveTask<String> {
            emitBlock { error }
        }.run(coroutineDispatcher)

        val combinedLiveTask =
            CombinedLiveTask(successLiveTask, errorLiveTask, liveTaskManager = liveTaskManager)
        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue(result is Resource.Error)
    }

    @Test
    fun multipleLiveTask_withError_returnCombinedException() {
        val error = Resource.Error(Exception())

        val errorLiveTask = CoroutineLiveTask<String> {
            emitBlock { error }
        }.run(coroutineDispatcher)

        every { liveTaskManager.errorMapper } returns ErrorMapper {
            it
        }
        val combinedLiveTask =
            CombinedLiveTask(errorLiveTask, liveTaskManager = liveTaskManager)
        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue((result as Resource.Error).exception is CombinedException)
    }

    @Test
    fun multipleLiveTask_withLoadingSuccessError_returnErrorResult() {
        val success = Resource.Success(null)
        val error = Resource.Error(Exception())
        val loading = Resource.Loading()

        val successLiveTask = CoroutineLiveTask<String> {
            emitBlock { success }
        }.run(coroutineDispatcher)

        val loadingLiveTask = CoroutineLiveTask<String> {
            emitBlock { loading }
        }.run(coroutineDispatcher)

        val errorLiveTask = CoroutineLiveTask<String> {
            emitBlock { error }
        }.run(coroutineDispatcher)

        val combinedLiveTask =
            CombinedLiveTask(
                errorLiveTask,
                loadingLiveTask,
                successLiveTask,
                liveTaskManager = liveTaskManager
            )
        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue(result is Resource.Error)
    }

    @Test
    fun multipleLiveTask_withLoadingSuccess_returnLoadingResult() {
        val success = Resource.Success(null)
        val loading = Resource.Loading()

        val successLiveTask = CoroutineLiveTask<String> {
            emitBlock { success }
        }.run(coroutineDispatcher)

        val loadingLiveTask = CoroutineLiveTask<String> {
            emitBlock { loading }
        }.run(coroutineDispatcher)

        val combinedLiveTask =
            CombinedLiveTask(
                loadingLiveTask,
                successLiveTask,
                liveTaskManager = liveTaskManager
            )
        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue(result is Resource.Loading)
    }

    @Test
    fun retry_Failed_ShouldWorking() {
        val success = Resource.Success("null")
        val error = Resource.Error(Exception())

        val successLiveTask = spyk(CoroutineLiveTask<String>())
        successLiveTask.setResultForTest(success)
        val errorLiveTask = spyk(CoroutineLiveTask<Any>())
        errorLiveTask.setResultForTest(error)

        val combinedLiveTask =
            CombinedLiveTask(
                errorLiveTask,
                successLiveTask,
                liveTaskManager = liveTaskManager
            )
        assertTrue(combinedLiveTask.liveResult.getOrAwaitValue() is Resource.Error)

        combinedLiveTask.run(coroutineDispatcher)
        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue(result is Resource.Loading)
    }

    @Test
    fun retry_OnNotRetryableFailed_ShouldNotRetryable() {
        val error = Resource.Error(Exception())

        val errorLiveTask = spyk(CoroutineLiveTask<Any>() {
            retryable(false)
        })
        errorLiveTask.setResultForTest(error)

        val combinedLiveTask =
            CombinedLiveTask(
                errorLiveTask,
                liveTaskManager = liveTaskManager
            )
        assertFalse(combinedLiveTask.isRetryable())
    }

    @Test
    fun retryable_OnAtLeastOneRetryable_ShouldRetryable() {
        val error = Resource.Error(Exception())
        val errorLiveTask = spyk(CoroutineLiveTask<Any>() {
            retryable(false)
        })
        errorLiveTask.setResultForTest(error)


        val error2 = Resource.Error(Exception())
        val error2LiveTask = spyk(CoroutineLiveTask<Any>() {
            retryable(true)
        })
        error2LiveTask.setResultForTest(error2)


        val combinedLiveTask =
            CombinedLiveTask(
                errorLiveTask,
                error2LiveTask,
                liveTaskManager = liveTaskManager
            )
        assertTrue(combinedLiveTask.isRetryable())
    }

    @Test
    fun retry_onNotRetryableTask_shouldNotRetryThat() = runBlocking {
        val error = Resource.Error(Exception())
        val errorLiveTask = spyk(CoroutineLiveTask<Any> {
            retryable(false)
            emitBlock { error }
        })
        errorLiveTask.run(coroutineDispatcher)

        val combinedLiveTask = CombinedLiveTask(
            errorLiveTask,
            liveTaskManager = liveTaskManager
        )
        combinedLiveTask.retry()
        verify(exactly = 0) { errorLiveTask.retry() }
    }

    @Test
    fun retry_atLeaseOneNotRetryable_shouldNotRetryable() = runBlocking {
        val error = Resource.Error(Exception())
        val errorLiveTask = spyk(CoroutineLiveTask<Any> {
            retryable(false)
            emitBlock { error }
        })
        errorLiveTask.run(coroutineDispatcher)

        val success = Resource.Success(null)
        val successLiveTask = spyk(CoroutineLiveTask<Any> {
            retryable(true)
            emitBlock { success }
        })
        successLiveTask.run(coroutineDispatcher)


        val combinedLiveTask = CombinedLiveTask(
            errorLiveTask,
            successLiveTask,
            liveTaskManager = liveTaskManager
        )
        assertFalse(combinedLiveTask.isRetryable())

    }

    @Test
    fun retryable_onInit_shouldBeNull() {

        val combinedLiveTask = CombinedLiveTask(liveTaskManager = liveTaskManager)
        assertNull(combinedLiveTask.isRetryableForTesting())

    }

    @Test
    fun multipleLiveTask_withDoubleSuccess_returnSuccessResult() {
        val success = Resource.Success(null)
        val success2 = Resource.Success(null)

        val successLiveTask = CoroutineLiveTask<String> {
            emitBlock { success }
        }.run(coroutineDispatcher)

        val success2LiveTask = CoroutineLiveTask<String> {
            emitBlock { success2 }
        }.run(coroutineDispatcher)


        val combinedLiveTask =
            CombinedLiveTask(
                success2LiveTask,
                successLiveTask,
                liveTaskManager = liveTaskManager
            )
        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue(result is Resource.Success)
    }


    @Test
    fun multipleLiveTask_cancel_shouldCancelAllTasks() {
        val loading = Resource.Loading()
        val success = Resource.Success(null)

        every { liveTaskManager.errorMapper } returns ErrorMapper {
            it
        }

        val loadingLiveTask = spyk(CoroutineLiveTask<Any> { }) as BaseLiveTask<Any>
        loadingLiveTask.setResultForTest(loading)

        val successLiveTask = spyk(CoroutineLiveTask<Any> { }) as BaseLiveTask<Any>
        successLiveTask.setResultForTest(success)

        val combinedLiveTask =
            CombinedLiveTask(
                successLiveTask,
                loadingLiveTask,
                liveTaskManager = liveTaskManager
            )
        combinedLiveTask.cancel(true)

        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue((loadingLiveTask.result() as Resource.Error).exception is CancellationException)
        assertTrue(successLiveTask.result() is Resource.Success)
        assertTrue((result as Resource.Error).exception is CancellationException)
    }

    @Test
    fun multipleLiveTask_cancelOnSingle_shouldKeepError() {
        val error = Resource.Error(Exception())

        every { liveTaskManager.errorMapper } returns ErrorMapper {
            it
        }

        val errorLiveTask = spyk(CoroutineLiveTask<Any>())
        errorLiveTask.setResultForTest(error)

        val combinedLiveTask =
            CombinedLiveTask(
                errorLiveTask,
                liveTaskManager = liveTaskManager
            )
        combinedLiveTask.cancel(true)

        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue((result as Resource.Error).exception is CancellationException)
        assertTrue((errorLiveTask.result() as Resource.Error).exception is CancellationException)
    }

    @Test
    fun multipleCombinedLiveTask_cancel_shouldCancelAllTasks() {
        val loading = Resource.Loading()
        val success = Resource.Success(null)

        every { liveTaskManager.errorMapper } returns ErrorMapper {
            it
        }

        val loadingLiveTask = spyk(CoroutineLiveTask<Any> { }) as BaseLiveTask<Any>
        loadingLiveTask.setResultForTest(loading)

        val successLiveTask = spyk(CombinedLiveTask()) as BaseLiveTask<Any>
        successLiveTask.setResultForTest(success)

        val combinedLiveTask =
            CombinedLiveTask(
                successLiveTask,
                loadingLiveTask,
                liveTaskManager = liveTaskManager
            )
        combinedLiveTask.cancel(true)

        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue((loadingLiveTask.result() as Resource.Error).exception is CancellationException)
        assertTrue(successLiveTask.result() is Resource.Success)
        assertTrue((result as Resource.Error).exception is CancellationException)
    }

    @Test
    fun multipleCombinedLiveTask_nestedCancel_shouldCancelAllTasks() {
        val loading = Resource.Loading()
        val success = Resource.Success(null)

        every { liveTaskManager.errorMapper } returns ErrorMapper {
            it
        }

        val loadingLiveTask = spyk(CoroutineLiveTask<Any>()) as BaseLiveTask<Any>
        loadingLiveTask.setResultForTest(loading)

        val nestedLiveTask = spyk(CoroutineLiveTask<Any>()) as BaseLiveTask<Any>
        nestedLiveTask.setResultForTest(loading)

        val successLiveTask = spyk(CombinedLiveTask(nestedLiveTask)) as BaseLiveTask<Any>
        successLiveTask.setResultForTest(success)

        val combinedLiveTask =
            CombinedLiveTask(
                successLiveTask,
                loadingLiveTask,
                liveTaskManager = liveTaskManager
            )
        combinedLiveTask.cancel(true)

        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue((loadingLiveTask.result() as Resource.Error).exception is CancellationException)
        assertTrue((nestedLiveTask.result() as Resource.Error).exception is CancellationException)
        assertTrue(successLiveTask.result() is Resource.Success)
        assertTrue((result as Resource.Error).exception is CancellationException)
    }

    @Test
    fun multipleCombinedLiveTask_nestedCancel_shouldNotCancelSuccessTasks() {
        val loading = Resource.Loading()
        val success = Resource.Success(null)

        every { liveTaskManager.errorMapper } returns ErrorMapper {
            it
        }

        val loadingLiveTask = spyk(CoroutineLiveTask<Any>()) as BaseLiveTask<Any>
        loadingLiveTask.setResultForTest(loading)

        val nestedLiveTask = spyk(CoroutineLiveTask<Any>()) as BaseLiveTask<Any>
        nestedLiveTask.setResultForTest(success)

        val successLiveTask = spyk(CombinedLiveTask(nestedLiveTask)) as BaseLiveTask<Any>
        successLiveTask.setResultForTest(success)

        val combinedLiveTask =
            CombinedLiveTask(
                successLiveTask,
                loadingLiveTask,
                liveTaskManager = liveTaskManager
            )
        combinedLiveTask.cancel(true)

        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue((loadingLiveTask.result() as Resource.Error).exception is CancellationException)
        assertTrue(successLiveTask.result() is Resource.Success)
        assertTrue((result as Resource.Error).exception is CancellationException)
    }

    @Test
    fun cancelLiveTask_onSingleCombinedLiveTask_shouldNotCancelSuccessTasks() {
        val success = Resource.Success(null)

        every { liveTaskManager.errorMapper } returns ErrorMapper {
            it
        }

        val nestedCombinedLiveTask = spyk(CombinedLiveTask())
        nestedCombinedLiveTask.setResultForTest(success)

        val combinedLiveTask =
            CombinedLiveTask(
                nestedCombinedLiveTask,
                liveTaskManager = liveTaskManager
            )
        combinedLiveTask.cancel(true)

        val result = combinedLiveTask.liveResult.getOrAwaitValue()
        assertTrue(nestedCombinedLiveTask.result() is Resource.Success)
        assertTrue(result is Resource.Success)
    }

    @Test
    fun isCancelable_onSingleLiveTask_shouldBeCancelable() {

        val coroutineLiveTask = spyk(CoroutineLiveTask<Any>())
        coroutineLiveTask.cancelable(true)

        val combinedLiveTask =
            CombinedLiveTask(coroutineLiveTask, liveTaskManager = liveTaskManager)
        assertEquals(true, combinedLiveTask.isCancelable())
    }

    @Test
    fun isCancelable_onDoubleVariantLiveTask_shouldNotBeCancelable() {

        val coroutineLiveTask = spyk(CoroutineLiveTask<Any>()).apply {
            cancelable(true)
        }
        val coroutineLiveTask2 = spyk(CoroutineLiveTask<Any>().apply {
            cancelable(false)
        })
        val combinedLiveTask =
            CombinedLiveTask(
                coroutineLiveTask,
                coroutineLiveTask2,
                liveTaskManager = liveTaskManager
            )
        assertEquals(true, combinedLiveTask.isCancelable())
    }

    @Test
    fun isCancelable_onDoubleNotCancelable_shouldNotBeCancelable() {

        val coroutineLiveTask = spyk(CoroutineLiveTask<Any> {
            cancelable(false)
        })
        val coroutineLiveTask2 = spyk(CoroutineLiveTask<Any> {
            cancelable(false)

        })
        val combinedLiveTask =
            CombinedLiveTask(
                coroutineLiveTask,
                coroutineLiveTask2,
                liveTaskManager = liveTaskManager
            )
        assertEquals(false, combinedLiveTask.isCancelable())
    }

    @Test
    fun isCancelable_onDoubleCancelable_shouldBeCancelable() {

        val coroutineLiveTask = spyk(CoroutineLiveTask<Any> {
            cancelable(true)
        })
        val coroutineLiveTask2 = spyk(CoroutineLiveTask<Any> {
            cancelable(true)
        })
        val combinedLiveTask =
            CombinedLiveTask(
                coroutineLiveTask,
                coroutineLiveTask2,
                liveTaskManager = liveTaskManager
            )
        assertEquals(true, combinedLiveTask.isCancelable())
    }

}