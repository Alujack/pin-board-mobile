package kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.usecase

import kh.edu.rupp.fe.ite.pinboard.core.utils.Resource
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.model.User
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Resource<User> {
        if (username.isBlank()) {
            return Resource.Error("Username cannot be empty")
        }
        if (password.isBlank()) {
            return Resource.Error("Password cannot be empty")
        }
        return repository.login(username, password)
    }
}

class RegisterUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        username: String,
        email: String,
        password: String
    ): Resource<User> {
        if (name.isBlank()) {
            return Resource.Error("Name cannot be empty")
        }
        if (username.isBlank()) {
            return Resource.Error("Username cannot be empty")
        }
        if (email.isBlank() || !email.contains("@")) {
            return Resource.Error("Please enter a valid email")
        }
        if (password.length < 6) {
            return Resource.Error("Password must be at least 6 characters")
        }
        return repository.register(name, username, email, password)
    }
}

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}

class GetAuthStateUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return repository.isAuthenticated()
    }
}

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<User?> {
        return repository.getCurrentUser()
    }
}