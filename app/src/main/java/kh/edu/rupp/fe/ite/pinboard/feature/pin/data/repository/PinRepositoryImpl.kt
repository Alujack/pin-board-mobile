package kh.edu.rupp.fe.ite.pinboard.feature.pin.data

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
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

    override suspend fun getBoards(): PinResult<List<Board>> {
        return PinResult.Error("Not implemented")
    }

    override suspend fun createPin(
        title: String,
        board: String,
        description: String,
        link: String?,
        media: List<File>
    ): PinResult<Pin> {
        return PinResult.Error("Not implemented")
    }

    override suspend fun searchPins(query: String): PinResult<List<Pin>> = runCatching {
        PinResult.Success(api.searchPins(query))
    }.getOrElse { e -> PinResult.Error(e.toReadableMessage()) }

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