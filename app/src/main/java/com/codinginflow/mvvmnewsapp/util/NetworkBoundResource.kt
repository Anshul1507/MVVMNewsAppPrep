package com.codinginflow.mvvmnewsapp.util

import kotlinx.coroutines.flow.*

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline onFetchSuccess: () -> Unit = { },
    crossinline onFetchFailed: (Throwable) -> Unit = { },
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = flow {
    val data = query().first()

    val flow = if (shouldFetch(data)) {
        emit(Resource.Loading(data))

        try {
            kotlinx.coroutines.delay(5000)
            saveFetchResult(fetch())
            onFetchSuccess()
            query().map { Resource.Success(it) }
        } catch (t: Throwable) {
            onFetchFailed(t)
            query().map { Resource.Error(t, it) }
        }
    } else {
        query().map { Resource.Success(it) }
    }
    emitAll(flow)
}