# 📱 Screen UI Data Action Flow Guide

## 🎯 Overview

This guide explains how user actions in the UI trigger data flow through the Clean Architecture layers and how the UI updates reactively.

---

## 🔄 The Complete Flow Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                    USER ACTION (UI)                          │
│  User clicks button, types text, selects item, etc.         │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│              SCREEN COMPOSABLE (Presentation)                │
│  • Captures user interaction                                │
│  • Calls ViewModel function                                 │
│  • Example: Button(onClick = { viewModel.createPin() })    │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│              VIEWMODEL (Presentation Layer)                   │
│  • Receives action                                          │
│  • Updates local state (loading, error)                     │
│  • Calls Use Case                                           │
│  • Example: viewModelScope.launch { useCase() }            │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│              USE CASE (Domain Layer)                         │
│  • Contains business logic                                  │
│  • Calls Repository interface                               │
│  • Example: return repository.createPin(...)                │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│         REPOSITORY IMPLEMENTATION (Data Layer)              │
│  • Implements repository interface                          │
│  • Calls API service                                        │
│  • Handles errors                                           │
│  • Example: api.createPin(...)                             │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│              API SERVICE (Retrofit)                         │
│  • Makes HTTP request                                       │
│  • Sends to backend                                         │
│  • Example: POST /api/pins                                  │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│              BACKEND API                                    │
│  • Processes request                                        │
│  • Returns response                                         │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓ (Response flows back UP)
┌─────────────────────────────────────────────────────────────┐
│         REPOSITORY → USE CASE → VIEWMODEL                   │
│  • Result wrapped in PinResult                              │
│  • Success or Error                                         │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│              VIEWMODEL UPDATES STATE                        │
│  • Updates StateFlow                                        │
│  • Example: _state.update { it.copy(isCreating = false) } │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│              UI REACTIVELY UPDATES                          │
│  • Compose recomposes                                       │
│  • Shows success/error                                      │
│  • Updates UI elements                                      │
└─────────────────────────────────────────────────────────────┘
```

---

## 📝 Example 1: Creating a Pin (Complete Flow)

### Step-by-Step Flow

#### 1️⃣ **USER ACTION** (CreatePinScreen.kt)
```kotlin
// User clicks "Publish" button
Button(
    onClick = { viewModel.createPin() },  // ← ACTION TRIGGERED
    enabled = !state.isCreating && state.title.isNotBlank() && ...
)
```

#### 2️⃣ **SCREEN → VIEWMODEL** (CreatePinScreen.kt → CreatePinViewModel.kt)
```kotlin
// Screen calls ViewModel function
viewModel.createPin()  // ← Function call
```

#### 3️⃣ **VIEWMODEL PROCESSES** (CreatePinViewModel.kt)
```kotlin
fun createPin() {
    val currentState = _state.value
    
    // Update UI state immediately (optimistic update)
    viewModelScope.launch {
        _state.value = currentState.copy(
            isCreating = true,      // ← Show loading spinner
            errorMessage = null
        )
        
        // Call Use Case
        when (val result = createPinUseCase(  // ← DOMAIN LAYER CALL
            title = currentState.title,
            board = currentState.selectedBoard!!._id,
            description = currentState.description,
            link = currentState.link.takeIf { it.isNotBlank() },
            media = currentState.selectedFiles
        )) {
            is PinResult.Success -> {
                // Update state on success
                _state.value = currentState.copy(
                    isCreating = false,
                    isPinCreated = true  // ← Triggers navigation
                )
            }
            is PinResult.Error -> {
                // Update state on error
                _state.value = currentState.copy(
                    isCreating = false,
                    errorMessage = result.message  // ← Show error
                )
            }
        }
    }
}
```

#### 4️⃣ **USE CASE** (Domain Layer)
```kotlin
class CreatePinUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(...): PinResult<Pin> {
        return repository.createPin(...)  // ← Calls Repository
    }
}
```

#### 5️⃣ **REPOSITORY IMPLEMENTATION** (Data Layer)
```kotlin
override suspend fun createPin(...): PinResult<Pin> {
    return try {
        val response = pinApi.createPin(...)  // ← API CALL
        PinResult.Success(response.data)
    } catch (e: Exception) {
        PinResult.Error(e.message ?: "Unknown error")
    }
}
```

#### 6️⃣ **API SERVICE** (Retrofit)
```kotlin
interface PinApi {
    @Multipart
    @POST("api/pins")
    suspend fun createPin(
        @Part("title") title: RequestBody,
        @Part("board") board: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): ApiResponse<Pin>
}
```

#### 7️⃣ **RESPONSE FLOWS BACK** (Repository → UseCase → ViewModel)
```kotlin
// Result flows back through layers
PinResult.Success(pin)  // or PinResult.Error("message")
```

#### 8️⃣ **VIEWMODEL UPDATES STATE**
```kotlin
// ViewModel receives result and updates StateFlow
_state.value = currentState.copy(
    isCreating = false,
    isPinCreated = true  // ← State updated
)
```

#### 9️⃣ **UI REACTIVELY UPDATES** (CreatePinScreen.kt)
```kotlin
@Composable
fun CreatePinScreen(viewModel: CreatePinViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()  // ← Observes StateFlow
    
    // UI automatically recomposes when state changes
    Button(
        onClick = { viewModel.createPin() },
        enabled = !state.isCreating  // ← Updates automatically
    ) {
        if (state.isCreating) {  // ← Shows loading spinner
            CircularProgressIndicator(...)
        } else {
            Text("Publish")
        }
    }
    
    // Error message appears automatically
    if (state.errorMessage != null) {
        Card(...) {
            Text(state.errorMessage)  // ← Error displayed
        }
    }
    
    // Navigation triggered automatically
    LaunchedEffect(state.isPinCreated) {
        if (state.isPinCreated) {
            onPinCreated()  // ← Navigate back
        }
    }
}
```

---

## 📝 Example 2: Loading Pins on Home Screen

### Flow Diagram

```
User opens Home Screen
        ↓
