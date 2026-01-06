# PinBoard Mobile App - Presentation Document

## 📱 Project Overview

**PinBoard** is a Pinterest-inspired mobile application built for Android that allows users to discover, save, and share visual content. The app enables users to create boards, pin images, interact with content through likes and comments, and receive real-time notifications.

### Key Highlights
- **Platform**: Android (Kotlin)
- **Architecture**: Clean Architecture (MVVM)
- **UI Framework**: Jetpack Compose
- **Backend**: RESTful API (Node.js/Express)
- **Real-time**: Firebase Cloud Messaging (FCM)
- **Version**: 2.0.0

---

## 🏗️ Architecture Overview

### Clean Architecture Pattern

The app follows **Clean Architecture** principles, separating concerns into three main layers:

```
┌─────────────────────────────────────────┐
│         PRESENTATION LAYER              │
│  (UI Components, ViewModels, Screens)   │
│                                         │
│  • Jetpack Compose UI                  │
│  • ViewModels (State Management)       │
│  • Navigation                           │
└─────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────┐
│          DOMAIN LAYER                    │
│  (Business Logic, Use Cases)             │
│                                         │
│  • Use Cases                            │
│  • Repository Interfaces                │
│  • Domain Models                        │
└─────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────┐
│           DATA LAYER                     │
│  (API, Local Storage, Repositories)      │
│                                         │
│  • Retrofit APIs                        │
│  • Repository Implementations           │
│  • Data Models                          │
│  • DataStore (Token Storage)            │
└─────────────────────────────────────────┘
```

### Layer Responsibilities

#### 1. **Presentation Layer** (`presentation/`)
- **Purpose**: Handles UI and user interactions
- **Components**:
  - `Screen.kt` - Composable UI screens
  - `ViewModel.kt` - State management and business logic coordination
  - `UiState` - Immutable state data classes
- **Technology**: Jetpack Compose, ViewModel, StateFlow

#### 2. **Domain Layer** (`domain/`)
- **Purpose**: Contains business logic independent of frameworks
- **Components**:
  - `usecase/` - Single-purpose business operations
  - `repository/` - Repository interfaces (contracts)
  - `model/` - Domain models
- **Technology**: Pure Kotlin, Coroutines

#### 3. **Data Layer** (`data/`)
- **Purpose**: Handles data sources (API, local storage)
- **Components**:
  - `remote/` - API interfaces (Retrofit)
  - `repository/` - Repository implementations
  - `model/` - Data transfer objects (DTOs)
  - `local/` - Local storage (DataStore)
- **Technology**: Retrofit, DataStore, OkHttp

---

## 🔄 Data Flow Architecture

### Request Flow (User Action → API)

```
User Action (UI)
    ↓
Screen Composable
    ↓
ViewModel Function
    ↓
Use Case
    ↓
Repository Interface
    ↓
Repository Implementation
    ↓
API Service (Retrofit)
    ↓
Backend API
    ↓
Response flows back up ↑
```

### Example: Like a Pin

```kotlin
// 1. USER ACTION (Screen)
Button(onClick = { viewModel.toggleLike(pinId) })

// 2. VIEWMODEL
fun toggleLike(pinId: String) {
    viewModelScope.launch {
        when (val result = togglePinLikeUseCase(pinId)) {
            is PinResult.Success -> {
                _uiState.update { 
                    it.copy(isLiked = result.data.isLiked) 
                }
            }
            is PinResult.Error -> { /* Handle error */ }
        }
    }
}

// 3. USE CASE
class TogglePinLikeUseCase(
    private val repository: PinRepository
) {
    suspend operator fun invoke(pinId: String): PinResult<TogglePinLikeResponse> {
        return repository.togglePinLike(pinId)
    }
}

// 4. REPOSITORY IMPLEMENTATION
override suspend fun togglePinLike(pinId: String): PinResult<TogglePinLikeResponse> {
    return try {
        val response = pinLikeApi.togglePinLike(pinId)
        PinResult.Success(response.data)
    } catch (e: Exception) {
        PinResult.Error(e.message ?: "Unknown error")
    }
}

// 5. API INTERFACE
interface PinLikeApi {
    @POST("pinLike/togglePinLike")
    suspend fun togglePinLike(
        @Query("pinId") pinId: String
    ): ApiResponse<TogglePinLikeResponse>
}
```

