package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board

@Composable
fun BoardSelectionSection(
    boards: List<Board>,
    selectedBoard: Board?,
    isLoading: Boolean,
    onBoardSelected: (Board) -> Unit
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
                    androidx.compose.material3.CircularProgressIndicator(
                        color = Color(0xFFE60023)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
