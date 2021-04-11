package com.part.livetaskcore

class LiveTaskManager {

    private var noConnectionInformer: NoConnectionInformerAAA? = null
    private var errorMapper: ErrorMapper = ErrorMapperImpl()
    private var errorObserver: ErrorObserverCallback = ErrorObserver

    fun getNoConnectionInformer(): NoConnectionInformerAAA? = noConnectionInformer
    fun getErrorMapper() = errorMapper
    fun getErrorObserver() = errorObserver

    private fun applyItems(
        noConnectionInformer: NoConnectionInformerAAA?,
        errorMapper: ErrorMapper?,
        errorObserver: ErrorObserverCallback?
    ) {
        noConnectionInformer?.let {
            this@LiveTaskManager.noConnectionInformer = it
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

        private var noConnectionInformer: NoConnectionInformerAAA? = null
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

        fun setNoConnectionInformer(checker: NoConnectionInformerAAA): Builder {
            noConnectionInformer = checker
            return this
        }

        fun apply() {
            applyItems(noConnectionInformer, errorMapper, errorObserver)
        }
    }

    companion object {
        var instance = LiveTaskManager()
    }
}