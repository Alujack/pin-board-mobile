package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote

import retrofit2.Response
import retrofit2.http.GET

// Example API interface showing how to use NetworkClient in other features
interface ExampleApi {
    
    @GET("api/user/profile")
    suspend fun getUserProfile(): Response<UserProfile>
    
    @GET("api/posts")
    suspend fun getPosts(): Response<List<Post>>
    
    @GET("api/user/settings")
    suspend fun getUserSettings(): Response<UserSettings>
}

// Example data classes
data class UserProfile(
    val id: String,
    val username: String,
    val email: String,
    val profilePicture: String?
)

data class Post(
    val id: String,
    val title: String,
    val content: String,
    val authorId: String,
    val createdAt: String
)

data class UserSettings(
    val notifications: Boolean,
    val theme: String,
    val language: String
)
