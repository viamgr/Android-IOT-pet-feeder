package com.part.livetaskcore

class ErrorMapperImpl : ErrorMapper {
    override fun mapError(exception: Exception) = exception
}