package kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote

import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface PinApi {
    @GET("/pins/saved")
    suspend fun getSavedPins(): List<Pin>

    @GET("/pins/search")
    suspend fun searchPins(@Query("search") query: String): List<Pin>

    @POST("/pins/{id}/save")
    suspend fun savePin(@Path("id") id: String): Response<Unit>

    // Per your spec the path is "/{id}/unsave"
    @POST("/{id}/unsave")
    suspend fun unsavePin(@Path("id") id: String): Response<Unit>

    @GET("/pins/media/{id}/download")
    @Streaming
    suspend fun downloadMedia(@Path("id") id: String): Response<ResponseBody>

    // If you have a created pins endpoint, add it here. Otherwise, stub from saved or search.
    @GET("/pins/created")
    suspend fun getCreatedPins(): List<Pin>
}