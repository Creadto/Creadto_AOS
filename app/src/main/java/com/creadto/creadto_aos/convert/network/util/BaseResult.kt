package com.creadto.creadto_aos.convert.network.util

sealed class BaseResult<T : Any> {
    class Success<T: Any>(val data: T) : BaseResult<T>()
    class Error<T: Any>(val code: Int, val message: String?) : BaseResult<T>()
    class Exception<T: Any>(val e: Throwable) : BaseResult<T>()
}