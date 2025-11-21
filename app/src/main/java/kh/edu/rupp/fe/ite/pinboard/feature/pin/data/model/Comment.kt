package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model

data class Comment(
    val _id: String,
    val pin: String,
    val user: CommentUser,
    val content: String,
    val parent_comment: String? = null,
    val likes: List<String> = emptyList(),
    val likesCount: Int = 0,
    val repliesCount: Int = 0,
    val isLiked: Boolean = false,
    val is_deleted: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)

data class CommentUser(
    val _id: String,
    val username: String,
    val profile_picture: String? = null,
    val full_name: String? = null,
    val bio: String? = null
)

data class CommentResponse(
    val success: Boolean,
    val message: String,
    val data: List<Comment>,
    val pagination: Pagination? = null
)

data class CreateCommentRequest(
    val content: String,
    val parent_comment: String? = null
)

data class CreateCommentResponse(
    val success: Boolean,
    val message: String,
    val data: Comment
)

data class ToggleLikeResponse(
    val success: Boolean,
    val message: String,
    val isLiked: Boolean
)

