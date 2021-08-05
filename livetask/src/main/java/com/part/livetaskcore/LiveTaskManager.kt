package com.part.livetaskcore

import com.part.livetaskcore.connection.ConnectionInformer
import com.part.livetaskcore.views.*

class LiveTaskManager {
    var connectionInformer: ConnectionInformer? = null
    var errorMapper: ErrorMapper = ErrorMapperImpl()
    var resourceMapper: ResourceMapper<*>? = null
    var viewTypes: MutableList<ViewTypeStore> = mutableListOf()

    var defaultViewType = DefaultViewTypes.Blur

    private fun applyItems(
        connectionInformer: ConnectionInformer?,
        errorMapper: ErrorMapper?,
        resourceMapper: ResourceMapper<*>?,
        viewTypes: MutableList<ViewTypeStore>,
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
        if (viewTypes.isNotEmpty()) {
            this.viewTypes.addAll(viewTypes)
        }
        instance = this
    }

    inline fun <reified TH : ViewTypeHandler> findViewTypeHandler(viewType: ViewType?): TH {
        val viewTypeStore =
            viewTypes.firstOrNull { viewType ?: defaultViewType == it.viewType && it.viewTypeHandler is TH }
        val viewTypeHandler = viewTypeStore?.viewTypeHandler as TH?
        return viewTypeHandler ?: error("$viewType hasn't been set.")

    }

    inner class Builder {


        private var connectionInformer: ConnectionInformer? = null
        private var errorMapper: ErrorMapper? = null
        private var resourceMapper: ResourceMapper<*>? = null
        private var viewTypes = mutableListOf<ViewTypeStore>()

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

        fun setViewType(
            viewType: ViewType,
            viewTypeHandler: ViewTypeHandler,
        ): Builder {
            if (viewTypes.any { it.viewType == viewType && it.viewTypeHandler.javaClass == viewTypeHandler.javaClass }) {
                throw Exception("$viewType is already set")
            }
            viewTypes.add(ViewTypeStore(viewType, viewTypeHandler))
            return this
        }

        fun apply() {
            applyItems(connectionInformer, errorMapper, resourceMapper, viewTypes)
        }
    }

    companion object {
        var instance = LiveTaskManager()
    }
}