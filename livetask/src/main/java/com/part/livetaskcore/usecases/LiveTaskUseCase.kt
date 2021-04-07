package com.part.livetaskcore.usecases

import com.part.livetaskcore.livatask.LiveTask
import com.part.livetaskcore.livatask.LiveTaskBuilder
import com.part.livetaskcore.livatask.liveTask
import com.viam.resource.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.experimental.ExperimentalTypeInference

abstract class LiveTaskUseCase<in P, R>(private val coroutineDispatcher: CoroutineDispatcher) {
    private var parameter: P? = null

    private var block: suspend LiveTaskBuilder<R>.() -> Unit = {}
    private var dynamicParameter: (() -> P)? = null

    private val task: LiveTask<R> by lazy {
        liveTask {
            block.invoke(this)
            val params = when {
                dynamicParameter != null -> {
                    dynamicParameter?.invoke()
                }
                parameter != null -> {
                    parameter
                }
                else -> {
                    null
                }
            }

            params?.let {
                val result =
                    runRequestThrowException(coroutineDispatcher) { execute(params = it) }
                emit(result)
            } ?: this.run {
                emit(Resource.Error(KotlinNullPointerException()))
            }
        }
    }

    abstract suspend fun execute(params: P): R


    @Deprecated(
        ".asLiveTask(parameter)",
        replaceWith = ReplaceWith("setParameter(parameter).asLiveTask(block)")
    )
    @OptIn(ExperimentalTypeInference::class)
    fun asLiveTask(
        parameter: P,
        @BuilderInference block: suspend LiveTaskBuilder<R>.() -> Unit = {},
    ): LiveTask<R> {
        this.block = block
        setParameter(parameter)
        return task
    }

    @OptIn(ExperimentalTypeInference::class)
    fun asLiveTask(
        @BuilderInference block: suspend LiveTaskBuilder<R>.() -> Unit = {},
    ): LiveTask<R> {
        this.block = block
        return task
    }

    fun setParameter(parameter: () -> P): LiveTaskUseCase<P, R> {
        this.dynamicParameter = parameter
        return this
    }

    fun setParameter(parameter: P): LiveTaskUseCase<P, R> {
        this.parameter = parameter
        return this
    }
}

