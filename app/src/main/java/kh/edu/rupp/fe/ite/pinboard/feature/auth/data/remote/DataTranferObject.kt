package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class AuthResponse(
    val user: UserDto,
    val token: String,
    val refreshToken: String
)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val username: String,
    val email: String
)

@Serializable
data class RefreshTokenResponse(
    val token: String,
    val refreshToken: String
)