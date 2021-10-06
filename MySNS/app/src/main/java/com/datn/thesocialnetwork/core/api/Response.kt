package com.datn.thesocialnetwork.core.api

sealed class Response<T>(
    var data: T? = null,
) {
    class Loading<T>(val message: String = "") : Response<T>()

    class Success<T>(
        data: T? = null,
        val message: String = "",
    ) : Response<T>(data)

    class Error<T>(val message: String = "") : Response<T>()
}
