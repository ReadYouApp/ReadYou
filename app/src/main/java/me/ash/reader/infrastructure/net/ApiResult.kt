package me.ash.reader.infrastructure.net

import me.ash.reader.infrastructure.net.ApiResult.BizError
import me.ash.reader.infrastructure.net.ApiResult.NetworkError
import me.ash.reader.infrastructure.net.ApiResult.Success
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class BizError(val exception: Exception) : ApiResult<Nothing>()
    data class NetworkError(val exception: Exception) : ApiResult<Nothing>()
    data class UnknownError(val throwable: Throwable) : ApiResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
}

@OptIn(ExperimentalContracts::class)
inline fun <T> ApiResult<T>.onSuccess(block: (T) -> Unit): ApiResult<T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    if (this is Success) {
        block(data)
    }
    return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> ApiResult<T>.onFailure(block: (Throwable) -> Unit): ApiResult<T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    when (this) {
        is BizError -> block(exception)
        is NetworkError -> block(exception)
        is ApiResult.UnknownError -> block(throwable)
        else -> {}
    }
    return this
}

inline fun <reified T> ApiResult<T>.getOrThrow(): T {
    return when (this) {
        is Success -> data
        is BizError -> throw exception
        is NetworkError -> throw exception
        is ApiResult.UnknownError -> throw throwable
    }
}

