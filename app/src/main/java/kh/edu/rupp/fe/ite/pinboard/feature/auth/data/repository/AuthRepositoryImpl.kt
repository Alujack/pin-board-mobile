package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.repository

import kh.edu.rupp.fe.ite.pinboard.core.utils.Resource
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.local.TokenManager
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.local.UserDao
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.mapper.toDomain
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.mapper.toEntity
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.AuthApiService
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.LoginRequest
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.RefreshTokenRequest
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.RegisterRequest
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.model.User
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService,
    private val tokenManager: TokenManager,
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun login(username: String, password: String): Resource<User> {
        return try {
            val response = api.login(LoginRequest(username, password))

            // Save tokens
            tokenManager.saveTokens(response.token, response.refreshToken)

            // Save user to local database
            userDao.insertUser(response.user.toEntity())

            Resource.Success(response.user.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun register(
        name: String,
        username: String,
        email: String,
        password: String
    ): Resource<User> {
        return try {
            val response = api.register(
                RegisterRequest(name, username, email, password)
            )

            // Save tokens
            tokenManager.saveTokens(response.token, response.refreshToken)

            // Save user to local database
            userDao.insertUser(response.user.toEntity())

            Resource.Success(response.user.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    override suspend fun refreshToken(): Resource<Unit> {
        return try {
            val currentRefreshToken = tokenManager.refreshToken.first()
                ?: return Resource.Error("No refresh token available")

            val response = api.refreshToken(RefreshTokenRequest(currentRefreshToken))

            // Update tokens
            tokenManager.saveTokens(response.token, response.refreshToken)

            Resource.Success(Unit)
        } catch (e: Exception) {
            // If refresh fails, clear everything
            clearAuth()
            Resource.Error(e.message ?: "Token refresh failed")
        }
    }

    override suspend fun logout() {
        clearAuth()
    }

    override fun getAuthToken(): Flow<String?> {
        return tokenManager.accessToken
    }

    override fun isAuthenticated(): Flow<Boolean> {
        return tokenManager.accessToken.map { it != null }
    }

    override fun getCurrentUser(): Flow<User?> {
        return userDao.getCurrentUser().map { it?.toDomain() }
    }

    private suspend fun clearAuth() {
        tokenManager.clearTokens()
        userDao.clearUser()
    }
}