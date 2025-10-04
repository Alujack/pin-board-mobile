package kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository

import kh.edu.rupp.fe.ite.pinboard.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Resource<kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.model.User>
    suspend fun register(name: String, username: String, email: String, password: String): Resource<kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.model.User>
    suspend fun refreshToken(): Resource<Unit>
    suspend fun logout()
    fun getAuthToken(): Flow<String?>
    fun isAuthenticated(): Flow<Boolean>
    fun getCurrentUser(): Flow<kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.model.User?>
}