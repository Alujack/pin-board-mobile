package kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository

sealed class UserActionResult {
    object Success : UserActionResult()
    data class Error(val message: String) : UserActionResult()
}

interface UserRepository {
    suspend fun followUser(userId: String): UserActionResult
    suspend fun unfollowUser(userId: String): UserActionResult
}
