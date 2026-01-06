# PinBoard Mobile App - Quick Reference Guide

## 🚀 Quick Start

### Key Files Location
- **Main Activity**: `MainActivity.kt`
- **App Class**: `app/App.kt`
- **Navigation**: `app/navigation/NavGraph.kt`
- **API Base URL**: `app/build.gradle.kts` (line 26)

### Architecture Layers
```
presentation/  → UI & ViewModels
domain/        → Use Cases & Repository Interfaces
data/          → API & Repository Implementations
```

---

## 📋 Common Tasks

### 1. Add a New Feature

#### Step 1: Create Data Model
```kotlin
// data/model/NewModel.kt
data class NewModel(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)
```

#### Step 2: Create API Interface
```kotlin
// data/remote/NewApi.kt
interface NewApi {
    @GET("new-endpoint")
    suspend fun getNewData(): ApiResponse<List<NewModel>>
}
```

#### Step 3: Add to NetworkModule
```kotlin
// di/NetworkModule.kt
@Provides
@Singleton
fun provideNewApi(networkClient: NetworkClient): NewApi =
    networkClient.create(NewApi::class.java)
```

#### Step 4: Add Repository Method
```kotlin
// domain/repository/PinRepository.kt
suspend fun getNewData(): PinResult<List<NewModel>>

// data/repository/PinRepositoryImpl.kt
override suspend fun getNewData(): PinResult<List<NewModel>> {
    return try {
        val response = newApi.getNewData()
        PinResult.Success(response.data)
    } catch (e: Exception) {
        PinResult.Error(e.message ?: "Error")
    }
}
```

#### Step 5: Create Use Case
```kotlin
// domain/usecase/GetNewDataUseCase.kt
class GetNewDataUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(): PinResult<List<NewModel>> {
        return repository.getNewData()
    }
}
```

#### Step 6: Create ViewModel
```kotlin
// presentation/new/NewViewModel.kt
@HiltViewModel
class NewViewModel @Inject constructor(
    private val getNewDataUseCase: GetNewDataUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewUiState())
    val uiState: StateFlow<NewUiState> = _uiState.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            when (val result = getNewDataUseCase()) {
                is PinResult.Success -> {
                    _uiState.update { it.copy(data = result.data) }
                }
                is PinResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }
}
```

#### Step 7: Create Screen
```kotlin
// presentation/new/NewScreen.kt
@Composable
fun NewScreen(viewModel: NewViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column {
        // Your UI here
    }
}
```

---

## 🔑 Key Patterns

### State Management Pattern
```kotlin
// 1. Define UI State
data class FeatureUiState(
    val data: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// 2. Create StateFlow in ViewModel
private val _uiState = MutableStateFlow(FeatureUiState())
val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

// 3. Update State
_uiState.update { it.copy(isLoading = true) }

// 4. Observe in Composable
val uiState by viewModel.uiState.collectAsState()
```

### Error Handling Pattern
```kotlin
// Use sealed class for results
sealed class PinResult<out T> {
    data class Success<T>(val data: T) : PinResult<T>()
    data class Error(val message: String) : PinResult<Nothing>()
}

// Handle in ViewModel
when (val result = useCase()) {
    is PinResult.Success -> { /* Handle success */ }
    is PinResult.Error -> { /* Handle error */ }
}
```

### API Call Pattern
```kotlin
// Repository implementation
override suspend fun getData(): PinResult<List<Data>> {
    return try {
        val response = api.getData()
        PinResult.Success(response.data)
    } catch (e: HttpException) {
        PinResult.Error("Server error: ${e.code()}")
    } catch (e: Exception) {
        PinResult.Error(e.message ?: "Unknown error")
    }
}
```

---

## 📡 API Endpoints Reference

### Authentication
- `POST /auth/login` - Login
- `POST /auth/register` - Register

### Pins
- `GET /api/pins` - Get all pins
- `GET /api/pins/{pinId}` - Get pin by ID
- `POST /api/pins` - Create pin
- `POST /api/pins/{pinId}/save` - Save pin
- `DELETE /api/pins/{pinId}/save` - Unsave pin

### Likes
- `POST /pinLike/togglePinLike?pinId={id}` - Toggle like
- `GET /pinLike/checkPinLiked?pinId={id}` - Check liked
- `GET /pinLike/getPinLikes?pinId={id}` - Get likes list

### Comments
- `GET /api/comment/getComments?pinId={id}` - Get comments
- `POST /api/comment/createComment` - Create comment
- `DELETE /api/comment/deleteComment?commentId={id}` - Delete
- `POST /api/comment/toggleCommentLike?commentId={id}` - Like comment

### Share
- `POST /share/sharePin?pinId={id}` - Track share
- `GET /share/generateShareLink?pinId={id}` - Get link

