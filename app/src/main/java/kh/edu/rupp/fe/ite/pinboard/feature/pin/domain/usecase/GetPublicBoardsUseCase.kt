package kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import javax.inject.Inject

class GetPublicBoardsUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(page: Int = 1, limit: Int = 50): PinResult<List<Board>> {
        return repository.getPublicBoards(page, limit)
    }
}

