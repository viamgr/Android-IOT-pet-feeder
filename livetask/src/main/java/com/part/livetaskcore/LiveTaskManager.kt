package com.part.livetaskcore

import android.content.Context
import com.part.livetaskcore.connection.ConnectionManager

object LiveTaskManager {

    private var connectionManager: ConnectionManager? = null
    private var errorMapper: ErrorMapper = ErrorMapperImpl()
    private var errorObserver: ErrorObserverCallback = ErrorObserver

    fun getConnectionManager() = connectionManager
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

        private var connectionManager: ConnectionManager? = null
        private var errorMapper: ErrorMapper? = null
        private var errorObserver: ErrorObserverCallback? = null

        fun setUpConnectionManager(context: Context): Builder {
            this.connectionManager = ConnectionManager(context)
            return this
        }

        fun setErrorMapper(errorMapper: ErrorMapper): Builder {
            this.errorMapper = errorMapper
            return this
        }

        fun setErrorObserver(callback: ErrorObserverCallback): Builder {
            this.errorObserver = callback
            return this
        }

        fun apply() {
            applyItems(connectionManager, errorMapper, errorObserver)
        }
    }
}