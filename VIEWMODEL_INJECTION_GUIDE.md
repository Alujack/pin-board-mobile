# 🔌 ViewModel Injection Guide - How ViewModels Get Into Screens

## 🎯 Overview

This guide explains the **complete injection mechanism** of how ViewModels are automatically provided to Compose screens using **Hilt (Dependency Injection)**.

---

## 🔄 The Complete Injection Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION SETUP                        │
│  @HiltAndroidApp                                            │
│  class App : Application()                                  │
│  • Hilt initializes dependency graph                        │
│  • Scans for @Module, @Provides, @Binds                     │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│              DEPENDENCY GRAPH BUILDING                      │
│  • Modules provide dependencies                             │
│  • UseCases are created                                     │
│  • Repositories are created                                 │
│  • ViewModels are ready to be created                      │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│              VIEWMODEL DECLARATION                          │
│  @HiltViewModel                                            │
│  class HomeViewModel @Inject constructor(                   │
│    private val getAllPinsUseCase: GetAllPinsUseCase        │
│  )                                                          │
│  • Hilt knows how to create this ViewModel                 │
│  • Dependencies (UseCases) are injected                    │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│              SCREEN COMPOSABLE                              │
│  @Composable                                                │
│  fun HomeScreen(                                            │
│    viewModel: HomeViewModel = hiltViewModel()              │
│  )                                                          │
│  • hiltViewModel() function retrieves ViewModel            │
│  • Hilt provides the ViewModel instance                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 📝 Step-by-Step: How It Works

### Step 1: Application Setup (Hilt Initialization)

**File**: `app/src/main/java/kh/edu/rupp/fe/ite/pinboard/app/App.kt`

```kotlin
@HiltAndroidApp  // ← This annotation tells Hilt to initialize
class App : Application() {
    // Hilt automatically generates code here
    // Creates dependency graph at compile time
}
```

**What happens:**
- Hilt scans your codebase for `@Module`, `@Provides`, `@Binds`, `@HiltViewModel`
- Builds a dependency graph
- Generates code to provide dependencies

---

### Step 2: ViewModel Declaration (Mark for Injection)

**File**: `CreatePinViewModel.kt`

```kotlin
@HiltViewModel  // ← This tells Hilt: "This ViewModel can be injected"
class CreatePinViewModel @Inject constructor(
    // Dependencies are injected here
    private val createPinUseCase: CreatePinUseCase,
    private val getBoardsUseCase: GetBoardsUseCase,
    private val createBoardUseCase: CreateBoardUseCase
) : ViewModel() {
    // ViewModel code...
}
```