Screen Composable renders
        ↓
LaunchedEffect triggers OR init block runs
        ↓
HomeViewModel.loadPins() called
        ↓
ViewModel updates: isLoading = true
        ↓
UI shows loading spinner
        ↓
GetAllPinsUseCase invoked
        ↓
PinRepository.getAllPins() called
        ↓
PinApi.getAllPins() HTTP GET request
        ↓
Backend returns list of pins
        ↓
Repository wraps in PinResult.Success
        ↓
UseCase returns result
        ↓
ViewModel updates: isLoading = false, pins = result.data
        ↓
UI automatically recomposes
        ↓
PinGrid displays pins
```

### Code Flow

#### 1️⃣ **SCREEN LOADS** (HomeScreen.kt)
```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // ViewModel.init automatically calls loadPins()
    // OR you can trigger manually:
    // LaunchedEffect(Unit) { viewModel.loadPins() }
}
```

#### 2️⃣ **VIEWMODEL INIT** (HomeViewModel.kt)
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPinsUseCase: GetAllPinsUseCase
) : ViewModel() {
    
    init {
        loadPins()  // ← Automatically called when ViewModel created
    }
    
    fun loadPins() {
        viewModelScope.launch {
            // Update state: show loading
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // Call Use Case
            when (val result = getAllPinsUseCase()) {
                is PinResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pins = result.data,  // ← Update pins
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
}
```

#### 3️⃣ **UI REACTS TO STATE** (HomeScreen.kt)
```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when {
        uiState.isLoading && uiState.pins.isEmpty() -> {
            LoadingView()  // ← Shows spinner
        }
        uiState.pins.isEmpty() && !uiState.isLoading -> {
            EmptyStateView(onRetry = { viewModel.loadPins() })
        }
        else -> {
            PinGrid(pins = uiState.pins)  // ← Shows pins when loaded
        }
    }
}
```

---

## 📝 Example 3: Toggle Like Action

### Flow

```
User clicks Like button
        ↓
PinCard: IconButton(onClick = { viewModel.toggleLike(pinId) })
        ↓
HomeViewModel.toggleLike(pinId)
        ↓
Optimistic UI update (optional - show heart immediately)
        ↓
TogglePinLikeUseCase invoked
        ↓
PinRepository.togglePinLike(pinId)
        ↓
PinLikeApi.togglePinLike(pinId) POST request
        ↓
Backend updates like status
        ↓
Returns: { isLiked: true, likesCount: 42 }
        ↓
Repository → UseCase → ViewModel
        ↓
ViewModel updates state: likedPinIds[pinId] = true
        ↓
UI recomposes: Heart icon fills, counter updates
```

### Code Example

#### 1️⃣ **USER CLICKS LIKE** (HomeScreen.kt)
```kotlin
IconButton(
    onClick = { viewModel.toggleLike(pin._id ?: "") }  // ← Action
) {
    Icon(
        imageVector = if (uiState.likedPinIds[pin._id] == true)
            Icons.Default.Favorite  // ← Filled heart
        else
            Icons.Default.FavoriteBorder,  // ← Empty heart
        tint = if (uiState.likedPinIds[pin._id] == true) 
            Color.Red else Color.Gray
    )
}
```

