package kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import javax.inject.Inject

class GetPinsByBoardUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(boardId: String): PinResult<List<Pin>> {
        if (boardId.isBlank()) {
            return PinResult.Error("Invalid board ID")
        }
        return repository.getPinsByBoard(boardId)
    }
}

