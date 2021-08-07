package com.viam.feeder.domain.base

import androidx.lifecycle.LiveData

abstract class LiveDataUseCase<R> {
    operator fun invoke(): LiveData<R> {
        return getField()
    }

    protected abstract fun getField(): LiveData<R>
}