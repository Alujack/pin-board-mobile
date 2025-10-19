package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.ApiResponse
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PinApi {
    
    @GET("api/boards")
    suspend fun getBoards(): Response<ApiResponse<List<Board>>>
    
    @Multipart
    @POST("api/pins/create")
    suspend fun createPin(
        @Part("title") title: RequestBody,
        @Part("board") board: RequestBody,
        @Part("description") description: RequestBody,
        @Part("link") link: RequestBody?,
        @Part media: List<MultipartBody.Part>
    ): Response<ApiResponse<Pin>>

    // Profile-related endpoints
    @GET("api/pins")
    suspend fun searchPins(@Query("search") query: String): Response<ApiResponse<List<Pin>>>
    
    @GET("api/pins/created")
    suspend fun getCreatedPins(): Response<ApiResponse<List<Pin>>>
    
    @GET("api/pins/saved")
    suspend fun getSavedPins(): Response<ApiResponse<List<Pin>>>
    
    @POST("api/pins/{pinId}/save")
    suspend fun savePin(@Path("pinId") pinId: String): Response<ApiResponse<Unit>>
    
    @DELETE("api/pins/{pinId}/save")
    suspend fun unsavePin(@Path("pinId") pinId: String): Response<ApiResponse<Unit>>
    
    @GET("api/pins/media/{pinId}/download")
    suspend fun downloadPin(@Path("pinId") pinId: String): Response<ApiResponse<Unit>>
}
