package com.part.livetaskcore.utils

class NoInternetException(message: String) : Exception(message)
class NoConnectionException(message: String) : Exception(message)
class UnAuthorizedException : Exception("")

fun Exception.detectException(): Exception {
    return when {
        this.message.toString().startsWith("Unable to resolve host") -> {
            NoConnectionException("")
        }
        else -> {
            this
        }
    }
}