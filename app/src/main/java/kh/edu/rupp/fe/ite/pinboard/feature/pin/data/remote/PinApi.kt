package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.ApiListResponse
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.MediaItem
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.PinResponse
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.SinglePinResponse
import okhttp3.ResponseBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PinApi {
    // Saved media (flat list across all saved pins)
    @GET("api/pins/saved/media")
    suspend fun getSavedMedia(): ApiListResponse<MediaItem>

    @GET("api/pins/search")
    suspend fun searchPins(
        @Query("q") query: String
    ): PinResponse

    @GET("api/pins")
    suspend fun getAllPins(): PinResponse

    @GET("api/pins/detail/{id}")
    suspend fun getPinById(@Path("id") id: String): SinglePinResponse

    @POST("api/pins/{id}/save")
    suspend fun savePin(@Path("id") id: String): Response<Unit>

    // Per your spec the path is "/{id}/unsave"
    @POST("api/pins/{id}/unsave")
    suspend fun unsavePin(@Path("id") id: String): Response<Unit>

    @GET("api/pins/media/{id}/download")
    @Streaming
    suspend fun downloadMedia(@Path("id") id: String): Response<ResponseBody>

    // Created images media (flat list)
    @GET("api/pins/created/media/images")
    suspend fun getCreatedImages(): ApiListResponse<MediaItem>

    // Boards
    @GET("api/boards")
    suspend fun getBoards(): ApiListResponse<Board>

    @GET("api/boards/{id}")
    suspend fun getBoardById(@Path("id") id: String): Board

    @GET("api/boards/{id}/pins")
    suspend fun getPinsByBoard(@Path("id") boardId: String): PinResponse

    // Create pin with multipart upload
    @Multipart
    @POST("api/pins/create")
    suspend fun createPin(
        @Part("title") title: RequestBody,
        @Part("board") board: RequestBody,
        @Part("description") description: RequestBody,
        @Part("link") link: RequestBody?,
        @Part media: List<MultipartBody.Part>
    ): Pin
}