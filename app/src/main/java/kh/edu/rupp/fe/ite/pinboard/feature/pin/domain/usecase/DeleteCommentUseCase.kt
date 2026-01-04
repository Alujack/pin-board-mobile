package kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase

import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(commentId: String): PinResult<Unit> {
        return repository.deleteComment(commentId)
    }
}

