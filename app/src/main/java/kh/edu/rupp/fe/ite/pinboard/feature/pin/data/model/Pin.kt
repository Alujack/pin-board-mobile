package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model

data class Pin(
    val _id: String,
    val title: String,
    val description: String,
    val link: String?,
    val board: Board,
    val media: List<Media>,
    val createdAt: String,
    val updatedAt: String
)

data class Media(
    val media_url: String,
    val public_id: String,
    val format: String,
    val resource_type: String
)
