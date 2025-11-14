package kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import java.io.File
import javax.inject.Inject

class CreatePinUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(
        title: String,
        board: String,
        description: String,
        link: String?,
        media: List<File>
    ): PinResult<Pin> {
        // Validation
        if (title.isBlank()) {
            return PinResult.Error("Title is required")
        }
        if (board.isBlank()) {
            return PinResult.Error("Board is required")
        }
        if (description.isBlank()) {
            return PinResult.Error("Description is required")
        }
        if (media.isEmpty()) {
            return PinResult.Error("At least one media file is required")
        }
        
        // Check file size (100MB limit)
        val maxSize = 100 * 1024 * 1024L
        val oversizedFiles = media.filter { it.length() > maxSize }
        if (oversizedFiles.isNotEmpty()) {
            return PinResult.Error("Files must be smaller than 100MB")
        }
        
        return repository.createPin(title, board, description, link, media)
    }
}