### Notifications
- `GET /notifications` - Get notifications
- `POST /notifications/mark-read` - Mark as read
- `POST /notifications/mark-all-read` - Mark all read
- `POST /notifications/register-token` - Register FCM token

### Search
- `GET /api/pins/search?q={query}` - Search pins

---

## 🎨 UI Components

### Common Composable Patterns

#### Loading State
```kotlin
if (uiState.isLoading) {
    CircularProgressIndicator()
}
```

#### Error State
```kotlin
uiState.errorMessage?.let { error ->
    Text(
        text = error,
        color = MaterialTheme.colorScheme.error
    )
}
```

#### Empty State
```kotlin
if (uiState.data.isEmpty()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No data available")
    }
}
```

#### Pull-to-Refresh
```kotlin
SwipeRefresh(
    state = rememberSwipeRefreshState(uiState.isRefreshing),
    onRefresh = { viewModel.refresh() }
) {
    // Content
}
```

---

## 🔧 Dependency Injection

### Providing Dependencies
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object MyModule {
    @Provides
    @Singleton
    fun provideMyService(): MyService {
        return MyService()
    }
}
```

### Injecting Dependencies
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val myService: MyService
) : ViewModel()
```

### Getting ViewModel in Screen
```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    // Use viewModel
}
```

---

## 📱 Navigation

### Navigate to Screen
```kotlin
navController.navigate("screen-route")
```

### Navigate with Arguments
```kotlin
// Define route with argument
composable("pin-detail/{pinId}") { backStackEntry ->
    val pinId = backStackEntry.arguments?.getString("pinId")
    PinDetailScreen(pinId = pinId)
}

// Navigate
navController.navigate("pin-detail/$pinId")
```

### Pass Complex Data
```kotlin
// Using SavedStateHandle in ViewModel
class MyViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val data: String? = savedStateHandle["data"]
}
```

---

## 🔐 Authentication

### Get Current Token
```kotlin
val token = tokenManager.getToken()
```

### Include Token in API Request
```kotlin
// NetworkClient automatically adds token
// via OkHttp Interceptor
```

### Check if Logged In
```kotlin
val token = tokenManager.getToken()
val isLoggedIn = token != null
```

---

## 🔔 Notifications

### Register FCM Token
```kotlin
fcmTokenManager.initializeFCM()
```

### Handle Notification
```kotlin
// In PinBoardMessagingService
override fun onMessageReceived(remoteMessage: RemoteMessage) {
    // Handle notification
}
```

---

## 🐛 Debugging Tips

### Check API Calls
- Enable OkHttp Logging Interceptor
- Check Logcat for "OkHttp" tag

### Check State Updates
```kotlin
// Add logging in ViewModel
_uiState.update { newState ->
    Log.d("ViewModel", "State updated: $newState")
    newState
}
```

### Check Navigation
- Use Navigation component's logging
- Check back stack

### Common Issues
1. **Token not included**: Check NetworkClient interceptor
2. **State not updating**: Ensure using `collectAsState()`
3. **Navigation not working**: Check route definitions
4. **API errors**: Check base URL and endpoint paths

---

## 📚 File Naming Conventions

- **Screens**: `{Feature}Screen.kt` (e.g., `HomeScreen.kt`)
- **ViewModels**: `{Feature}ViewModel.kt` (e.g., `HomeViewModel.kt`)
- **Use Cases**: `{Action}{Entity}UseCase.kt` (e.g., `GetAllPinsUseCase.kt`)
- **APIs**: `{Entity}Api.kt` (e.g., `PinApi.kt`)
- **Models**: `{Entity}.kt` (e.g., `Pin.kt`)
- **Repositories**: `{Entity}Repository.kt` (interface), `{Entity}RepositoryImpl.kt` (implementation)

---

## 🎯 Best Practices

1. **Always use Use Cases** - Don't call repository directly from ViewModel
2. **Immutable State** - Use `copy()` to update state
3. **Error Handling** - Always handle both success and error cases
4. **Loading States** - Show loading indicators during async operations
5. **Empty States** - Provide meaningful empty state UI
6. **Type Safety** - Use sealed classes for results
7. **Dependency Injection** - Inject all dependencies, avoid singletons
8. **Coroutines** - Use `viewModelScope` for ViewModel coroutines
9. **StateFlow** - Use StateFlow for UI state, not LiveData
10. **Compose** - Keep composables small and focused

---

## 📖 Additional Resources

- **Full Documentation**: `PRESENTATION_DOCUMENT.md`
- **Architecture Diagrams**: `ARCHITECTURE_DIAGRAM.md`
- **Feature Updates**: `FEATURE_UPDATES.md`
- **Firebase Setup**: `FIREBASE_SETUP.md`

---

**Quick Tips**:
- Use Android Studio's "Find Usages" (Alt+F7) to trace code
- Use "Go to Declaration" (Ctrl+B) to navigate
- Check Logcat for runtime errors
- Use breakpoints for debugging

