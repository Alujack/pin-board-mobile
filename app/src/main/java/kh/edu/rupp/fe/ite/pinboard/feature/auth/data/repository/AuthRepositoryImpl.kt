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
                if (authResponse != null && authResponse.session != null) {
                    val token = authResponse.session.sessionToken
                    val sessionId = authResponse.session.sessionId
                    
                    saveToken(token)
                    saveSessionId(sessionId)
                    
                    AuthResult.Success(
                        token = token,
                        user = authResponse.user?.toUser()
                            ?: User(id = "", username = username, password = "", null, null, null, null)
                    )
                } else {
                    AuthResult.Error("Invalid response from server")
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
                    // Registration successful — but no token, only user info
                    val user = User(
                        id = authResponse.id ?: authResponse._id.orEmpty(),
                        username = authResponse.username.orEmpty(),
                        password = "",
                        profile_picture = null,
                        is_active = authResponse.status,
                        createdAt = authResponse.createdAt,
                        updatedAt = authResponse.updatedAt
                    )

                    // ✅ Return success without token, let UI navigate to Login screen
                    AuthResult.Success(
                        token = "",
                        user = user
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

    override suspend fun saveSessionId(sessionId: String) {
        tokenManager.saveSessionId(sessionId)
    }

    override suspend fun getSessionId(): String? {
        return tokenManager.getSessionId()
    }

    override suspend fun clearSessionId() {
        tokenManager.clearSessionId()
    }

    override suspend fun clearAllTokens() {
        tokenManager.clearAllTokens()
    }
}

// Extension function
private fun kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.UserDto.toUser(): User {
    return User(
        id = id,
        username = username,
        password = "",
        role = role,
        status = status,
        profile_picture = null,
        is_active = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
