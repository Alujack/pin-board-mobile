package kh.edu.rupp.fe.ite.pinboard.core.network

import retrofit2.Response

object NetworkUtils {

    /**
     * Wraps a Retrofit API call in a try-catch block to safely handle network requests.
     * It returns a sealed class `ApiResult` which represents either a success or an error state.
     *
     * @param T The type of the successful response body.
     * @param apiCall A suspend lambda function that executes the Retrofit API call and returns a `Response<T>`.
     * @return [ApiResult.Success] containing the response body `T` if the call is successful and the body is not null.
     *         [ApiResult.Error] containing an exception in case of a failed request, a null body, or any other network-related exception.
     */
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error(NullPointerException("Response body is null"))
                }
            } else {
                ApiResult.Error(Exception("HTTP ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }
}