package kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import javax.inject.Inject

class GetBoardsUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(): PinResult<List<Board>> {
        return repository.getBoards()
    }
}

