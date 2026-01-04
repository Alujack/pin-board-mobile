package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.create

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePinScreen(
    onNavigateBack: () -> Unit,
    onPinCreated: () -> Unit,
    viewModel: CreatePinViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle successful pin creation
    LaunchedEffect(state.isPinCreated) {
        if (state.isPinCreated) {
            onPinCreated()
        }
    }

    // Shared handler to turn picked URIs into files
    val handlePickedUris: (List<Uri>) -> Unit = { uris ->
        val resolver = context.contentResolver
        val files = uris.mapNotNull { uri ->
            try {
                val mime = resolver.getType(uri)
                val ext = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(mime ?: "")
                    ?: when {
                        uri.toString().endsWith(".mp4", true) -> "mp4"
                        uri.toString().endsWith(".mov", true) -> "mov"
                        uri.toString().endsWith(".webm", true) -> "webm"
                        uri.toString().endsWith(".jpg", true) -> "jpg"
                        uri.toString().endsWith(".jpeg", true) -> "jpeg"
                        uri.toString().endsWith(".png", true) -> "png"
                        else -> "bin"
                    }
                val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.$ext")
                resolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { out -> input.copyTo(out) }
                }
                file
            } catch (e: Exception) {
                null
            }
        }
        if (files.isNotEmpty()) viewModel.onFilesSelected(files)
    }

    // Primary: Photo Picker (multiple, images + videos)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = 10
        )
    ) { uris: List<Uri> -> handlePickedUris(uris) }

    // Fallback: Storage Access Framework multi-docs
    val documentsPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> -> handlePickedUris(uris) }

    val launchMediaPicker = remember {
        {
            // Try the Photo Picker first
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            )
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            "Create Pin",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            when (state.step) {
                                CreatePinStep.MEDIA -> onNavigateBack()
                                else -> viewModel.prevStep()
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color(0xFF1C1C1C)
                    ),
                    actions = {
                        when (state.step) {
                            CreatePinStep.BOARD -> {
                                Button(
                                    onClick = { viewModel.createPin() },
                                    enabled = !state.isCreating && state.title.isNotBlank() && 
                                            state.description.isNotBlank() && 
                                            state.selectedBoard != null && 
                                            state.selectedFiles.isNotEmpty(),
                                    shape = RoundedCornerShape(24.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE60023),
                                        contentColor = Color.White
                                    )
                                ) {
                                    if (state.isCreating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Publish")
                                    }
                                }
                            }
                            else -> {
                                TextButton(onClick = { viewModel.nextStep() }) {
                                    Text("Next")
                                }
                            }
                        }
                    }
                )
                Divider(color = Color(0x11000000))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (state.step) {
                CreatePinStep.MEDIA -> {
                    MediaSelectionSection(
                        selectedFiles = state.selectedFiles,
                        onSelectMedia = { launchMediaPicker() },
                        onRemoveFile = { file -> viewModel.removeFile(file) },
                        onSelectMediaFallback = { documentsPickerLauncher.launch(arrayOf("image/*", "video/*")) }
                    )
                }
                CreatePinStep.DETAILS -> {
                    FormSection(
                        title = state.title,
                        description = state.description,
                        link = state.link,
                        onTitleChange = viewModel::onTitleChange,
                        onDescriptionChange = viewModel::onDescriptionChange,
                        onLinkChange = viewModel::onLinkChange
                    )
                }
                CreatePinStep.BOARD -> {
                    BoardSelectionSection(
                        boards = state.boards,
                        selectedBoard = state.selectedBoard,
                        isLoading = state.isLoading,
                        isCreatingBoard = state.isCreatingBoard,
                        showCreateBoardDialog = state.showCreateBoardDialog,
                        newBoardName = state.newBoardName,
                        newBoardDescription = state.newBoardDescription,
                        onBoardSelected = viewModel::onBoardSelected,
                        onShowCreateBoardDialog = viewModel::showCreateBoardDialog,
                        onHideCreateBoardDialog = viewModel::hideCreateBoardDialog,
                        onNewBoardNameChange = viewModel::onNewBoardNameChange,
                        onNewBoardDescriptionChange = viewModel::onNewBoardDescriptionChange,
                        onCreateBoard = viewModel::createBoard
                    )
                }
            }

            // Error Message
            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.errorMessage ?: "",
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }
                }
            }
        }
    }
}

 
