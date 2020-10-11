package com.viam.feeder.core.network

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class WifiWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    companion object {

        fun start(context: Context): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true)
                .build()

            val request =
                PeriodicWorkRequestBuilder<WifiWorker>(1, TimeUnit.SECONDS)
                    // Additional configuration
                    .setConstraints(constraints)
                    .build()

            WorkManager
                .getInstance(context)
                .enqueue(request)
            return request
        }
    }

    override fun doWork(): Result {
        return Result.success()
    }
}
