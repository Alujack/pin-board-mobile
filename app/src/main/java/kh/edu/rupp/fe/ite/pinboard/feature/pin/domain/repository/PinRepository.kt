package kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import java.io.File

sealed class PinResult<out T> {
    data class Success<T>(val data: T) : PinResult<T>()
    data class Error(val message: String) : PinResult<Nothing>()
}

interface PinRepository {
    suspend fun getBoards(): PinResult<List<Board>>
    suspend fun createPin(
        title: String,
        board: String,
        description: String,
        link: String?,
        media: List<File>
    ): PinResult<Pin>
}
