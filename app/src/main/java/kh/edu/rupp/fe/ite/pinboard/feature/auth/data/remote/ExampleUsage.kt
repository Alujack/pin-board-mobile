package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote

import javax.inject.Inject
import javax.inject.Singleton

// Example of how to use NetworkClient in other features
@Singleton
class ExampleRepository @Inject constructor(
    private val networkClient: NetworkClient
) {
    
    // Create API instances using NetworkClient
    private val exampleApi: ExampleApi by lazy {
        networkClient.create(ExampleApi::class.java)
    }
    
    // All API calls will automatically include tokens and handle refresh
    suspend fun getUserProfile() = exampleApi.getUserProfile()
    suspend fun getPosts() = exampleApi.getPosts()
    suspend fun getUserSettings() = exampleApi.getUserSettings()
    
    // Benefits of using NetworkClient:
    // 1. Automatic token injection
    // 2. Automatic token refresh on 401 errors
    // 3. Centralized network configuration
    // 4. Consistent error handling
    // 5. No need to manually handle authentication in each feature
}
