package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model

data class Pin(
    val _id: String,
    val title: String,
    val description: String,
    val link: String?,
    val board: Board,
    val imageUrl: String?,
    val videoUrl: String?,
    val createdAt: String,
    val updatedAt: String
)
