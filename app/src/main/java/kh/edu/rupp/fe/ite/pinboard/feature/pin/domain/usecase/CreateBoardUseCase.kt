package kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import javax.inject.Inject

class CreateBoardUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String?,
        isPublic: Boolean = true
    ): PinResult<Board> {
        return repository.createBoard(name, description, isPublic)
    }
}

