package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.ApiResponse
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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
}
