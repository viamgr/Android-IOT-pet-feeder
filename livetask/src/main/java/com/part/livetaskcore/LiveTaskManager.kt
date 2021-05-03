package com.part.livetaskcore

class LiveTaskManager {

    var connectionInformer: ConnectionInformer? = null
    var errorMapper: ErrorMapper = ErrorMapperImpl()
    var resourceMapper: ResourceMapper<*> = ResourceMapper { it as Resource<*> }


    private fun applyItems(
        connectionInformer: ConnectionInformer?,
        errorMapper: ErrorMapper?,
        resourceMapper: ResourceMapper<*>?,
    ) {
        connectionInformer?.let {
            this@LiveTaskManager.connectionInformer = it
        }
        errorMapper?.let {
            this@LiveTaskManager.errorMapper = it
        }
        resourceMapper?.let {
            this@LiveTaskManager.resourceMapper = it
        }
        instance = this
    }

    inner class Builder {

        private var connectionInformer: ConnectionInformer? = null
        private var errorMapper: ErrorMapper? = null
        var resourceMapper: ResourceMapper<*>? = null

        fun setErrorMapper(errorMapper: ErrorMapper): Builder {
            this.errorMapper = errorMapper
            return this
        }


        fun setConnectionInformer(checker: ConnectionInformer): Builder {
            connectionInformer = checker
            return this
        }

        fun setResourceMapper(resourceMapper: ResourceMapper<*>): Builder {
            this.resourceMapper = resourceMapper
            return this
        }

        fun apply() {
            applyItems(connectionInformer, errorMapper, resourceMapper)
        }
    }

    companion object {
        var instance = LiveTaskManager()
    }
}