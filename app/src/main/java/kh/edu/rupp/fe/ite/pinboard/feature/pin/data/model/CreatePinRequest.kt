package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model

data class CreatePinRequest(
    val title: String,
    val board: String,
    val description: String,
    val link: String? = null
)
