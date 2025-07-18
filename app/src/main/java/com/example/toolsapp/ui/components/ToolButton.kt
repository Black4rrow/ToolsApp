package com.example.toolsapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ToolButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var buttonColor by remember { mutableStateOf(Color.Transparent) }

    ElevatedButton(
        onClick = {
            onClick()
            buttonColor = Color(
                red = (0..255).random() / 255f,
                green = (0..255).random() / 255f,
                blue = (0..255).random() / 255f
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = Color.White
        )
    ) {
        Text(text = text)
    }
}