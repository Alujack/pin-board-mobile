package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model

import com.google.gson.annotations.SerializedName

// ============================================
// UPDATED Pin model with all API fields
// ============================================
data class Pin(
    @SerializedName("_id")
    val _id: String?,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("link_url")
    val link: String?,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("media")
    val media: List<Media>? = null,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?,
    
    @SerializedName("likesCount")
    val likesCount: Int = 0,
    
    @SerializedName("isLiked")
    val isLiked: Boolean = false,

    @SerializedName("id")
    val id: String? = null
) {
    // Computed properties to extract URLs from media array
    val imageUrl: String?
        get() = media?.firstOrNull { it.resourceType == "image" }?.mediaUrl

    val videoUrl: String?
        get() = media?.firstOrNull { it.resourceType == "video" }?.mediaUrl

    // Get first media URL regardless of type
    val firstMediaUrl: String?
        get() = media?.firstOrNull()?.mediaUrl
}

// ============================================
// API Response wrapper
// ============================================
data class PinResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<Pin>
)

// For single pin endpoints
data class SinglePinResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: Pin
)

// ============================================
// Supporting models
// ============================================



data class User(
    @SerializedName("_id")
    val _id: String?,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("id")
    val id: String? = null
)

data class Media(
    @SerializedName("media_url")
    val mediaUrl: String,

    @SerializedName("public_id")
    val publicId: String?,

    @SerializedName("format")
    val format: String?,

    @SerializedName("resource_type")
    val resourceType: String? // "image" or "video"
)

// ============================================
// Extension functions for easy conversion
// ============================================

// Convert API response to Pin list
fun PinResponse.toPinList(): List<Pin> = this.data

// Check if Pin has valid media
fun Pin.hasMedia(): Boolean = !media.isNullOrEmpty()

// Get all image URLs from a Pin
fun Pin.getAllImageUrls(): List<String> {
    return media?.filter { it.resourceType == "image" }
        ?.mapNotNull { it.mediaUrl }
        ?: emptyList()
}

// Get all video URLs from a Pin
fun Pin.getAllVideoUrls(): List<String> {
    return media?.filter { it.resourceType == "video" }
        ?.mapNotNull { it.mediaUrl }
        ?: emptyList()
}