### State Management Flow

```
┌─────────────────┐
│   UI State      │
│  (StateFlow)    │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│   ViewModel     │
│  (State Holder) │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│   Use Cases     │
│  (Business Logic)│
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│   Repository    │
│  (Data Source)  │
└─────────────────┘
```

---

## 🎨 Design Patterns Used

### 1. **Dependency Injection (Hilt)**
- **Purpose**: Manage dependencies and enable testing
- **Implementation**: Hilt (Dagger-based)

```kotlin
// Module Definition
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun providePinApi(networkClient: NetworkClient): PinApi =
        networkClient.create(PinApi::class.java)
}

// Usage in ViewModel
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPinsUseCase: GetAllPinsUseCase,
    private val savePinUseCase: SavePinUseCase
) : ViewModel()
```

### 2. **Repository Pattern**
- **Purpose**: Abstract data sources
- **Benefit**: Easy to swap data sources (API → Local DB)

```kotlin
// Interface (Domain Layer)
interface PinRepository {
    suspend fun getAllPins(): PinResult<List<Pin>>
    suspend fun togglePinLike(pinId: String): PinResult<TogglePinLikeResponse>
}

// Implementation (Data Layer)
class PinRepositoryImpl @Inject constructor(
    private val api: PinApi
) : PinRepository {
    override suspend fun getAllPins(): PinResult<List<Pin>> {
        return try {
            val response = api.getAllPins()
            PinResult.Success(response.data)
        } catch (e: Exception) {
            PinResult.Error(e.message ?: "Error")
        }
    }
}
```

### 3. **Use Case Pattern**
- **Purpose**: Encapsulate single business operations
- **Benefit**: Reusable, testable business logic

```kotlin
class GetAllPinsUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(): PinResult<List<Pin>> {
        return repository.getAllPins()
    }
}
```

### 4. **State Management (MVVM)**
- **Purpose**: Separate UI from business logic
- **Implementation**: ViewModel + StateFlow

```kotlin
data class HomeUiState(
    val pins: List<Pin> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun loadPins() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // ... load data
        }
    }
}
```

### 5. **Sealed Classes for Result Handling**
- **Purpose**: Type-safe error handling

```kotlin
sealed class PinResult<out T> {
    data class Success<T>(val data: T) : PinResult<T>()
    data class Error(val message: String) : PinResult<Nothing>()
}

// Usage
when (val result = getAllPinsUseCase()) {
    is PinResult.Success -> { /* Handle success */ }
    is PinResult.Error -> { /* Handle error */ }
}
```

---

## 📊 Detailed Data Flow Examples

### 1. Authentication Flow

```
┌──────────────┐
│ Login Screen │
└──────┬───────┘
       │ User enters credentials
       ↓
┌──────────────────┐
│ LoginViewModel   │
│ • Validates input│
│ • Calls UseCase  │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ LoginUseCase     │
│ • Calls Repo     │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ AuthRepository   │
│ • API Call       │
│ • Save Token     │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ Backend API      │
│ • Validates      │
│ • Returns Token  │
└──────────────────┘
       │
       ↓ (Response flows back)
┌──────────────────┐
│ TokenManager     │
│ • Save to        │
│   DataStore      │
└──────────────────┘
       │
       ↓
┌──────────────────┐
│ Navigate to Home │
└──────────────────┘
```

**Code Example:**

```kotlin
// LoginViewModel.kt
fun login() {
    viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true)
        
        when (val result = authRepository.login(
            currentState.username,
            currentState.password
        )) {
            is AuthResult.Success -> {
                // Save token
                authRepository.saveToken(result.token ?: "")
                // Navigate to home
                _state.value = currentState.copy(isLoginSuccessful = true)
                // Register FCM token
                fcmTokenManager.initializeFCM()
            }
            is AuthResult.Error -> {
                _state.value = currentState.copy(
                    errorMessage = result.message
                )
            }
        }
    }
}
```

