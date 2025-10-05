package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.repository

import  kh.edu.rupp.fe.ite.pinboard.core.utils.Resource
import  kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.AuthApi
import  kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.model.User
import  kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val api: AuthApi
): AuthRepository {

    override suspend fun login(email: String, password: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.login( kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.LoginRequest(email, password))
            Resource.Success(User(token = response.token ?: "", message = response.message ?: ""))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun register(name: String, email: String, password: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.register( kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.RegisterRequest(email, password, name))
            Resource.Success(User(token = response.token ?: "", message = response.message ?: ""))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}
