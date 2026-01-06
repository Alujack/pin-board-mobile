package kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import javax.inject.Inject

class GetRelatedPinsUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(pinId: String): PinResult<List<Pin>> {
        if (pinId.isBlank()) {
            return PinResult.Error("Invalid pin ID")
        }
        return repository.getRelatedPins(pinId)
    }
}