### 2. Pin Loading Flow

```
┌──────────────┐
│  Home Screen │
└──────┬───────┘
       │ Screen loads
       ↓
┌──────────────────┐
│ HomeViewModel    │
│ init {           │
│   loadPins()     │
│ }                │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ GetAllPinsUseCase│
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ PinRepository    │
│ • getAllPins()   │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ PinApi           │
│ GET /api/pins    │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ Backend          │
│ • Query DB       │
│ • Return Pins    │
└──────────────────┘
       │
       ↓ (Response)
┌──────────────────┐
│ Update UI State  │
│ • pins: List<Pin>│
│ • isLoading: false
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ Compose UI       │
│ • LazyColumn     │
│ • Pin Cards      │
└──────────────────┘
```

**Code Example:**

```kotlin
// HomeViewModel.kt
init {
    loadPins()
}

fun loadPins() {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        when (val result = getAllPinsUseCase()) {
            is PinResult.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pins = result.data,
                        errorMessage = null
                    )
                }
            }
            is PinResult.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}

// HomeScreen.kt
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.errorMessage != null -> ErrorMessage(uiState.errorMessage)
        else -> PinGrid(pins = uiState.pins)
    }
}
```

### 3. Like Pin Flow

```
┌──────────────┐
│ Pin Card     │
│ [Like Button]│
└──────┬───────┘
       │ User clicks
       ↓
┌──────────────────┐
│ HomeViewModel    │
│ toggleLike(pinId)│
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ TogglePinLike    │
│ UseCase          │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ PinRepository    │
│ togglePinLike()  │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ PinLikeApi       │
│ POST /pinLike/   │
│   togglePinLike  │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ Backend          │
│ • Update DB      │
│ • Return status  │
└──────────────────┘
       │
       ↓ (Response)
┌──────────────────┐
│ Update UI State  │
│ • isLiked: true  │
│ • likesCount++   │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ UI Updates       │
│ • Heart fills    │
│ • Counter ++     │
└──────────────────┘
```

**Code Example:**

```kotlin
// HomeViewModel.kt
fun toggleLike(pinId: String) {
    viewModelScope.launch {
        when (val result = togglePinLikeUseCase(pinId)) {
            is PinResult.Success -> {
                _uiState.update { currentState ->
                    val updatedPins = currentState.pins.map { pin ->
                        if (pin._id == pinId) {
                            pin.copy(
                                isLiked = result.data.isLiked,
                                likesCount = result.data.likesCount
                            )
                        } else {
                            pin
                        }
                    }
                    currentState.copy(pins = updatedPins)
                }
            }
            is PinResult.Error -> {
                _uiState.update { 
                    it.copy(errorMessage = result.message) 
                }
            }
        }
    }
}
```

### 4. Push Notification Flow

```
┌──────────────────┐
│ Firebase Cloud   │
│ Messaging        │
│ (Backend sends)  │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ PinBoardMessaging│
│ Service          │
│ • Receives FCM   │
│ • Shows Notification
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ Notification     │
│ Channel          │
│ • Display        │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ User taps        │
│ notification     │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ Deep Link        │
│ • Navigate to    │
│   Pin Detail     │
└──────────────────┘
```

**Code Example:**

```kotlin
// PinBoardMessagingService.kt
class PinBoardMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "New Notification",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }
    }
    
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add deep link data
            data["pinId"]?.let { putExtra("pinId", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(this).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }
}
```

---

## ✨ Key Features

### 1. **Authentication**
- User registration and login
- JWT token-based authentication
- Session management with DataStore
- Secure token storage

**Implementation:**
- `AuthRepository` - Handles auth API calls
- `TokenManager` - Manages token storage (DataStore)
- `LoginViewModel` - Manages login state
- `LoginScreen` - UI for login

### 2. **Pin Discovery**
- Browse all pins in a grid layout
- Pull-to-refresh functionality
- Infinite scroll (lazy loading)
- Pin detail view

