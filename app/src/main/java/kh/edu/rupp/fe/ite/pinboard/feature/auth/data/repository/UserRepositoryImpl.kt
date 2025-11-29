package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.repository

import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.AuthApi
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.FollowUserRequest
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.UserActionResult
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : UserRepository {

    override suspend fun followUser(userId: String): UserActionResult {
        return try {
            val response = authApi.followUser(FollowUserRequest(userId))
            if (response.isSuccessful && (response.body()?.success == true)) {
                UserActionResult.Success
            } else {
                UserActionResult.Error(response.body()?.message ?: "Failed to follow user")
            }
        } catch (e: Exception) {
            UserActionResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun unfollowUser(userId: String): UserActionResult {
        return try {
            val response = authApi.unfollowUser(FollowUserRequest(userId))
            if (response.isSuccessful && (response.body()?.success == true)) {
                UserActionResult.Success
            } else {
                UserActionResult.Error(response.body()?.message ?: "Failed to unfollow user")
            }
        } catch (e: Exception) {
            UserActionResult.Error(e.message ?: "Network error")
        }
    }
}
