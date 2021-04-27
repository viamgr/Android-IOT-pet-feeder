package com.part.livetaskcore

class LiveTaskManager {

    private var connectionInformer: ConnectionInformer? = null
    private var errorMapper: ErrorMapper = ErrorMapperImpl()
    private var errorObserver: ErrorObserverCallback = ErrorObserver

    fun getConnectionInformer(): ConnectionInformer? = connectionInformer
    fun getErrorMapper() = errorMapper
    fun getErrorObserver() = errorObserver

    private fun applyItems(
        connectionInformer: ConnectionInformer?,
        errorMapper: ErrorMapper?,
        errorObserver: ErrorObserverCallback?
    ) {
        connectionInformer?.let {
            this@LiveTaskManager.connectionInformer = it
        }
        errorMapper?.let {
            this@LiveTaskManager.errorMapper = it
        }
        errorObserver?.let {
            this@LiveTaskManager.errorObserver = it
        }
        instance = this
    }

    inner class Builder {

        private var connectionInformer: ConnectionInformer? = null
        private var errorMapper: ErrorMapper? = null
        private var errorObserver: ErrorObserverCallback? = null

        fun setErrorMapper(errorMapper: ErrorMapper): Builder {
            this.errorMapper = errorMapper
            return this
        }

        fun setErrorObserver(callback: ErrorObserverCallback): Builder {
            errorObserver = callback
            return this
        }

        fun setConnectionInformer(checker: ConnectionInformer): Builder {
            connectionInformer = checker
            return this
        }

        fun apply() {
            applyItems(connectionInformer, errorMapper, errorObserver)
        }
    }

    companion object {
        var instance = LiveTaskManager()
    }
}