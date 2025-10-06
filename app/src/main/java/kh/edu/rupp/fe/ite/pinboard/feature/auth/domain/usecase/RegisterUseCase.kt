package kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.usecase

import kh.edu.rupp.fe.ite.pinboard.core.utils.Resource
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.model.User
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.AuthRepository
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.AuthResult

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(name: String, email: String, password: String): AuthResult {
        return repository.register(name, email, password)
    }
}
