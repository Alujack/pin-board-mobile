package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T,
    val pagination: Pagination? = null
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)
