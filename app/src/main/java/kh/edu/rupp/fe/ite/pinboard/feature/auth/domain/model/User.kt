package kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.model

data class User(
    val id: String,
    val name: String,
    val username: String,
    val email: String
)

data class AuthToken(
    val accessToken: String,
    val refreshToken: String
)