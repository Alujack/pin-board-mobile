package kh.edu.rupp.fe.ite.pinboard.feature.pin.data

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.google.gson.JsonSyntaxException
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote.PinApi
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.MediaItem
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class PinRepositoryImpl @Inject constructor(
    private val api: PinApi,
    private val appContext: Context
) : PinRepository {

    override suspend fun getBoards(): PinResult<List<Board>> = runCatching {
        val resp = api.getBoards()
        val list = resp.data ?: emptyList()
        PinResult.Success(list)
    }.getOrElse { e -> PinResult.Error(e.toReadableMessage()) }

    override suspend fun createPin(
        title: String,
        board: String,
        description: String,
        link: String?,
        media: List<File>
    ): PinResult<Pin> = withContext(Dispatchers.IO) {
        try {
            val textMime = "text/plain".toMediaType()
            val titleRb = title.toRequestBody(textMime)
            val boardRb = board.toRequestBody(textMime)
            val descRb = description.toRequestBody(textMime)
            val linkRb = link?.takeIf { it.isNotBlank() }?.toRequestBody(textMime)

            val parts = media.map { file ->
                val mime = when {
                    file.name.endsWith(".png", true) -> "image/png"
                    file.name.endsWith(".webp", true) -> "image/webp"
                    file.name.endsWith(".gif", true) -> "image/gif"
                    else -> "image/jpeg"
                }.toMediaType()
                val body = file.asRequestBody(mime)
                MultipartBody.Part.createFormData("media", file.name, body)
            }

            val created = api.createPin(
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

    override suspend fun getCreatedImages(): PinResult<List<MediaItem>> = runCatching {
        val resp = api.getCreatedImages()
        val list = resp.data ?: emptyList()
        PinResult.Success(list)
    }.getOrElse { e -> PinResult.Error(e.toReadableMessage()) }

    override suspend fun getSavedMedia(): PinResult<List<MediaItem>> = runCatching {
        val resp = api.getSavedMedia()
        val list = resp.data ?: emptyList()
        PinResult.Success(list)
    }.getOrElse { e -> PinResult.Error(e.toReadableMessage()) }

    override suspend fun savePin(pinId: String): PinResult<Unit> = runCatching {
        val resp = api.savePin(pinId)
        if (resp.isSuccessful) PinResult.Success(Unit) else PinResult.Error("Save failed: ${resp.code()}")
    }.getOrElse { e -> PinResult.Error(e.toReadableMessage()) }

    override suspend fun unsavePin(pinId: String): PinResult<Unit> = runCatching {
        val resp = api.unsavePin(pinId)
        if (resp.isSuccessful) PinResult.Success(Unit) else PinResult.Error("Unsave failed: ${resp.code()}")
    }.getOrElse { e -> PinResult.Error(e.toReadableMessage()) }

    override suspend fun downloadPin(pinId: String): PinResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.downloadMedia(pinId)
            if (!response.isSuccessful || response.body() == null) {
                return@withContext PinResult.Error("Download failed: ${response.code()}")
            }

            val body = response.body()!!
            val mime = body.contentType()?.toString() ?: "image/jpeg"
            val ext = when {
                mime.contains("png") -> "png"
                mime.contains("webp") -> "webp"
                else -> "jpg"
            }

            val filename = "pin_${timestamp()}.$ext"
            val resolver = appContext.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PinBoard")
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext PinResult.Error("Failed to create media entry")

            resolver.openOutputStream(uri)?.use { out ->
                body.byteStream().use { input -> input.copyTo(out) }
            } ?: return@withContext PinResult.Error("Failed to open output stream")

            PinResult.Success(Unit)
        } catch (e: Exception) {
            PinResult.Error(e.toReadableMessage())
        }
    }

    private fun Throwable.toReadableMessage(): String = when (this) {
        is HttpException -> "Network error ${code()}"
        is IOException -> "Connection error"
        else -> message ?: "Unknown error"
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}