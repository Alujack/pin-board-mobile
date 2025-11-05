package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.ApiListResponse
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.MediaItem
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface PinApi {
    // Saved media (flat list across all saved pins)
    @GET("api/pins/saved/media")
    suspend fun getSavedMedia(): ApiListResponse<MediaItem>

    @GET("api/pins/search")
    suspend fun searchPins(@Query("search") query: String): List<Pin>

    @POST("api/pins/{id}/save")
    suspend fun savePin(@Path("id") id: String): Response<Unit>

    // Per your spec the path is "/{id}/unsave"
    @POST("api/{id}/unsave")
    suspend fun unsavePin(@Path("id") id: String): Response<Unit>

    @GET("api/pins/media/{id}/download")
    @Streaming
    suspend fun downloadMedia(@Path("id") id: String): Response<ResponseBody>

    // Created images media (flat list)
    @GET("api/pins/created/media/images")
    suspend fun getCreatedImages(): ApiListResponse<MediaItem>
}