**Implementation:**
- `GetAllPinsUseCase` - Fetches all pins
- `HomeViewModel` - Manages home screen state
- `HomeScreen` - Displays pin grid

### 3. **Pin Interactions**

#### Like/Unlike
- Real-time like/unlike functionality
- Visual feedback (heart animation)
- Like count display
- Synced with backend

**API Endpoints:**
- `POST /pinLike/togglePinLike` - Toggle like
- `GET /pinLike/checkPinLiked` - Check status
- `GET /pinLike/getPinLikes` - Get likes list

#### Save/Unsave
- Save pins to personal collection
- Unsave pins
- Track saved pins

**API Endpoints:**
- `POST /api/pins/{pinId}/save` - Save pin
- `DELETE /api/pins/{pinId}/save` - Unsave pin

#### Share
- Native Android share functionality
- Share tracking via backend
- Generate shareable links
- Share count tracking

**API Endpoints:**
- `POST /share/sharePin` - Track share
- `GET /share/generateShareLink` - Get shareable URL

#### Download
- Download pin images to device
- Save to gallery
- MediaStore integration

### 4. **Comments System**
- Create comments on pins
- Reply to comments (nested)
- Like/unlike comments
- Delete own comments
- Real-time updates
- Pagination support

**API Endpoints:**
- `POST /api/comment/createComment` - Create comment
- `GET /api/comment/getComments` - Fetch comments
- `DELETE /api/comment/deleteComment` - Delete comment
- `POST /api/comment/toggleCommentLike` - Like comment

**Code Example:**

```kotlin
// CommentsViewModel.kt
fun createComment(content: String, parentId: String? = null) {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        
        when (val result = createCommentUseCase(pinId, content, parentId)) {
            is PinResult.Success -> {
                loadComments() // Reload to show new comment
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        showSuccessMessage = true
                    ) 
                }
            }
            is PinResult.Error -> {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = result.message
                    ) 
                }
            }
        }
    }
}
```

### 5. **Push Notifications**
- Firebase Cloud Messaging (FCM) integration
- Real-time push notifications
- In-app notification center
- Notification management (mark as read)
- Deep linking support

**Notification Types:**
- 📌 Pin liked
- 💬 New comment
- 🔁 Comment reply
- 👤 New follower
- 🔖 Pin saved
- 📢 System notifications

**Implementation:**
- `PinBoardMessagingService` - FCM service
- `FCMTokenManager` - Token management
- `NotificationApi` - Backend API
- `NotificationsViewModel` - State management

### 6. **Search**
- Text-based search
- Search pins by title/description
- Real-time search results

**API Endpoint:**
- `GET /api/pins/search?q={query}` - Search pins

### 7. **User Profiles**
- View own profile
- View other users' profiles
- See created pins
- See saved pins
- Follow/unfollow users

**API Endpoints:**
- `GET /api/users/profile` - Get own profile
- `GET /api/users/{userId}` - Get user profile
- `POST /api/users/{userId}/follow` - Follow user
- `DELETE /api/users/{userId}/follow` - Unfollow user

### 8. **Boards**
- Create boards
- View board details
- View pins in board
- Public/private boards

**API Endpoints:**
- `GET /api/boards` - Get user boards
- `POST /api/boards` - Create board
- `GET /api/boards/{boardId}` - Get board details
- `GET /api/boards/{boardId}/pins` - Get board pins

### 9. **Create Pin**
- Upload images/videos
- Add title and description
- Select board
- Add link URL
- Multiple media support

**API Endpoint:**
- `POST /api/pins` - Create pin (multipart/form-data)

---

## 🛠️ Technical Stack

### Core Technologies
- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36
- **Build System**: Gradle (Kotlin DSL)

### Architecture Components
- **UI**: Jetpack Compose
- **State Management**: ViewModel + StateFlow
- **Navigation**: Navigation Compose
- **Dependency Injection**: Hilt (Dagger)
- **Async Operations**: Kotlin Coroutines + Flow

