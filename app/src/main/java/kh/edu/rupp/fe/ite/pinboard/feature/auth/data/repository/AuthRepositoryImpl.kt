package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.repository

import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.local.TokenManager
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.AuthApi
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.LoginRequest
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.RegisterRequest
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.model.User
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.AuthRepository
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.AuthResult
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(username: String, password: String): AuthResult {
        return try {
            val response = authApi.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    saveToken(authResponse.token)
                    AuthResult.Success(
                        token = authResponse.token,
                        user = authResponse.user.toUser()
                    )
                } else {
                    AuthResult.Error("Empty response from server")
                }
            } else {
                AuthResult.Error(response.message() ?: "Login failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error occurred")
        }
    }

    override suspend fun register(username: String, password: String, profilePicture: String?): AuthResult {
        return try {
            val response = authApi.register(RegisterRequest(username, password, profilePicture))
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    saveToken(authResponse.token)
                    AuthResult.Success(
                        token = authResponse.token,
                        user = authResponse.user.toUser()
                    )
                } else {
                    AuthResult.Error("Empty response from server")
                }
            } else {
                AuthResult.Error(response.message() ?: "Registration failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error occurred")
        }
    }

    override suspend fun saveToken(token: String) {
        tokenManager.saveToken(token)
    }

    override suspend fun getToken(): String? {
        return tokenManager.getToken()
    }

    override suspend fun clearToken() {
        tokenManager.clearToken()
    }
}

// Extension function
private fun kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.UserDto.toUser(): User {
    return User(
        id = id,
        username = username,
        password = "",
        profile_picture = profile_picture,
        is_active = is_active,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
