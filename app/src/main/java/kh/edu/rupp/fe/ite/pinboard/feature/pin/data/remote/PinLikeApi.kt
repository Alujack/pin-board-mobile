package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote

import retrofit2.Response
import retrofit2.http.*

interface PinLikeApi {
    @POST("api/pinLike/togglePinLike")
    suspend fun togglePinLike(
        @Body request: Map<String, String>
    ): Response<TogglePinLikeResponse>

    @GET("api/pinLike/getPinLikes")
    suspend fun getPinLikes(
        @Query("pinId") pinId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PinLikesResponse>

    @GET("api/pinLike/checkPinLiked")
    suspend fun checkPinLiked(
        @Query("pinId") pinId: String
    ): Response<CheckPinLikedResponse>
}

data class TogglePinLikeResponse(
    val success: Boolean,
    val message: String,
    val isLiked: Boolean,
    val likesCount: Int
)

data class PinLikesResponse(
    val success: Boolean,
    val message: String,
    val data: List<PinLikeUser>,
    val pagination: PinLikePagination?
)

data class PinLikeUser(
    val _id: String,
    val username: String,
    val profile_picture: String?
)

data class PinLikePagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)

data class CheckPinLikedResponse(
    val success: Boolean,
    val message: String,
    val isLiked: Boolean
)

