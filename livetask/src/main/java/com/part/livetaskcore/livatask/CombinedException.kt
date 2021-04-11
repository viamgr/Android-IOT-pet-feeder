package com.part.livetaskcore.livatask

data class CombinedException(val exceptions: MutableList<Exception>) :
    Exception(exceptions.joinToString("\n") { it.message.toString() })