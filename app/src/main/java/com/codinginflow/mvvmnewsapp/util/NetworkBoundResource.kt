package com.codinginflow.mvvmnewsapp.util

import kotlinx.coroutines.flow.*

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = flow {
    val data = query().first()

    val flow = if (shouldFetch(data)) {
        emit(Resource.Loading(data))

        try {
            kotlinx.coroutines.delay(1000)
            val fetchedResult = fetch()
            saveFetchResult(fetchedResult)
            query().map { Resource.Success(it) }
        } catch (t: Throwable) {
            query().map {
                Resource.Error(t.localizedMessage ?: "An unknown error occurred", it)
            }
        }
    } else {
        query().map { Resource.Success(it) }
    }
    emitAll(flow)
}