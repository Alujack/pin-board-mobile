package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.*
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote.*
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class PinRepositoryImpl
@Inject
constructor(
        private val api: PinApi,
        private val commentApi: CommentApi,
        private val pinLikeApi: PinLikeApi,
        private val shareApi: ShareApi,
        private val notificationApi: NotificationApi,
        private val appContext: Context
) : PinRepository {

    override suspend fun getBoards(): PinResult<List<Board>> =
            runCatching {
                val resp = api.getBoards()
                val list = resp.data ?: emptyList()
                PinResult.Success(list)
            }
                    .getOrElse { e -> PinResult.Error(e.toReadableMessage()) }

    override suspend fun createPin(
            title: String,
            board: String,
            description: String,
            link: String?,
            media: List<File>
    ): PinResult<Pin> =
            withContext(Dispatchers.IO) {
                try {
                    val textMime = "text/plain".toMediaType()
                    val titleRb = title.toRequestBody(textMime)
                    val boardRb = board.toRequestBody(textMime)
                    val descRb = description.toRequestBody(textMime)
                    val linkRb = link?.takeIf { it.isNotBlank() }?.toRequestBody(textMime)

                    val parts =
                            media.map { file ->
                                val mime =
                                        when {
                                            file.name.endsWith(".png", true) -> "image/png"
                                            file.name.endsWith(".webp", true) -> "image/webp"
                                            file.name.endsWith(".gif", true) -> "image/gif"
                                            else -> "image/jpeg"
                                        }.toMediaType()
                                val body = file.asRequestBody(mime)
                                MultipartBody.Part.createFormData("media", file.name, body)
                            }

                    val created =
                            api.createPin(
                                    title = titleRb,
                                    board = boardRb,
                                    description = descRb,
                                    link = linkRb,
                                    media = parts
                            )
                    PinResult.Success(created)
                } catch (e: Throwable) {
                    PinResult.Error(e.toReadableMessage())
                }
            }

    override suspend fun searchPins(query: String): PinResult<List<Pin>> {
        return try {
            val response = api.searchPins(query)

            if (response.success) {
                PinResult.Success(response.data)
            } else {
                PinResult.Error(response.message)
            }
        } catch (e: HttpException) {
            PinResult.Error("Network error: ${e.code()} ${e.message()}")
        } catch (e: JsonSyntaxException) {
            PinResult.Error("Invalid response format: ${e.message}")
        } catch (e: Exception) {
            PinResult.Error(e.message ?: "Search failed")
        }
    }

    override suspend fun getAllPins(): PinResult<List<Pin>> {
        return try {
            val response = api.getAllPins()

            if (response.success) {
                PinResult.Success(response.data)
            } else {
                PinResult.Error(response.message)
            }
        } catch (e: HttpException) {
            PinResult.Error("Network error: ${e.code()} ${e.message()}")
        } catch (e: Exception) {
            PinResult.Error(e.message ?: "Failed to fetch pins")
        }
    }

    override suspend fun getCreatedImages(): PinResult<List<MediaItem>> =
            runCatching {
                val resp = api.getCreatedImages()
                val list = resp.data ?: emptyList()
                PinResult.Success(list)
            }
                    .getOrElse { e -> PinResult.Error(e.toReadableMessage()) }

    override suspend fun getSavedMedia(): PinResult<List<MediaItem>> =
            runCatching {
                val resp = api.getSavedMedia()
                val list = resp.data ?: emptyList()
                PinResult.Success(list)
            }
                    .getOrElse { e -> PinResult.Error(e.toReadableMessage()) }

    override suspend fun savePin(pinId: String): PinResult<Unit> =
            runCatching {
                val resp = api.savePin(pinId)
                if (resp.isSuccessful) PinResult.Success(Unit)
                else PinResult.Error("Save failed: ${resp.code()}")
            }
                    .getOrElse { e -> PinResult.Error(e.toReadableMessage()) }

    override suspend fun unsavePin(pinId: String): PinResult<Unit> =
            runCatching {
                val resp = api.unsavePin(pinId)
                if (resp.isSuccessful) PinResult.Success(Unit)
                else PinResult.Error("Unsave failed: ${resp.code()}")
            }
                    .getOrElse { e -> PinResult.Error(e.toReadableMessage()) }

    override suspend fun downloadPin(pinId: String): PinResult<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    val response = api.downloadMedia(pinId)
                    if (!response.isSuccessful || response.body() == null) {
                        return@withContext PinResult.Error("Download failed: ${response.code()}")
                    }

                    val body = response.body()!!
                    val mime = body.contentType()?.toString() ?: "image/jpeg"
                    val ext =
                            when {
                                mime.contains("png") -> "png"
                                mime.contains("webp") -> "webp"
                                else -> "jpg"
                            }

                    val filename = "pin_${timestamp()}.$ext"
                    val resolver = appContext.contentResolver
                    val contentValues =
                            ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                                put(MediaStore.MediaColumns.MIME_TYPE, mime)
                                put(
                                        MediaStore.MediaColumns.RELATIVE_PATH,
                                        Environment.DIRECTORY_PICTURES + "/PinBoard"
                                )
                            }

                    val uri =
                            resolver.insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    contentValues
                            )
                                    ?: return@withContext PinResult.Error(
                                            "Failed to create media entry"
                                    )

                    resolver.openOutputStream(uri)?.use { out ->
                        body.byteStream().use { input -> input.copyTo(out) }
                    }
                            ?: return@withContext PinResult.Error("Failed to open output stream")

                    PinResult.Success(Unit)
                } catch (e: Exception) {
                    PinResult.Error(e.toReadableMessage())
                }
            }

    private fun Throwable.toReadableMessage(): String =
            when (this) {
                is HttpException -> "Network error ${code()}"
                is IOException -> "Connection error"
                else -> message ?: "Unknown error"
            }

    override suspend fun getPinById(pinId: String): PinResult<Pin> {
        return try {
            val response = api.getPinById(pinId)

            if (response.success) {
                PinResult.Success(response.data)
            } else {
                PinResult.Error(response.message)
            }
        } catch (e: HttpException) {
            PinResult.Error("Network error: ${e.code()} ${e.message()}")
        } catch (e: JsonSyntaxException) {
            PinResult.Error("Invalid response format: ${e.message}")
        } catch (e: Exception) {
            PinResult.Error(e.message ?: "Failed to fetch pin details")
        }
    }

    override suspend fun getBoardById(boardId: String): PinResult<Board> {
        return try {
            val board = api.getBoardById(boardId)
            PinResult.Success(board)
        } catch (e: HttpException) {
            PinResult.Error("Network error: ${e.code()} ${e.message()}")
        } catch (e: Exception) {
            PinResult.Error(e.message ?: "Failed to fetch board details")
        }
    }

    override suspend fun getPinsByBoard(boardId: String): PinResult<List<Pin>> {
        return try {
            val response = api.getPinsByBoard(boardId)
            if (response.success) {
                PinResult.Success(response.data)
            } else {
                PinResult.Error(response.message)
            }
        } catch (e: HttpException) {
            PinResult.Error("Network error: ${e.code()} ${e.message()}")
        } catch (e: Exception) {
            PinResult.Error(e.message ?: "Failed to fetch board pins")
        }
    }

    private fun timestamp(): String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    // Comments
    override suspend fun getComments(
            pinId: String,
            page: Int,
            limit: Int
    ): PinResult<CommentResponse> {
        return try {
            val response = commentApi.getComments(pinId, page, limit)
            if (response.isSuccessful && response.body() != null) {
                PinResult.Success(response.body()!!)
            } else {
                PinResult.Error("Failed to fetch comments: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun createComment(
            pinId: String,
            content: String,
            parentCommentId: String?
    ): PinResult<Comment> {
        return try {
            val request =
                    CreateCommentRequest(
                            pinId = pinId,
                            body = CommentBody(content = content, parent_comment = parentCommentId)
                    )

            val response = commentApi.createComment(request)
            if (response.isSuccessful && response.body() != null) {
                PinResult.Success(response.body()!!.data)
            } else {
                PinResult.Error("Failed to create comment: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun deleteComment(commentId: String): PinResult<Unit> {
        return try {
            val response = commentApi.deleteComment(commentId)
            if (response.isSuccessful) {
                PinResult.Success(Unit)
            } else {
                PinResult.Error("Failed to delete comment: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun toggleCommentLike(commentId: String): PinResult<ToggleLikeResponse> {
        return try {
            val request = ToggleCommentLikeRequest(commentId = commentId)
            val response = commentApi.toggleCommentLike(request)
            if (response.isSuccessful && response.body() != null) {
                PinResult.Success(response.body()!!)
            } else {
                PinResult.Error("Failed to toggle comment like: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error(e.toReadableMessage())
        }
    }

    // Pin Likes
    override suspend fun togglePinLike(pinId: String): PinResult<TogglePinLikeResponse> {
        return try {
            val response = pinLikeApi.togglePinLike(mapOf("pinId" to pinId))
            if (response.isSuccessful && response.body() != null) {
                PinResult.Success(response.body()!!)
            } else {
                PinResult.Error("Failed to toggle pin like: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun checkPinLiked(pinId: String): PinResult<Boolean> {
        return try {
            val response = pinLikeApi.checkPinLiked(pinId)
            if (response.isSuccessful && response.body() != null) {
                PinResult.Success(response.body()!!.isLiked)
            } else {
                PinResult.Error("Failed to check pin liked: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun getPinLikes(pinId: String, page: Int): PinResult<PinLikesResponse> {
        return try {
            val response = pinLikeApi.getPinLikes(pinId, page)
            if (response.isSuccessful && response.body() != null) {
                PinResult.Success(response.body()!!)
            } else {
                PinResult.Error("Failed to fetch pin likes: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error(e.toReadableMessage())
        }
    }

    // Share
    override suspend fun sharePin(pinId: String): PinResult<SharePinResponse> {
        return try {
            val response = shareApi.sharePin(mapOf("pinId" to pinId))
            if (response.isSuccessful && response.body() != null) {
                PinResult.Success(response.body()!!)
            } else {
                PinResult.Error("Failed to share pin: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun generateShareLink(pinId: String): PinResult<String> {
        return try {
            val response = shareApi.generateShareLink(pinId)
            if (response.isSuccessful && response.body() != null) {
                PinResult.Success(response.body()!!.shareUrl)
            } else {
                PinResult.Error("Failed to generate share link: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error(e.toReadableMessage())
        }
    }

    // Notifications
    override suspend fun getNotifications(
            page: Int,
            limit: Int
    ): PinResult<NotificationListResponse> {
        return try {
            val response = notificationApi.getNotifications(page, limit)
            if (response.isSuccessful && response.body() != null) {
                PinResult.Success(response.body()!!)
            } else {
                PinResult.Error("Failed to fetch notifications: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun markNotificationAsRead(notificationId: String): PinResult<Unit> {
        return try {
            val response = notificationApi.markAsRead(mapOf("notificationId" to notificationId))
            if (response.isSuccessful) {
                PinResult.Success(Unit)
            } else {
                PinResult.Error("Failed to mark notification as read: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun markAllNotificationsAsRead(): PinResult<Unit> {
        return try {
            val response = notificationApi.markAllAsRead()
            if (response.isSuccessful) {
                PinResult.Success(Unit)
            } else {
                PinResult.Error("Failed to mark all notifications as read: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun registerFCMToken(token: String): PinResult<Unit> {
        return try {
            val response = notificationApi.registerFCMToken(mapOf("fcm_token" to token))
            if (response.isSuccessful) {
                PinResult.Success(Unit)
            } else {
                PinResult.Error("Failed to register FCM token: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error(e.toReadableMessage())
        }
    }
}
