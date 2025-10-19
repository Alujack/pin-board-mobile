package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.repository

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote.PinApi
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class PinRepositoryImpl @Inject constructor(
    private val pinApi: PinApi
) : PinRepository {

    override suspend fun getBoards(): PinResult<List<Board>> {
        return try {
            val response = pinApi.getBoards()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    PinResult.Success(apiResponse.data)
                } else {
                    PinResult.Error(apiResponse?.message ?: "No boards found")
                }
            } else {
                PinResult.Error("Failed to fetch boards: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error("Network error: ${e.message}")
        }
    }

    private fun guessMimeType(file: File): String {
        val name = file.name.lowercase()
        return when {
            name.endsWith(".jpg") || name.endsWith(".jpeg") -> "image/jpeg"
            name.endsWith(".png") -> "image/png"
            name.endsWith(".webp") -> "image/webp"
            name.endsWith(".gif") -> "image/gif"
            name.endsWith(".mp4") -> "video/mp4"
            name.endsWith(".mov") -> "video/quicktime"
            name.endsWith(".webm") -> "video/webm"
            name.endsWith(".mkv") -> "video/x-matroska"
            name.endsWith(".3gp") -> "video/3gpp"
            else -> "application/octet-stream"
        }
    }

    override suspend fun createPin(
        title: String,
        board: String,
        description: String,
        link: String?,
        media: List<File>
    ): PinResult<Pin> {
        return try {
            // Convert files to MultipartBody.Part
            val mediaParts = media.map { file ->
                val mime = guessMimeType(file)
                val requestFile = file.asRequestBody(mime.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("media", file.name, requestFile)
            }

            // Convert strings to RequestBody
            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val boardBody = board.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val linkBody = link?.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = pinApi.createPin(
                title = titleBody,
                board = boardBody,
                description = descriptionBody,
                link = linkBody,
                media = mediaParts
            )

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    PinResult.Success(apiResponse.data)
                } else {
                    PinResult.Error(apiResponse?.message ?: "Failed to create pin: Invalid response")
                }
            } else {
                PinResult.Error("Failed to create pin: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error("Network error: ${e.message}")
        }
    }

    override suspend fun searchPins(query: String): PinResult<List<Pin>> {
        return try {
            val response = pinApi.searchPins(query)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    PinResult.Success(apiResponse.data)
                } else {
                    PinResult.Error(apiResponse?.message ?: "No pins found")
                }
            } else {
                PinResult.Error("Failed to search pins: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error("Network error: ${e.message}")
        }
    }

    override suspend fun getCreatedPins(): PinResult<List<Pin>> {
        return try {
            val response = pinApi.getCreatedPins()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    PinResult.Success(apiResponse.data)
                } else {
                    PinResult.Error(apiResponse?.message ?: "No created pins found")
                }
            } else {
                PinResult.Error("Failed to fetch created pins: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error("Network error: ${e.message}")
        }
    }

    override suspend fun getSavedPins(): PinResult<List<Pin>> {
        return try {
            val response = pinApi.getSavedPins()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    PinResult.Success(apiResponse.data)
                } else {
                    PinResult.Error(apiResponse?.message ?: "No saved pins found")
                }
            } else {
                PinResult.Error("Failed to fetch saved pins: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error("Network error: ${e.message}")
        }
    }

    override suspend fun savePin(pinId: String): PinResult<Unit> {
        return try {
            val response = pinApi.savePin(pinId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    PinResult.Success(Unit)
                } else {
                    PinResult.Error(apiResponse?.message ?: "Failed to save pin")
                }
            } else {
                PinResult.Error("Failed to save pin: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error("Network error: ${e.message}")
        }
    }

    override suspend fun unsavePin(pinId: String): PinResult<Unit> {
        return try {
            val response = pinApi.unsavePin(pinId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    PinResult.Success(Unit)
                } else {
                    PinResult.Error(apiResponse?.message ?: "Failed to unsave pin")
                }
            } else {
                PinResult.Error("Failed to unsave pin: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error("Network error: ${e.message}")
        }
    }

    override suspend fun downloadPin(pinId: String): PinResult<Unit> {
        return try {
            val response = pinApi.downloadPin(pinId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    PinResult.Success(Unit)
                } else {
                    PinResult.Error(apiResponse?.message ?: "Failed to download pin")
                }
            } else {
                PinResult.Error("Failed to download pin: ${response.code()}")
            }
        } catch (e: Exception) {
            PinResult.Error("Network error: ${e.message}")
        }
    }
}
