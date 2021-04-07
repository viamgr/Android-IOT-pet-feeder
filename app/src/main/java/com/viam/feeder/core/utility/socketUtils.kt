package com.viam.feeder.core.utility

import com.viam.resource.Resource
import com.viam.websocket.model.SocketTransfer
import com.viam.websocket.model.SocketTransfer.Error
import com.viam.websocket.model.SocketTransfer.Success

fun SocketTransfer.toResource(): Resource<SocketTransfer> {
    return when (this) {
        is Success -> {
            Resource.Success(this)
        }
        is Error -> {
            Resource.Error(exception)
        }
        else -> {
            Resource.Loading(Resource.Loading(this))
        }
    }
}