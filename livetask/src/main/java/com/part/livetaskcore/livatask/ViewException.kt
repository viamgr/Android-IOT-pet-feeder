package com.part.livetaskcore.livatask

data class ViewException(val viewMessage: String, override val cause: Throwable?) : Exception()