package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, val name: String)
data class AuthResponse(val token: String?, val message: String?)

interface AuthApi {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
}
