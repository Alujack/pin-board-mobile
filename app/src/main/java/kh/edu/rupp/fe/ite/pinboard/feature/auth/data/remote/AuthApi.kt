// feature/auth/data/remote/AuthApi.kt
package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

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
