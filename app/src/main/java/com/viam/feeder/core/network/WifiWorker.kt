package com.viam.feeder.core.network

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*

class WifiWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    companion object {

        fun start(context: Context): LiveData<WorkInfo> {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            val request =
                OneTimeWorkRequestBuilder<WifiWorker>()
                    // Additional configuration
                    .setConstraints(constraints)
                    .build()

            val instance = WorkManager
                .getInstance(context)

            instance.enqueue(request)
            return instance.getWorkInfoByIdLiveData(request.id)
        }
    }

    override fun doWork(): Result {
        return Result.success()
    }
}
