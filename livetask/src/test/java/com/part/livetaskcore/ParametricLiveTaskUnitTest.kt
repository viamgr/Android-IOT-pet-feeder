package com.part.livetaskcore

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.part.livetaskcore.livatask.ParametricCoroutineLiveTask
import com.part.livetaskcore.usecases.ParametricFlowUseCase
import com.part.livetaskcore.usecases.asLiveTask
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class ParametricLiveTaskUnitTest {
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
    fun initiate_parametricLiveTask_shouldSuccess() {
        ParametricCoroutineLiveTask<Any, Any>(liveTaskManager)
        assertTrue(true)
    }

    @Test
    fun getParameter_onInit_shouldNull() {
        val parametricCoroutineLiveTask = ParametricCoroutineLiveTask<Any, Any>(liveTaskManager)
        try {
            parametricCoroutineLiveTask.getParameter()
            assertTrue(false)
        } catch (e: Exception) {

        }
    }

    @Test
    fun setParameter_shouldSet() {
        val parametricCoroutineLiveTask = ParametricCoroutineLiveTask<Any, Any>(liveTaskManager)
        val parameter = "test"
        parametricCoroutineLiveTask.setParameter(parameter)
        assertEquals(parameter, parametricCoroutineLiveTask.getParameter())
    }

    @Test
    fun withParameter_shouldSet() {
        val parameter = "test"
        val parametricCoroutineLiveTask = ParametricCoroutineLiveTask<Any, Any>(liveTaskManager) {
            withParameter(parameter)
        }
        parametricCoroutineLiveTask.configure()
        assertEquals(parameter, parametricCoroutineLiveTask.getParameter())
    }

    @Test
    fun parametricFlow_invokeAndResult_shouldWork() = runBlockingTest {
        var result: String? = null
        val emitValue = "value"
        val parameter = "AnyParams"

        val flowOf = object : ParametricFlowUseCase<String, String> {
            override fun invoke(parameter: String): Flow<String> {
                return flowOf(emitValue)
            }
        }
        val parametricFlowUseCase = spyk(flowOf)
        val liveTask = parametricFlowUseCase.asLiveTask {
            onSuccess<String> {
                result = it
            }
            withParameter(parameter)
        }
        liveTask.run(coroutineDispatcher)
        assertEquals(result, emitValue)
        verify { parametricFlowUseCase.invoke(parameter) }
    }


    @Test
    fun parametricFlowWithMultipleEmit_successCount_shouldMatchWithEmitCount() = runBlockingTest {
        val parameter = "AnyParams"
        var successCount = 0

        val flowOf = object : ParametricFlowUseCase<String, String> {
            override fun invoke(parameter: String): Flow<String> {
                return flow {
                    emit("a")
                    emit("b")
                    emit("c")
                }
            }
        }
        val parametricFlowUseCase = spyk(flowOf)
        val liveTask = parametricFlowUseCase.asLiveTask {
            onSuccess<String> {
                successCount++
            }
            withParameter(parameter)
        }
        liveTask.run(coroutineDispatcher)
        assertEquals(3, successCount)
    }

}