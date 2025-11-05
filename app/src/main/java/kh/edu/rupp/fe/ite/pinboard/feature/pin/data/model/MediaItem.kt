package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model

data class MediaItem(
    val media_url: String?,
    val thumbnail_url: String?,
    val public_id: String?,
    val format: String?,
    val resource_type: String?,
    val pinId: String?
)

data class ApiListResponse<T>(
    val success: Boolean?,
    val message: String?,
    val data: List<T>?
)