#### 2️⃣ **VIEWMODEL HANDLES** (HomeViewModel.kt)
```kotlin
fun toggleLike(pinId: String) {
    viewModelScope.launch {
        when (val result = togglePinLikeUseCase(pinId)) {
            is PinResult.Success -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        likedPinIds = currentState.likedPinIds + (pinId to result.data.isLiked)
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
```

#### 3️⃣ **UI UPDATES AUTOMATICALLY**
```kotlin
// When state.likedPinIds changes, Compose automatically recomposes
// the IconButton and updates the heart icon
```

---

## 🔑 Key Concepts

### 1. **StateFlow for Reactive Updates**

```kotlin
// ViewModel
private val _uiState = MutableStateFlow(CreatePinState())
val uiState: StateFlow<CreatePinState> = _uiState.asStateFlow()

// Screen
val state by viewModel.state.collectAsStateWithLifecycle()
// ↑ This automatically recomposes when _state changes
```

### 2. **Unidirectional Data Flow**

```
UI → ViewModel → UseCase → Repository → API
                                    ↓
UI ← ViewModel ← UseCase ← Repository ← API
```

**Important**: Data flows in ONE direction:
- **Down**: Actions flow down (UI → API)
- **Up**: Results flow up (API → UI)

### 3. **State Updates Pattern**

```kotlin
// Always use .update() or .copy() for immutable updates
_uiState.update { currentState ->
    currentState.copy(
        isLoading = true,  // ← Update specific fields
        errorMessage = null
    )
}
```

### 4. **Error Handling Flow**

```kotlin
when (val result = useCase()) {
    is PinResult.Success -> {
        // Update state with success data
        _uiState.update { it.copy(data = result.data) }
    }
    is PinResult.Error -> {
        // Update state with error
        _uiState.update { it.copy(errorMessage = result.message) }
    }
}
```

### 5. **Loading States**

```kotlin
// Before operation
_uiState.update { it.copy(isLoading = true) }

// After operation (success or error)
_uiState.update { it.copy(isLoading = false) }
```

---

## 🎨 UI State Management Pattern

### State Structure
```kotlin
data class CreatePinState(
    // Data
    val title: String = "",
    val description: String = "",
    val selectedFiles: List<File> = emptyList(),
    
    // UI State
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val errorMessage: String? = null,
    
    // Navigation
    val isPinCreated: Boolean = false,
    val step: CreatePinStep = CreatePinStep.MEDIA
)
```

### State Updates
```kotlin
// Single field update
fun onTitleChange(title: String) {
    _state.value = _state.value.copy(
        title = title,
        errorMessage = null  // Clear error when user types
    )
}

// Multiple field update
fun createPin() {
    _state.value = _state.value.copy(
        isCreating = true,
        errorMessage = null
    )
}
```

---

## 🔄 Complete Action Flow Summary

### For ANY User Action:

1. **User interacts** → Button click, text input, selection, etc.
2. **Screen captures** → `onClick = { viewModel.action() }`
3. **ViewModel receives** → Function called
4. **State updated** → Loading/optimistic update
5. **Use Case invoked** → Business logic
6. **Repository called** → Data access
7. **API request** → HTTP call
8. **Backend responds** → Success/Error
9. **Result flows back** → Through layers
10. **State updated** → Success/Error state
11. **UI recomposes** → Automatic update via StateFlow

---

## 💡 Best Practices

### ✅ DO:
- Update state immediately for loading indicators
- Use `StateFlow` for reactive UI updates
- Handle errors in ViewModel
- Use `viewModelScope.launch` for coroutines
- Clear errors when user interacts again

### ❌ DON'T:
- Make API calls directly from Screen
- Update state from multiple threads simultaneously
- Forget to handle loading/error states
- Mutate state directly (always use `.copy()` or `.update()`)

---

## 🎓 Learning Points

1. **Reactive UI**: StateFlow automatically triggers UI updates
2. **Separation of Concerns**: Each layer has a specific responsibility
3. **Unidirectional Flow**: Data flows in one direction
4. **Error Handling**: Errors flow back through layers
5. **State Management**: Single source of truth in ViewModel

---

**Remember**: The UI is a **reactive view** of the state. When state changes, UI automatically updates! 🎯

