package kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository

import kh.edu.rupp.fe.ite.pinboard.core.utils.Resource
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun register(name: String, email: String, password: String): Resource<User>
}
