package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface CommentApi {
    @GET("comment/getComments")
    suspend fun getComments(
        @Query("pinId") pinId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String = "newest"
    ): Response<CommentResponse>

    @POST("comment/createComment")
    suspend fun createComment(
        @Body request: Map<String, Any>
    ): Response<CreateCommentResponse>

    @PUT("comment/updateComment")
    suspend fun updateComment(
        @Body request: Map<String, Any>
    ): Response<CreateCommentResponse>

    @DELETE("comment/deleteComment")
    suspend fun deleteComment(
        @Query("commentId") commentId: String
    ): Response<ApiResponse<Unit>>

    @POST("comment/toggleCommentLike")
    suspend fun toggleCommentLike(
        @Body request: Map<String, String>
    ): Response<ToggleLikeResponse>

    @GET("comment/getComments")
    suspend fun getReplies(
        @Query("pinId") pinId: String,
        @Query("parent_comment") parentCommentId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<CommentResponse>
}

