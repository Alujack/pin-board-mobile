package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.*

interface NotificationApi {
    @POST("api/notifications/register-token")
    suspend fun registerFCMToken(
        @Body request: Map<String, String>
    ): Response<ApiResponse<RegisterTokenResponse>>

    @POST("api/notifications/remove-token")
    suspend fun removeFCMToken(): Response<ApiResponse<Unit>>

    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<NotificationListResponse>

    @POST("api/notifications/mark-read")
    suspend fun markAsRead(
        @Body request: Map<String, String>
    ): Response<ApiResponse<Unit>>

    @POST("api/notifications/mark-all-read")
    suspend fun markAllAsRead(): Response<ApiResponse<Unit>>
}

data class RegisterTokenResponse(
    val userId: String,
    val registered: Boolean
)

data class NotificationListResponse(
    val success: Boolean,
    val message: String,
    val data: List<NotificationItem>,
    val pagination: Pagination? = null
)

data class NotificationItem(
    val _id: String,
    val user: String,
    val from_user: NotificationUser?,
    val type: String,
    val content: String,
    val is_read: Boolean,
    val metadata: NotificationMetadata?,
    val created_at: String
)

data class NotificationUser(
    val _id: String,
    val username: String,
    val profile_picture: String?
)

data class NotificationMetadata(
    val pin_id: String?,
    val board_id: String?,
    val user_id: String?,
    val comment_id: String?,
    val action: String?
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)

