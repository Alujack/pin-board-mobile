package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model

data class Board(
    val _id: String,
    val name: String,
    val description: String? = null,
    val is_public: Boolean = true,
    val status: String = "active",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val pinCount: Int = 0,
    val user: BoardUser? = null
)

data class BoardUser(
    val _id: String,
    val username: String,
    val id: String
)
