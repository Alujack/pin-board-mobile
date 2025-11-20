package kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote.NotificationListResponse
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(page: Int = 1, limit: Int = 20): PinResult<NotificationListResponse> {
        return repository.getNotifications(page, limit)
    }
}

