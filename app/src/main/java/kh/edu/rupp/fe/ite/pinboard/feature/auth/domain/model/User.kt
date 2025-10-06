package kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.model

data class User(
    val id: String,
    val username: String,
    val password: String,
    val profile_picture: String?,
    val is_active: String?,
    val createdAt: String?,
    val updatedAt: String?
)