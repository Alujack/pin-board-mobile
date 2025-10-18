package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FormSection(
    title: String,
    description: String,
    link: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLinkChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Details",
                fontSize = 18.sp,
                color = Color(0xFF1C1C1C)
            )

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                placeholder = { Text("Add a title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE60023),
                    focusedLabelColor = Color(0xFFE60023)
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                placeholder = { Text("Tell everyone what your Pin is about") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE60023),
                    focusedLabelColor = Color(0xFFE60023)
                ),
                maxLines = 4
            )

            OutlinedTextField(
                value = link,
                onValueChange = onLinkChange,
                label = { Text("Link (optional)") },
                placeholder = { Text("Add a link") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE60023),
                    focusedLabelColor = Color(0xFFE60023)
                )
            )
        }
    }
}
