package com.tsunetomo.raven.util

sealed class Resource<T>(val data: T?, val error: Throwable?) {
    class Success<T>(data: T?) : Resource<T>(data, null)
    class Error<T>(error: Throwable?) : Resource<T>(null, error)
    class Loading<T> : Resource<T>(null, null)
}
