package com.codinginflow.mvvmnewsapp.util

sealed class Resource<T>(
    val data: T? = null,
    val throwable: Throwable? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    // TODO: 15.01.2021 I modified this to take a Throwable instead of a string
    class Error<T>(throwable: Throwable, data: T? = null) : Resource<T>(data, throwable)
}