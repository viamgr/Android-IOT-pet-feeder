package com.part.livetaskcore

fun interface ErrorMapper {
    fun mapError(exception: Exception): Exception
}