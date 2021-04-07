package com.part.livetaskcore.utils

data class BaseResponse<T>(val meta: Meta, val data: T)

data class Meta(val statusCode: Int, val message: String, val requestTime: Int)