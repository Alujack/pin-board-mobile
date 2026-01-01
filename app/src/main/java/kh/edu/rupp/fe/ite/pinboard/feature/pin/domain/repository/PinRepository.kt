package kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.*
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote.*
import java.io.File

sealed class PinResult<out T> {
    data class Success<T>(val data: T) : PinResult<T>()
    data class Error(val message: String) : PinResult<Nothing>()
}

interface PinRepository {
    suspend fun getBoards(): PinResult<List<Board>>
    suspend fun getPublicBoards(page: Int = 1, limit: Int = 50): PinResult<List<Board>>
    suspend fun createBoard(
        name: String,
        description: String?,
        isPublic: Boolean
    ): PinResult<Board>
    suspend fun createPin(
        title: String,
        board: String,
        description: String,
        link: String?,
        media: List<File>
    ): PinResult<Pin>
    
    // Search
    suspend fun searchPins(query: String): PinResult<List<Pin>>
    
    // Profile-related media
    suspend fun getCreatedImages(): PinResult<List<MediaItem>>
    suspend fun getSavedMedia(): PinResult<List<MediaItem>>
    
    // Save/unsave/download
    suspend fun savePin(pinId: String): PinResult<Unit>
    suspend fun unsavePin(pinId: String): PinResult<Unit>
    suspend fun downloadPin(pinId: String): PinResult<Unit>
    suspend fun getAllPins(): PinResult<List<Pin>>
    suspend fun getPinById(pinId: String): PinResult<Pin>
    suspend fun getBoardById(boardId: String): PinResult<Board>
    suspend fun getPinsByBoard(boardId: String): PinResult<List<Pin>>
    
    // Comments
    suspend fun getComments(pinId: String, page: Int = 1, limit: Int = 20): PinResult<CommentResponse>
    suspend fun createComment(pinId: String, content: String, parentCommentId: String? = null): PinResult<Comment>
    suspend fun deleteComment(commentId: String): PinResult<Unit>
    suspend fun toggleCommentLike(commentId: String): PinResult<ToggleLikeResponse>
    
    // Pin Likes
    suspend fun togglePinLike(pinId: String): PinResult<TogglePinLikeResponse>
    suspend fun checkPinLiked(pinId: String): PinResult<Boolean>
    suspend fun getPinLikes(pinId: String, page: Int = 1): PinResult<PinLikesResponse>
    
    // Share
    suspend fun sharePin(pinId: String): PinResult<SharePinResponse>
    suspend fun generateShareLink(pinId: String): PinResult<String>
    
    // Notifications
    suspend fun getNotifications(page: Int = 1, limit: Int = 20): PinResult<NotificationListResponse>
    suspend fun markNotificationAsRead(notificationId: String): PinResult<Unit>
    suspend fun markAllNotificationsAsRead(): PinResult<Unit>
    suspend fun registerFCMToken(token: String): PinResult<Unit>
    suspend fun removeFCMToken(): PinResult<Unit>
}
