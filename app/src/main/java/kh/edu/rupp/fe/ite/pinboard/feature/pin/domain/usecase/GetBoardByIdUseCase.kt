package kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import javax.inject.Inject

class GetBoardByIdUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(boardId: String): PinResult<Board> {
        if (boardId.isBlank()) {
            return PinResult.Error("Invalid board ID")
        }
        return repository.getBoardById(boardId)
    }
}

