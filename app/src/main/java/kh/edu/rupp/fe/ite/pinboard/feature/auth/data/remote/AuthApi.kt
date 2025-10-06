// feature/auth/data/remote/AuthApi.kt
package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
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

// Response model
data class AuthResponse(
    val token: String,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val username: String,
    val profile_picture: String?,
    val is_active: String?,
    val createdAt: String?,
    val updatedAt: String?
)