package com.part.livetaskcore

import android.content.Context
import com.part.livetaskcore.connection.ConnectionManager

typealias NoConnectionInformer = (throwable: Throwable) -> Boolean

object LiveTaskManager {

    private var connectionManager: ConnectionManager? = null
    private var noConnectionInformer: NoConnectionInformer? = null
    private var errorMapper: ErrorMapper = ErrorMapperImpl()
    private var errorObserver: ErrorObserverCallback = ErrorObserver

    fun getConnectionManager() = connectionManager
    fun getNoConnectionInformer(): NoConnectionInformer? = noConnectionInformer
    fun getErrorMapper() = errorMapper
    fun getErrorObserver() = errorObserver

    private fun applyItems(
        connectionManager: ConnectionManager?,
        errorMapper: ErrorMapper?,
        errorObserver: ErrorObserverCallback?
    ) {
        connectionManager?.let {
            this@LiveTaskManager.connectionManager = it
        }
        errorMapper?.let {
            this@LiveTaskManager.errorMapper = it
        }
        errorObserver?.let {
            this@LiveTaskManager.errorObserver = it
        }
    }

    class Builder {

        fun setUpConnectionManager(context: Context): Builder {
            connectionManager = ConnectionManager(context)
            return this
        }

        fun setErrorMapper(errorMapper: ErrorMapper): Builder {
            LiveTaskManager.errorMapper = errorMapper
            return this
        }

        fun setErrorObserver(callback: ErrorObserverCallback): Builder {
            errorObserver = callback
            return this
        }

        fun setNoConnectionInformer(checker: NoConnectionInformer): Builder {
            noConnectionInformer = checker
            return this
        }

        fun apply() {
            applyItems(connectionManager, errorMapper, errorObserver)
        }
    }
}