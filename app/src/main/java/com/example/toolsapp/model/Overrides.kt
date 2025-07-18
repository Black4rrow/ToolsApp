package com.example.toolsapp.model

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

fun Modifier.topBorder(height: Dp, color: Color) = run {
    this.drawWithContent {
        drawContent()
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = height.toPx()
        )
    }
}

fun Modifier.rightBorder(width: Dp, color: Color) = run {
    this.drawWithContent {
        drawContent()
        drawLine(
            color = color,
            start = Offset(size.width, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = width.toPx()
        )
    }
}

fun Modifier.bottomBorder(height: Dp, color: Color) = run {
    this.drawWithContent {
        drawContent()
        drawLine(
            color = color,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = height.toPx()
        )
    }
}

fun Modifier.leftBorder(width: Dp, color: Color) = run {
    this.drawWithContent {
        drawContent()
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = width.toPx()
        )
    }
}
