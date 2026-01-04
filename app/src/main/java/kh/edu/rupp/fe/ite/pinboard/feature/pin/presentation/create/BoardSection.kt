package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board

@Composable
fun BoardSelectionSection(
    boards: List<Board>,
    selectedBoard: Board?,
    isLoading: Boolean,
    isCreatingBoard: Boolean,
    showCreateBoardDialog: Boolean,
    newBoardName: String,
    newBoardDescription: String,
    onBoardSelected: (Board) -> Unit,
    onShowCreateBoardDialog: () -> Unit,
    onHideCreateBoardDialog: () -> Unit,
    onNewBoardNameChange: (String) -> Unit,
    onNewBoardDescriptionChange: (String) -> Unit,
    onCreateBoard: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Choose a board",
                fontSize = 18.sp,
                color = Color(0xFF1C1C1C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFE60023)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Create Board Button
                    CreateBoardButton(
                        onClick = onShowCreateBoardDialog
                    )
                    
                    // Board List
                    boards.forEach { board ->
                        BoardItem(
                            board = board,
                            isSelected = selectedBoard?._id == board._id,
                            onSelect = { onBoardSelected(board) }
                        )
                    }
                }
            }
        }
    }
    
    // Create Board Dialog
    if (showCreateBoardDialog) {
        CreateBoardDialog(
            boardName = newBoardName,
            boardDescription = newBoardDescription,
            isCreating = isCreatingBoard,
            onNameChange = onNewBoardNameChange,
            onDescriptionChange = onNewBoardDescriptionChange,
            onDismiss = onHideCreateBoardDialog,
            onCreate = onCreateBoard
        )
    }
}

@Composable
fun BoardItem(
    board: Board,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE60023).copy(alpha = 0.1f) else Color.Transparent
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE60023))
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Folder,
                contentDescription = null,
                tint = if (isSelected) Color(0xFFE60023) else Color(0xFF6B6B6B)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = board.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFFE60023) else Color(0xFF1C1C1C)
                    )
                )
                if (!board.description.isNullOrBlank()) {
                    Text(
                        text = board.description,
                        fontSize = 14.sp,
                        color = Color(0xFF6B6B6B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${board.pinCount} pins",
                    fontSize = 12.sp,
                    color = Color(0xFF6B6B6B)
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFFE60023)
                )
            } else {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFB0B0B0)
                )
            }
        }
    }
}

@Composable
fun CreateBoardButton(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = null,
                tint = Color(0xFFE60023)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Create board",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE60023)
                )
            )
        }
    }
}

@Composable
fun CreateBoardDialog(
    boardName: String,
    boardDescription: String,
    isCreating: Boolean,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isCreating) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create board",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1C)
                )
                
                OutlinedTextField(
                    value = boardName,
                    onValueChange = onNameChange,
                    label = { Text("Board name") },
                    placeholder = { Text("Enter board name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCreating,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE60023),
                        focusedLabelColor = Color(0xFFE60023)
                    )
                )
                
                OutlinedTextField(
                    value = boardDescription,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description (optional)") },
                    placeholder = { Text("Enter description") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCreating,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE60023),
                        focusedLabelColor = Color(0xFFE60023)
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isCreating
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = onCreate,
                        modifier = Modifier.weight(1f),
                        enabled = !isCreating && boardName.trim().isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE60023),
                            contentColor = Color.White
                        )
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}
