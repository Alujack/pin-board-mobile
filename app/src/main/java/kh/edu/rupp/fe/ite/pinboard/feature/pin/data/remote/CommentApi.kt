package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface CommentApi {
    @GET("api/comment/getComments")
    suspend fun getComments(
        @Query("pinId") pinId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String = "newest"
    ): Response<CommentResponse>

    @POST("api/comment/createComment")
    suspend fun createComment(
        @Body request: CreateCommentRequest
    ): Response<CreateCommentResponse>

    @PUT("api/comment/updateComment")
    suspend fun updateComment(
        @Body request: UpdateCommentRequest
    ): Response<CreateCommentResponse>

    @DELETE("api/comment/deleteComment")
    suspend fun deleteComment(
        @Query("commentId") commentId: String
    ): Response<ApiResponse<Unit>>

    @POST("api/comment/toggleCommentLike")
    suspend fun toggleCommentLike(
        @Body request: ToggleCommentLikeRequest
    ): Response<ToggleLikeResponse>

    @GET("api/comment/getComments")
    suspend fun getReplies(
        @Query("pinId") pinId: String,
        @Query("parent_comment") parentCommentId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<CommentResponse>
}

// Request models
data class CreateCommentRequest(
    val pinId: String,
    val body: CommentBody
)

data class CommentBody(
    val content: String,
    val parent_comment: String? = null
)

data class UpdateCommentRequest(
    val commentId: String,
    val body: CommentUpdateBody
)

data class CommentUpdateBody(
    val content: String
)

data class ToggleCommentLikeRequest(
    val commentId: String
)

