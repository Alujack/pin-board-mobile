package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote

import retrofit2.Response
import retrofit2.http.*

interface ShareApi {
    @POST("api/share/sharePin")
    suspend fun sharePin(
        @Body request: Map<String, String>
    ): Response<SharePinResponse>

    @GET("api/share/getShareCount")
    suspend fun getShareCount(
        @Query("pinId") pinId: String
    ): Response<ShareCountResponse>

    @GET("api/share/generateShareLink")
    suspend fun generateShareLink(
        @Query("pinId") pinId: String
    ): Response<ShareLinkResponse>
}

data class SharePinResponse(
    val success: Boolean,
    val message: String,
    val shareCount: Int
)

data class ShareCountResponse(
    val success: Boolean,
    val message: String,
    val shareCount: Int
)

data class ShareLinkResponse(
    val success: Boolean,
    val message: String,
    val shareUrl: String
)

