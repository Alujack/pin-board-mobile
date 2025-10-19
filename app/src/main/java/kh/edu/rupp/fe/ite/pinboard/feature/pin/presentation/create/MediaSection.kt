package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun MediaSelectionSection(
    selectedFiles: List<File>,
    onSelectMedia: () -> Unit,
    onRemoveFile: (File) -> Unit,
    onSelectMediaFallback: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Add media",
                fontSize = 18.sp,
                color = Color(0xFF1C1C1C)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Upload an image or video (max 100MB)",
                fontSize = 14.sp,
                color = Color(0xFF6B6B6B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedFiles.isEmpty()) {
                OutlinedButton(
                    onClick = onSelectMedia,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE60023)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Add photos or videos")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { onSelectMediaFallback() }) {
                    Text("Use file picker (multi-select)")
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = selectedFiles,
                            key = { it.absolutePath }
                        ) { file ->
                            MediaPreviewItem(
                                file = file,
                                onRemove = { onRemoveFile(file) }
                            )
                        }
                        item {
                            // Add tile
                            OutlinedButton(
                                onClick = onSelectMedia,
                                modifier = Modifier
                                    .size(100.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFE60023)
                                )
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Add")
                                }
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = onSelectMedia,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE60023)
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add more media")
                    }
                    TextButton(onClick = { onSelectMediaFallback() }) {
                        Text("Use file picker (multi-select)")
                    }
                }
            }
        }
    }
}
