package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote

import com.google.gson.annotations.SerializedName

data class CreateBoardRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("is_public")
    val is_public: Boolean = true
)

