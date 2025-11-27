package kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Comment
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import javax.inject.Inject

class CreateCommentUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(pinId: String, content: String, parentCommentId: String? = null): PinResult<Comment> {
        return repository.createComment(pinId, content, parentCommentId)
    }
}