### Networking
- **HTTP Client**: Retrofit 2.9.0
- **JSON Parsing**: Gson (via Retrofit converter)
- **Logging**: OkHttp Logging Interceptor

### Image Loading
- **Library**: Coil 2.5.0
- **Features**: Caching, placeholders, transformations

### Local Storage
- **Preferences**: DataStore Preferences
- **Purpose**: Token storage, user preferences

### Firebase
- **Cloud Messaging**: Firebase Messaging 24.0.0
- **Purpose**: Push notifications

### UI Libraries
- **Material Design**: Material 3
- **Icons**: Material Icons Extended
- **Pull-to-Refresh**: Accompanist SwipeRefresh

---

## 📁 Project Structure

```
app/src/main/java/kh/edu/rupp/fe/ite/pinboard/
├── app/
│   ├── App.kt                    # Application class (Hilt)
│   └── navigation/
│       ├── AuthNavGraph.kt       # Auth navigation
│       └── NavGraph.kt            # Main navigation
│
├── core/
│   └── utils/
│       └── Resource.kt           # Result wrapper
│
├── feature/
│   ├── auth/                     # Authentication feature
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   └── TokenManager.kt
│   │   │   ├── remote/
│   │   │   │   ├── AuthApi.kt
│   │   │   │   └── NetworkClient.kt
│   │   │   └── repository/
│   │   │       └── AuthRepositoryImpl.kt
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   └── User.kt
│   │   │   ├── repository/
│   │   │   │   └── AuthRepository.kt
│   │   │   └── usecase/
│   │   │       ├── LoginUseCase.kt
│   │   │       └── RegisterUseCase.kt
│   │   ├── presentation/
│   │   │   ├── login/
│   │   │   │   ├── LoginScreen.kt
│   │   │   │   └── LoginViewModel.kt
│   │   │   ├── register/
│   │   │   │   ├── RegisterScreen.kt
│   │   │   │   └── RegisterViewModel.kt
│   │   │   └── welcome/
│   │   │       └── WelcomeScreen.kt
│   │   └── di/
│   │       └── AuthModule.kt
│   │
│   └── pin/                      # Pin feature
│       ├── data/
│       │   ├── model/
│       │   │   ├── Pin.kt
│       │   │   ├── Comment.kt
│       │   │   ├── Board.kt
│       │   │   └── ...
│       │   ├── remote/
│       │   │   ├── PinApi.kt
│       │   │   ├── CommentApi.kt
│       │   │   ├── PinLikeApi.kt
│       │   │   ├── ShareApi.kt
│       │   │   └── NotificationApi.kt
│       │   └── repository/
│       │       └── PinRepositoryImpl.kt
│       ├── domain/
│       │   ├── repository/
│       │   │   └── PinRepository.kt
│       │   └── usecase/
│       │       ├── GetAllPinsUseCase.kt
│       │       ├── TogglePinLikeUseCase.kt
│       │       ├── CreateCommentUseCase.kt
│       │       ├── SharePinUseCase.kt
│       │       └── ...
│       ├── presentation/
│       │   ├── home/
│       │   │   ├── HomeScreen.kt
│       │   │   └── HomeViewModel.kt
│       │   ├── detail/
│       │   │   ├── PinDetailScreen.kt
│       │   │   └── PinDetailViewModel.kt
│       │   ├── comments/
│       │   │   ├── CommentsScreen.kt
│       │   │   └── CommentsViewModel.kt
│       │   ├── notifications/
│       │   │   ├── NotificationsScreen.kt
│       │   │   └── NotificationsViewModel.kt
│       │   ├── search/
│       │   │   ├── SearchScreen.kt
│       │   │   └── SearchViewModel.kt
│       │   ├── profile/
│       │   │   ├── ProfileScreen.kt
│       │   │   └── ProfileViewModel.kt
│       │   └── create/
│       │       ├── CreatePinScreen.kt
│       │       └── CreatePinViewModel.kt
│       ├── services/
│       │   ├── PinBoardMessagingService.kt
│       │   └── FCMTokenManager.kt
│       └── di/
│           ├── NetworkModule.kt
│           └── PinModule.kt
│
└── MainActivity.kt               # Main activity
```

