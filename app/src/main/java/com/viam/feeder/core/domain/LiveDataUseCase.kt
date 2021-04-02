package com.viam.feeder.core.domain

import androidx.lifecycle.LiveData

abstract class LiveDataUseCase<R> {
    operator fun invoke(): LiveData<R> {
        return getField()
    }

    protected abstract fun getField(): LiveData<R>
}