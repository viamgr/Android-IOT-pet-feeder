package com.viam.feeder.core.task

import androidx.lifecycle.MutableLiveData
import javax.inject.Singleton

@Singleton
object AutoRetryHandler : MutableLiveData<Boolean>()