package kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository

import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.model.User

sealed class AuthResult {
    data class Success(val token: String?, val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

interface AuthRepository {
    suspend fun login(username: String, password: String): AuthResult
    suspend fun register(username: String, password: String, profilePicture: String?): AuthResult

    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
}