---

## 💻 Code Examples

### Example 1: Complete Feature Implementation (Like Pin)

#### 1. Domain Layer - Use Case
```kotlin
class TogglePinLikeUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(pinId: String): PinResult<TogglePinLikeResponse> {
        return repository.togglePinLike(pinId)
    }
}
```

#### 2. Data Layer - API Interface
```kotlin
interface PinLikeApi {
    @POST("pinLike/togglePinLike")
    suspend fun togglePinLike(
        @Query("pinId") pinId: String
    ): ApiResponse<TogglePinLikeResponse>
}
```

#### 3. Data Layer - Repository Implementation
```kotlin
override suspend fun togglePinLike(pinId: String): PinResult<TogglePinLikeResponse> {
    return try {
        val response = pinLikeApi.togglePinLike(pinId)
        PinResult.Success(response.data)
    } catch (e: HttpException) {
        PinResult.Error("Server error: ${e.code()}")
    } catch (e: Exception) {
        PinResult.Error(e.message ?: "Unknown error")
    }
}
```

#### 4. Presentation Layer - ViewModel
```kotlin
@HiltViewModel
class PinDetailViewModel @Inject constructor(
    private val togglePinLikeUseCase: TogglePinLikeUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PinDetailUiState())
    val uiState: StateFlow<PinDetailUiState> = _uiState.asStateFlow()
    
    fun toggleLike() {
        val pinId = _uiState.value.pin?._id ?: return
        
        viewModelScope.launch {
            when (val result = togglePinLikeUseCase(pinId)) {
                is PinResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLiked = result.data.isLiked,
                            likesCount = result.data.likesCount
                        )
                    }
                }
                is PinResult.Error -> {
                    _uiState.update { 
                        it.copy(errorMessage = result.message) 
                    }
                }
            }
        }
    }
}
```

#### 5. Presentation Layer - UI Screen
```kotlin
@Composable
fun PinDetailScreen(
    viewModel: PinDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column {
        // Pin Image
        AsyncImage(
            model = uiState.pin?.imageUrl,
            contentDescription = null
        )
        
        // Like Button
        Row {
            IconButton(onClick = { viewModel.toggleLike() }) {
                Icon(
                    imageVector = if (uiState.isLiked) 
                        Icons.Default.Favorite 
                    else 
                        Icons.Default.FavoriteBorder,
                    tint = if (uiState.isLiked) Color.Red else Color.Gray
                )
            }
            Text(text = "${uiState.likesCount} likes")
        }
    }
}
```

### Example 2: State Management Pattern

```kotlin
// Define UI State
data class HomeUiState(
    val pins: List<Pin> = emptyList(),
    val savedPinIds: Set<String> = emptySet(),
    val likedPinIds: Map<String, Boolean> = emptyMap(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

// ViewModel with StateFlow
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPinsUseCase: GetAllPinsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun loadPins() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = getAllPinsUseCase()) {
                is PinResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pins = result.data
                        )
                    }
                }
                is PinResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
}

// Compose UI consuming state
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.errorMessage != null -> ErrorView(uiState.errorMessage)
        else -> PinGrid(pins = uiState.pins)
    }
}
```

### Example 3: Dependency Injection Setup

```kotlin
// Application class
@HiltAndroidApp
class App : Application()

// Network Module
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun providePinApi(networkClient: NetworkClient): PinApi =
        networkClient.create(PinApi::class.java)
}

// Repository Module
@Module
@InstallIn(SingletonComponent::class)
abstract class PinModule {
    @Binds
    abstract fun bindPinRepository(
        pinRepositoryImpl: PinRepositoryImpl
    ): PinRepository
}

// ViewModel Injection
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPinsUseCase: GetAllPinsUseCase
) : ViewModel()
```

### Example 4: Error Handling Pattern

