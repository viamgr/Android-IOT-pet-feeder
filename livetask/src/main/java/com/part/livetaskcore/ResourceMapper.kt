package com.part.livetaskcore

fun interface ResourceMapper<R> {
    fun map(input: Any?): Resource<R>
}