**Key Points:**
- `@HiltViewModel` annotation marks the ViewModel for injection
- `@Inject constructor(...)` tells Hilt what dependencies to inject
- Hilt automatically provides the UseCases (they're also marked with `@Inject`)

---

### Step 3: Screen Gets ViewModel (The Magic Happens)

**File**: `CreatePinScreen.kt`

```kotlin
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun CreatePinScreen(
    onNavigateBack: () -> Unit,
    onPinCreated: () -> Unit,
    viewModel: CreatePinViewModel = hiltViewModel()  // ← MAGIC HERE!
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Use viewModel...
}
```

**What `hiltViewModel()` does:**
1. Looks up the ViewModel in Hilt's dependency graph
2. Creates it if it doesn't exist (or reuses existing instance)
3. Injects all dependencies (UseCases)
4. Returns the ViewModel instance

---

## 🔍 Two Ways to Get ViewModel in Compose

### Method 1: `hiltViewModel()` (Standard - Most Common)

```kotlin
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()  // ← Default parameter
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

**When to use:**
- Standard screen ViewModels
- No navigation arguments needed
- Most common pattern

---

### Method 2: `hiltViewModel()` with Navigation (For Navigation Arguments)

```kotlin
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PinDetailScreen(
    pinId: String,
    viewModel: PinDetailViewModel = hiltViewModel()  // ← Gets SavedStateHandle automatically
) {
    // ViewModel can access pinId via SavedStateHandle
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

**When to use:**
- When ViewModel needs navigation arguments
- When using `SavedStateHandle` in ViewModel

**Example ViewModel with SavedStateHandle:**

```kotlin
@HiltViewModel
class PinDetailViewModel @Inject constructor(
    private val getPinUseCase: GetPinUseCase,
    savedStateHandle: SavedStateHandle  // ← Automatically provided by Hilt
) : ViewModel() {
    
    val pinId: String = savedStateHandle.get<String>("pinId") ?: ""
    
    init {
        loadPin(pinId)
    }
}
```

---

## 🏗️ Complete Dependency Chain Example

Let's trace how dependencies flow from bottom to top:

```
┌─────────────────────────────────────────────────────────────┐
│                    API SERVICE                              │
│  interface PinApi { ... }                                   │
└───────────────────────┬─────────────────────────────────────┘
                        │ Provided by NetworkModule
                        ↓
┌─────────────────────────────────────────────────────────────┐
│              REPOSITORY IMPLEMENTATION                      │
│  class PinRepositoryImpl @Inject constructor(              │
│    private val api: PinApi  // ← Injected                  │
│  )                                                          │
└───────────────────────┬─────────────────────────────────────┘
                        │ Bound to interface
                        ↓
┌─────────────────────────────────────────────────────────────┐
│              REPOSITORY INTERFACE                           │
│  interface PinRepository { ... }                           │
└───────────────────────┬─────────────────────────────────────┘
                        │ Injected into UseCase
                        ↓
┌─────────────────────────────────────────────────────────────┐
│                    USE CASE                                 │
│  class GetAllPinsUseCase @Inject constructor(              │
│    private val repository: PinRepository  // ← Injected     │
│  )                                                          │
└───────────────────────┬─────────────────────────────────────┘
                        │ Injected into ViewModel
                        ↓
┌─────────────────────────────────────────────────────────────┐
│                    VIEWMODEL                                │
│  @HiltViewModel                                            │
│  class HomeViewModel @Inject constructor(                  │
│    private val getAllPinsUseCase: GetAllPinsUseCase        │
│    // ↑ Injected                                           │
│  )                                                          │
└───────────────────────┬─────────────────────────────────────┘
                        │ Retrieved via hiltViewModel()
                        ↓
┌─────────────────────────────────────────────────────────────┐
│                    SCREEN                                   │
│  @Composable                                                │
│  fun HomeScreen(                                            │
│    viewModel: HomeViewModel = hiltViewModel()              │
│  )                                                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 💻 Real Code Examples from Your Project

### Example 1: HomeScreen

**Screen** (`HomeScreen.kt`):
```kotlin
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onPinClick: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()  // ← Injection
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Use viewModel...
}
```

**ViewModel** (`HomeViewModel.kt`):
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPinsUseCase: GetAllPinsUseCase,
    private val savePinUseCase: SavePinUseCase,
    private val unsavePinUseCase: UnsavePinUseCase,
    private val togglePinLikeUseCase: TogglePinLikeUseCase
) : ViewModel() {
    // ViewModel code...
}
```

---

### Example 2: CreatePinScreen

**Screen** (`CreatePinScreen.kt`):
```kotlin
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun CreatePinScreen(
    onNavigateBack: () -> Unit,
    onPinCreated: () -> Unit,
    viewModel: CreatePinViewModel = hiltViewModel()  // ← Injection
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Use viewModel...
}
```

**ViewModel** (`CreatePinViewModel.kt`):
```kotlin
@HiltViewModel
class CreatePinViewModel @Inject constructor(
    private val createPinUseCase: CreatePinUseCase,
    private val getBoardsUseCase: GetBoardsUseCase,
    private val createBoardUseCase: CreateBoardUseCase
) : ViewModel() {
    // ViewModel code...
}
```

---

### Example 3: PinDetailScreen (with SavedStateHandle)

**Screen** (`PinDetailScreen.kt`):
```kotlin
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PinDetailScreen(
    pinId: String,
    viewModel: PinDetailViewModel = hiltViewModel()  // ← Injection
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Use viewModel...
}
```

**ViewModel** (`PinDetailViewModel.kt`):
```kotlin
@HiltViewModel
class PinDetailViewModel @Inject constructor(
    private val getPinUseCase: GetPinUseCase,
    savedStateHandle: SavedStateHandle  // ← Automatically provided
) : ViewModel() {
    // Can access navigation arguments via SavedStateHandle
}
```

---

## 🔑 Key Concepts Explained

### 1. **`@HiltViewModel` Annotation**

```kotlin
@HiltViewModel  // ← Tells Hilt: "This ViewModel can be injected"
class MyViewModel @Inject constructor(...) : ViewModel()
```

**Purpose:**
- Marks the ViewModel for Hilt injection
- Hilt generates code to create this ViewModel
- Required for `hiltViewModel()` to work

---

### 2. **`@Inject constructor()`**

```kotlin
@Inject constructor(
    private val useCase: MyUseCase  // ← Dependency injection
)
```

**Purpose:**
- Tells Hilt what dependencies this ViewModel needs
- Hilt automatically provides these dependencies
- Dependencies must also be injectable (marked with `@Inject`)

---

### 3. **`hiltViewModel()` Function**

```kotlin
viewModel: HomeViewModel = hiltViewModel()
```

**What it does:**
- **Retrieves** existing ViewModel instance OR **creates** new one
- **Injects** all dependencies automatically
- **Scoped** to the composable's lifecycle
- **Returns** the ViewModel instance

**Behind the scenes:**
```kotlin
// Simplified version of what hiltViewModel() does:
fun <T : ViewModel> hiltViewModel(): T {
    // 1. Get ViewModelStoreOwner (from Compose context)
    // 2. Check if ViewModel already exists
    // 3. If not, create new instance using Hilt
    // 4. Inject dependencies
    // 5. Return ViewModel
}
```

---

### 4. **ViewModel Lifecycle**

```
Screen Composed
    ↓
hiltViewModel() called
    ↓
ViewModel created (if first time)
    ↓
Dependencies injected
    ↓
ViewModel.init { } runs
    ↓
Screen uses ViewModel
    ↓
Screen Disposed
    ↓
ViewModel survives (if navigation keeps it)
    ↓
Screen Composed again
    ↓
Same ViewModel instance reused (if exists)
```

**Important:**
- ViewModel survives configuration changes
- ViewModel is scoped to navigation graph
- Same instance reused when navigating back

---

## 📚 Import Statements

### For Standard ViewModels:
```kotlin
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
```

### For Navigation ViewModels:
```kotlin
import androidx.hilt.navigation.compose.hiltViewModel
```

**Difference:**
- `lifecycle.viewmodel.compose.hiltViewModel` - Standard lifecycle scoping
- `navigation.compose.hiltViewModel` - Navigation scoping (can access SavedStateHandle)

**In your project, both are used:**
- `CreatePinScreen` uses `lifecycle.viewmodel.compose.hiltViewModel`
- `HomeScreen` uses `navigation.compose.hiltViewModel`

---

## 🎯 Complete Example: Full Flow

### 1. Application Setup
```kotlin
// App.kt
@HiltAndroidApp
class App : Application()
```

### 2. Module Provides Dependencies
```kotlin
// NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun providePinApi(): PinApi = ...
}
```

### 3. UseCase Gets Repository
```kotlin
// GetAllPinsUseCase.kt
class GetAllPinsUseCase @Inject constructor(
    private val repository: PinRepository  // ← Injected
) {
    suspend operator fun invoke() = repository.getAllPins()
}
```

### 4. ViewModel Gets UseCase
```kotlin
// HomeViewModel.kt
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPinsUseCase: GetAllPinsUseCase  // ← Injected
) : ViewModel() {
    fun loadPins() {
        viewModelScope.launch {
            val result = getAllPinsUseCase()  // ← Use it
        }
    }
}
```

### 5. Screen Gets ViewModel
```kotlin
// HomeScreen.kt
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()  // ← Injected
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Use viewModel...
}
```

---

## ❓ Common Questions

### Q: Do I need to manually create ViewModel instances?
**A:** No! `hiltViewModel()` does it automatically.

### Q: What if ViewModel needs Activity/Fragment context?
**A:** Use `@ActivityContext` or `@FragmentContext` qualifiers in ViewModel constructor.

### Q: Can I pass parameters to ViewModel?
**A:** Yes, via `SavedStateHandle` for navigation arguments, or use ViewModelFactory (advanced).

### Q: What's the difference between `hiltViewModel()` and `viewModel()`?
**A:** 
- `hiltViewModel()` - Uses Hilt for dependency injection
- `viewModel()` - Manual ViewModel creation (no DI)

### Q: Can I test ViewModels with Hilt?
**A:** Yes! Use `@HiltAndroidTest` and `HiltTestRule` for integration tests.

---

## ✅ Best Practices

### ✅ DO:
- Always use `hiltViewModel()` for ViewModel injection
- Mark ViewModels with `@HiltViewModel`
- Use `@Inject constructor()` for dependencies
- Use default parameter: `viewModel: MyViewModel = hiltViewModel()`

### ❌ DON'T:
- Don't manually create ViewModel instances
- Don't forget `@HiltViewModel` annotation
- Don't use `viewModel()` without Hilt setup
- Don't pass ViewModel as parameter (let Hilt inject it)

---

## 🎓 Summary

**The Injection Process:**

1. **Application** → `@HiltAndroidApp` initializes Hilt
2. **ViewModel** → `@HiltViewModel` + `@Inject constructor()` marks it for injection
3. **Screen** → `hiltViewModel()` retrieves/creates ViewModel
4. **Hilt** → Automatically injects all dependencies
5. **Screen** → Uses ViewModel instance

**Key Takeaway:**
> **`hiltViewModel()` is a magic function that automatically provides your ViewModel with all its dependencies injected!** 🪄

You just need to:
- Mark ViewModel with `@HiltViewModel`
- Use `@Inject constructor()` for dependencies
- Call `hiltViewModel()` in your screen

That's it! Hilt handles the rest! 🎉

