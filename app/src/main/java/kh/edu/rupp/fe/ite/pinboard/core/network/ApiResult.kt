package kh.edu.rupp.fe.ite.pinboard.core.network

/**
 * A sealed class representing the result of an API call.
 *
 * This class is designed to encapsulate both successful and failed network responses,
 * providing a type-safe way to handle API outcomes.
 *
 * @param T The type of the data expected in a successful response.
 */
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val exception: Throwable) : ApiResult<Nothing>()
}