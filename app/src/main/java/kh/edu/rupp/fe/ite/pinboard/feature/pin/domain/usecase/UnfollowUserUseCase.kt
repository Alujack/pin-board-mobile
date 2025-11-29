package kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase

import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.UserActionResult
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.UserRepository
import javax.inject.Inject

class UnfollowUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): UserActionResult {
        return userRepository.unfollowUser(userId)
    }
}
