package kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.model

data class User(
    val id: String? = null,
    val username: String,
    val password: String? = null,
    val profile_picture: String? = null,
    val is_active: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val role: String? = null,   // ✅ Optional, because backend returns "role"
    val status: String? = null  // ✅ Optional alias for active state
)
