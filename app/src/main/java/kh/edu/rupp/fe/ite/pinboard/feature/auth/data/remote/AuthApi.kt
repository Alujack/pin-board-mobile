// feature/auth/data/remote/AuthApi.kt
package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    // Current authenticated user
    @GET("api/auth/me")
    suspend fun me(): MeResponse
    
    // Get user profile with stats
    @GET("api/users/me")
    suspend fun getCurrentUserProfile(): Response<UserProfileResponse>

    // Follow / Unfollow another user
    // Backend routes: /follow/followUser and /follow/unfollowUser
    @POST("api/follow/followUser")
    suspend fun followUser(@Body request: FollowUserRequest): Response<FollowActionResponse>

    @POST("api/follow/unfollowUser")
    suspend fun unfollowUser(@Body request: FollowUserRequest): Response<FollowActionResponse>

    // Get followers of a user
    // GET /follow/getFollowers?userId=...&page=...&limit=...
    @GET("api/follow/getFollowers")
    suspend fun getFollowers(
        @Query("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<FollowListResponse>

    // Get users that a user is following
    // GET /follow/getFollowing?userId=...&page=...&limit=...
    @GET("api/follow/getFollowing")
    suspend fun getFollowing(
        @Query("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<FollowListResponse>

    // Check if current user is following another user
    // GET /follow/checkFollowing?userId=...
    @GET("api/follow/checkFollowing")
    suspend fun checkFollowing(
        @Query("userId") userId: String
    ): Response<CheckFollowingResponse>

    // Get suggested users to follow
    // GET /follow/getSuggestedUsers?limit=...
    @GET("api/follow/getSuggestedUsers")
    suspend fun getSuggestedUsers(
        @Query("limit") limit: Int = 10
    ): Response<FollowListResponse>
}

// Request models
data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val profile_picture: String? = null
)

data class RefreshTokenRequest(
    val sessionId: String
)

data class RefreshTokenResponse(
    val sessionId: String,
    val sessionToken: String,
    val expiresAt: String
)

// AuthResponse.kt
// AuthResponse.kt
data class AuthResponse(
    val user: UserDto? = null,
    val session: SessionDto? = null,

    // for register response
    val _id: String? = null,
    val username: String? = null,
    val role: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val id: String? = null
)

data class SessionDto(
    val sessionId: String,
    val sessionToken: String,
    val expiredAt: String
)

data class UserDto(
    val _id: String,
    val username: String,
    val password: String?,
    val role: String?,
    val status: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val id: String?
)

// /auth/me response model
data class MeResponse(
    val _id: String,
    val username: String,
    val role: String?,
    val status: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val __v: Int?,
    val saved_pins: List<String>?,
    val is_active: String?,
    val id: String?
)

// User profile with stats
data class UserProfileResponse(
    val success: Boolean,
    val message: String,
    val data: UserProfileData
)

data class UserProfileData(
    val _id: String,
    val username: String,
    val full_name: String?,
    val bio: String?,
    val profile_picture: String?,
    val website: String?,
    val location: String?,
    val followersCount: Int,
    val followingCount: Int,
    val pinsCount: Int,
    val isFollowing: Boolean
)

// Follow/unfollow request body
data class FollowUserRequest(
    val userId: String
)

// Generic follow/unfollow response
data class FollowActionResponse(
    val success: Boolean,
    val message: String
)

// List response for followers / following / suggested users
data class FollowListResponse(
    val success: Boolean,
    val message: String,
    val data: List<UserProfileData>
)

// Response for checkFollowing endpoint
data class CheckFollowingResponse(
    val success: Boolean,
    val message: String,
    val data: CheckFollowingData
)

data class CheckFollowingData(
    val following: Boolean,
    val status: String?
)
