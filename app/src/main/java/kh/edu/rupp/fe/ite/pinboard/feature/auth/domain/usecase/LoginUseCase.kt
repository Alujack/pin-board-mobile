package kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.usecase

import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.AuthRepository
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.AuthResult

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): AuthResult {
        return repository.login(username, password)
    }
}
