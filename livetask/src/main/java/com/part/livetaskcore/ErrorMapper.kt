package com.part.livetaskcore

interface ErrorMapper {
    fun mapError(exception: Exception): Exception
}