```kotlin
// Sealed class for results
sealed class PinResult<out T> {
    data class Success<T>(val data: T) : PinResult<T>()
    data class Error(val message: String) : PinResult<Nothing>()
}

// Extension function for error handling
fun Throwable.toReadableMessage(): String {
    return when (this) {
        is HttpException -> {
            when (code()) {
                401 -> "Unauthorized. Please login again."
                404 -> "Resource not found."
                500 -> "Server error. Please try again later."
                else -> "Network error: ${message()}"
            }
        }
        is IOException -> "Network connection error. Check your internet."
        else -> message ?: "Unknown error occurred"
    }
}

// Usage in Repository
override suspend fun getAllPins(): PinResult<List<Pin>> {
    return try {
        val response = api.getAllPins()
        PinResult.Success(response.data)
    } catch (e: Exception) {
        PinResult.Error(e.toReadableMessage())
    }
}
```

---

## 🔐 Security Features

### 1. **Token Management**
- JWT tokens stored securely in DataStore
- Automatic token inclusion in API requests
- Token refresh mechanism
- Secure token clearing on logout

### 2. **Network Security**
- HTTPS only (enforced by backend)
- Token-based authentication
- Request/response logging (debug only)

### 3. **Data Privacy**
- No sensitive data in logs
- Secure storage of credentials
- Session management

---

## 📈 Performance Optimizations

### 1. **Image Loading**
- Coil library for efficient image loading
- Automatic caching
- Placeholder support
- Memory-efficient loading

### 2. **Lazy Loading**
- LazyColumn for lists
- Pagination support
- On-demand loading

### 3. **State Management**
- Immutable state objects
- Efficient recomposition
- StateFlow for reactive updates

### 4. **Network Optimization**
- Request caching (where applicable)
- Error retry mechanisms
- Background thread operations

---

## 🧪 Testing Strategy

### Unit Testing
- Use Cases
- ViewModels
- Repository implementations

### Integration Testing
- API integration
- Repository + API
- End-to-end flows

### UI Testing
- Compose UI tests
- Navigation tests
- User interaction tests

---

## 🚀 Deployment

### Build Configuration
- **Debug**: Development builds with logging
- **Release**: Optimized builds with ProGuard

### Build Steps
1. Configure Firebase (`google-services.json`)
2. Set API base URL in `build.gradle.kts`
3. Build APK/AAB
4. Sign with release key
5. Test on devices
6. Deploy to Play Store

---

## 📚 Learning Resources

### Architecture
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)

### Jetpack Compose
- [Compose Documentation](https://developer.android.com/jetpack/compose)
- [Compose Samples](https://github.com/android/compose-samples)

### Dependency Injection
- [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android)

### Firebase
- [FCM Documentation](https://firebase.google.com/docs/cloud-messaging)

---

## 🎯 Future Enhancements

### Planned Features
- [ ] Video playback support
- [ ] Image zoom/pinch gestures
- [ ] Offline mode with local caching
- [ ] Advanced search filters
- [ ] User profile customization
- [ ] Board collaboration
- [ ] Pin analytics
- [ ] Dark mode theme
- [ ] Tablet optimization
- [ ] Accessibility improvements

---

## 📞 Support & Contact

For questions or issues:
1. Check documentation files (`FEATURE_UPDATES.md`, `UPDATE_SUMMARY.md`)
2. Review API documentation
3. Verify Firebase setup (`FIREBASE_SETUP.md`)
4. Check application logs
5. Test with backend running

---

## 📄 Summary

**PinBoard Mobile App** is a modern Android application built with:
- ✅ Clean Architecture (separation of concerns)
- ✅ MVVM pattern (state management)
- ✅ Jetpack Compose (modern UI)
- ✅ Hilt (dependency injection)
- ✅ Coroutines & Flow (async operations)
- ✅ Retrofit (networking)
- ✅ Firebase (push notifications)
- ✅ Material Design 3 (beautiful UI)

The app demonstrates best practices in Android development, including proper architecture, error handling, state management, and user experience design.

---

**Last Updated**: December 2024  
**Version**: 2.0.0  
**Author**: PinBoard Development Team

